/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.callback;

import com.mcleodmoores.excel4j.ConstructorDefinition;
import com.mcleodmoores.excel4j.ConstructorMetadata;
import com.mcleodmoores.excel4j.FunctionDefinition;
import com.mcleodmoores.excel4j.FunctionMetadata;
import com.mcleodmoores.excel4j.FunctionType;
import com.mcleodmoores.excel4j.XLArgument;
import com.mcleodmoores.excel4j.XLConstructor;
import com.mcleodmoores.excel4j.XLFunction;
import com.mcleodmoores.excel4j.XLNamespace;
import com.mcleodmoores.excel4j.javacode.ConstructorInvoker;
import com.mcleodmoores.excel4j.javacode.MethodInvoker;
import com.mcleodmoores.excel4j.lowlevel.LowLevelExcelCallback;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLInteger;
import com.mcleodmoores.excel4j.values.XLLocalReference;
import com.mcleodmoores.excel4j.values.XLMultiReference;

/**
 * Provides a layer to process function metadata into relatively raw calls back to Excel.
 */
public class DefaultExcelCallback implements ExcelCallback {
  private static final int VARARGS_MAX_PARAMS = 32;
  private final LowLevelExcelCallback _rawCallback;

  /**
   * Create a callback adapter.
   * @param rawCallback  the raw callback interface to call through to
   */
  public DefaultExcelCallback(final LowLevelExcelCallback rawCallback) {
    _rawCallback = rawCallback;
  }

  @Override
  public LowLevelExcelCallback getLowLevelExcelCallback() {
    return _rawCallback;
  }

  @Override
  public byte[] getBinaryName(final long handle, final long length) {
    return new byte[0];
  }

  @Override
  public void registerFunction(final FunctionDefinition functionDefinition) {
    final FunctionMetadata functionMetadata = functionDefinition.getFunctionMetadata();
    final MethodInvoker methodInvoker = functionDefinition.getMethodInvoker();
    final XLNamespace namespaceAnnotation = functionMetadata.getNamespace();
    final XLFunction functionAnnotation = functionMetadata.getFunctionSpec();
    final XLArgument[] argumentAnnotations = functionMetadata.getArguments();
    final String exportName = functionDefinition.getExportName();
    final String functionName = buildFunctionName(methodInvoker, namespaceAnnotation, functionAnnotation);
    final boolean isVarArgs = methodInvoker.isVarArgs();
    final String argumentNames = buildArgNames(argumentAnnotations);
    final Integer functionTypeInt = getFunctionType(functionAnnotation);
    final String signature = buildFunctionSignature(functionAnnotation, argumentAnnotations, methodInvoker);
    final String functionCategory = buildFunctionCategory(functionAnnotation, methodInvoker);
    final String helpTopic = buildHelpTopic(functionAnnotation);
    final String description = buildDescription(functionAnnotation);
    final String[] argsHelp = buildArgsHelp(argumentAnnotations);
    _rawCallback.xlfRegister(functionDefinition.getExportNumber(), exportName, isVarArgs,  signature, functionName, argumentNames,
                             functionTypeInt, functionCategory, "", helpTopic, description, argsHelp);
  }

  @Override
  public void registerConstructor(final ConstructorDefinition classDefinition) {
    final ConstructorMetadata constructorMetadata = classDefinition.getConstructorMetadata();
    final ConstructorInvoker constructorInvoker = classDefinition.getConstructorInvoker();
    final XLNamespace namespaceAnnotation = constructorMetadata.getNamespace();
    final XLConstructor constructorAnnotation = constructorMetadata.getConstructorSpec();
    final XLArgument[] argumentAnnotations = constructorMetadata.getArguments();
    final String exportName = classDefinition.getExportName();
    final String className = buildConstructorName(constructorInvoker, namespaceAnnotation, constructorAnnotation);
    final boolean isVarArgs = constructorInvoker.isVarArgs();
    final String argumentNames = buildArgNames(argumentAnnotations);
    final Integer functionTypeInt = getConstructorType();
    final String signature = buildConstructorSignature(constructorAnnotation, argumentAnnotations, constructorInvoker);
    final String functionCategory = buildConstructorCategory(constructorAnnotation, constructorInvoker);
    final String helpTopic = buildHelpTopic(constructorAnnotation);
    final String description = buildDescription(constructorAnnotation);
    final String[] argsHelp = buildArgsHelp(argumentAnnotations);
    _rawCallback.xlfRegister(classDefinition.getExportNumber(), exportName, isVarArgs,  signature, className, argumentNames,
                             functionTypeInt, functionCategory, "", helpTopic, description, argsHelp);
  }

  private static String[] buildArgsHelp(final XLArgument[] argumentAnnotations) {
    final String[] results = new String[argumentAnnotations.length];
    for (int i = 0; i < argumentAnnotations.length; i++) {
      if (argumentAnnotations[i] != null && !argumentAnnotations[i].description().isEmpty()) {
        results[i] = argumentAnnotations[i].description();
      } else {
        results[i] = null;
      }
    }
    return results;
  }

  private static String buildDescription(final XLFunction functionAnnotation) {
    if (functionAnnotation != null && !functionAnnotation.description().isEmpty()) {
      return functionAnnotation.description();
    }
    return null;
  }

  private static String buildDescription(final XLConstructor constructorAnnotation) {
    if (constructorAnnotation != null && !constructorAnnotation.description().isEmpty()) {
      return constructorAnnotation.description();
    }
    return null;
  }

  private static String buildHelpTopic(final XLFunction functionAnnotation) {
    if (functionAnnotation != null && !functionAnnotation.helpTopic().isEmpty()) {
      return functionAnnotation.helpTopic();
    }
    return null;
  }

  private static String buildHelpTopic(final XLConstructor constructorAnnotation) {
    if (constructorAnnotation != null && !constructorAnnotation.helpTopic().isEmpty()) {
      return constructorAnnotation.helpTopic();
    }
    return null;
  }

  private static String buildFunctionCategory(final XLFunction functionAnnotation, final MethodInvoker methodInvoker) {
    if (functionAnnotation != null && !functionAnnotation.category().isEmpty()) {
      return functionAnnotation.category();
    }
    return methodInvoker.getMethodDeclaringClass().getSimpleName();
  }

  private static String buildConstructorCategory(final XLConstructor constructorAnnotation, final ConstructorInvoker constructorInvoker) {
    if (constructorAnnotation != null && !constructorAnnotation.category().isEmpty()) {
      return constructorAnnotation.category();
    }
    return constructorInvoker.getDeclaringClass().getSimpleName();
  }

  private static String buildFunctionSignature(final XLFunction functionAnnotation, final XLArgument[] argumentAnnotations, final MethodInvoker methodInvoker) {
    final StringBuilder signature = new StringBuilder();
    final Class<?> excelReturnType = methodInvoker.getExcelReturnType();
    final Class<?>[] parameterTypes = methodInvoker.getExcelParameterTypes();
    final boolean isVolatile = (functionAnnotation != null) ? functionAnnotation.isVolatile() : false; // default
    final boolean isMTSafe = (functionAnnotation != null) ? functionAnnotation.isMultiThreadSafe() : true; // default, this is the 2010s, yo.
    final boolean isMacroEquivalent = (functionAnnotation != null) ? functionAnnotation.isMacroEquivalent() : false; // default
    final boolean isAsynchronous = (functionAnnotation != null) ? functionAnnotation.isAsynchronous() : false; // default
    final FunctionType functionType = (functionAnnotation != null) ? functionAnnotation.functionType() : FunctionType.FUNCTION; // default;
    if ((isVolatile && isMTSafe) || (isMTSafe && isMacroEquivalent)) {
      throw new Excel4JRuntimeException("Illegal combination of XLFunction attributes, cannot be volatile & thread-safe or macro-equivalent & thread-safe");
    }
    // Return type character
    if (functionType == FunctionType.COMMAND) {
      if (!excelReturnType.isAssignableFrom(XLInteger.class)) {
        throw new Excel4JRuntimeException("Commands must have a return type XLInteger (gets converted to type J (int))");
      }
      signature.append("J"); // means int, but we'll convert from XLInteger to make the class hierarchy cleaner.
    } else {
      if (isAsynchronous) {
        signature.append(">X"); // means void function first parameter is asynchronous callback handle, which we don't expose to the user.
      } else {
        if (excelReturnType.isAssignableFrom(XLLocalReference.class)
            || excelReturnType.isAssignableFrom(XLMultiReference.class)) {
          // REVIEW: Not sure if this is a valid thing to do.
          signature.append("U"); // XLOPER12 range/ref/array. I've not idea if this is even valid. Not clear in docs.
        } else {
          signature.append("Q"); // XLOPER12
        }
      }
    }
    // Parameters
    for (int i = 0; i < parameterTypes.length; i++) {
      final XLArgument argumentAnnotation = argumentAnnotations[i];
      if (argumentAnnotation != null && argumentAnnotation.referenceType()) {
        if (!isMacroEquivalent) {
          throw new Excel4JRuntimeException("Cannot register reference type parameters if not a macro equivalent: "
                                             + "function annotation @XLFunction(isMacroEquivalent = true) required");
        }
        signature.append("U"); // XLOPER12 byref
      } else {
        signature.append("Q"); // XLOPER12 byval
      }
    }
    if (methodInvoker.isVarArgs()) {
      if (parameterTypes.length == 0) {
        throw new IllegalStateException("Internal Error: Variable argument list function should have at least one parameter type");
      }
      final boolean isLastTypeReferenceType = argumentAnnotations[parameterTypes.length - 1].referenceType();
      for (int i = 0; i < VARARGS_MAX_PARAMS - parameterTypes.length; i++) {
        if (isLastTypeReferenceType) {
          signature.append("U"); // XLOPER12 byref
        } else {
          signature.append("Q"); // XLOPER12 byval
        }
      }
    }
    // Characters on the end -- we checked some invalid states at the start.
    if (isMacroEquivalent) {
      signature.append("#");
    } else if (isMTSafe) {
      signature.append("$");
    } else if (isVolatile) {
      signature.append("!");
    }
    return signature.toString();
  }

  private static String buildConstructorSignature(final XLConstructor constructorAnnotation, final XLArgument[] argumentAnnotations, final ConstructorInvoker constructorInvoker) {
    final StringBuilder signature = new StringBuilder();
    final Class<?> excelReturnType = constructorInvoker.getExcelReturnType();
    final Class<?>[] parameterTypes = constructorInvoker.getExcelParameterTypes();
    final FunctionType functionType = FunctionType.FUNCTION;
    // Parameters
    for (int i = 0; i < parameterTypes.length; i++) {
      final XLArgument argumentAnnotation = argumentAnnotations[i];
      if (argumentAnnotation != null && argumentAnnotation.referenceType()) {
        signature.append("U"); // XLOPER12 byref
      } else {
        signature.append("Q"); // XLOPER12 byval
      }
    }
    if (constructorInvoker.isVarArgs()) {
      if (parameterTypes.length == 0) {
        throw new IllegalStateException("Internal Error: Variable argument list function should have at least one parameter type");
      }
      final boolean isLastTypeReferenceType = argumentAnnotations[parameterTypes.length - 1].referenceType();
      for (int i = 0; i < VARARGS_MAX_PARAMS - parameterTypes.length; i++) {
        if (isLastTypeReferenceType) {
          signature.append("U"); // XLOPER12 byref
        } else {
          signature.append("Q"); // XLOPER12 byval
        }
      }
    }
    // Characters on the end -- we checked some invalid states at the start.
    return signature.toString();
  }

  /**
   * Build the function name string using the namespace if specified.
   * @param methodInvoker  the method invoker for this function, not null
   * @param namespaceAnnotation  the namespace annotation if there is one, or null if there isn't.
   * @param functionAnnotation  the function annotation is there is one, or null if there isn't.
   * @return the name of the function to register with Excel
   */
  private static String buildFunctionName(final MethodInvoker methodInvoker, final XLNamespace namespaceAnnotation, final XLFunction functionAnnotation) {
    final StringBuilder functionName = new StringBuilder();
    if (namespaceAnnotation != null) {
      functionName.append(namespaceAnnotation.value());
    }
    if (functionAnnotation != null) {
      if (!functionAnnotation.name().isEmpty()) {
        functionName.append(functionAnnotation.name());
      } else {
        functionName.append(methodInvoker.getMethodName());
      }
    }
    return functionName.toString();
  }

  private static String buildConstructorName(final ConstructorInvoker constructorInvoker, final XLNamespace namespaceAnnotation, final XLConstructor constructorAnnotation) {
    final StringBuilder functionName = new StringBuilder();
    if (namespaceAnnotation != null) {
      functionName.append(namespaceAnnotation.value());
    }
    if (constructorAnnotation != null) {
      if (!constructorAnnotation.name().isEmpty()) {
        functionName.append(constructorAnnotation.name());
      } else {
        functionName.append(constructorInvoker.getClass().getSimpleName());
      }
    }
    return functionName.toString();
  }

  /**
   * Build the string containing a list of argument annotations.
   * @param argumentAnnotations  array of argument annotations, can contain nulls
   * @return  the argument annotations separated by a comma
   */
  private static String buildArgNames(final XLArgument[] argumentAnnotations) {
    final StringBuilder argumentNames = new StringBuilder();
    int argCounter = 1;

    for (int i = 0; i < argumentAnnotations.length; i++) {
      final XLArgument argumentAnnotation = argumentAnnotations[i];
      if (argumentAnnotation != null) {
        if (!argumentAnnotation.name().isEmpty()) {
          argumentNames.append(argumentAnnotation.name());
        } else {
          // TODO: try paranamer/JavaDocs? #46
          argumentNames.append(Integer.toString(argCounter));
        }
      } else {
        // TODO: try paranamer/JavaDocs? #46
        argumentNames.append(Integer.toString(argCounter));
      }
      if (i < argumentAnnotations.length - 1) {
        argumentNames.append(",");
      }
      argCounter++;
    }
    return argumentNames.toString();
  }

  /**
   * Get the type of the function
   * @param functionAnnotation the function annotation if there is one, null otherwise
   * @return the type, defaults to 1 (FUNCTION)
   */
  private static int getFunctionType(final XLFunction functionAnnotation) {
    if (functionAnnotation != null) {
      return functionAnnotation.functionType().getExcelValue();
    }
    return FunctionType.FUNCTION.getExcelValue();
  }

  private static int getConstructorType() {
    return FunctionType.FUNCTION.getExcelValue();
  }

}
