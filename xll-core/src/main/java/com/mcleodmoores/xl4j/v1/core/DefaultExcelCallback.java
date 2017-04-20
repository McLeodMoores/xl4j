/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.core;

import com.mcleodmoores.xl4j.v1.api.annotations.FunctionType;
import com.mcleodmoores.xl4j.v1.api.annotations.XLConstant;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.api.core.CallTarget;
import com.mcleodmoores.xl4j.v1.api.core.ConstructorInvoker;
import com.mcleodmoores.xl4j.v1.api.core.ExcelCallback;
import com.mcleodmoores.xl4j.v1.api.core.FieldGetter;
import com.mcleodmoores.xl4j.v1.api.core.FunctionDefinition;
import com.mcleodmoores.xl4j.v1.api.core.MethodInvoker;
import com.mcleodmoores.xl4j.v1.api.values.XLInteger;
import com.mcleodmoores.xl4j.v1.api.values.XLLocalReference;
import com.mcleodmoores.xl4j.v1.api.values.XLMultiReference;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;
import com.mcleodmoores.xl4j.v1.xll.LowLevelExcelCallback;

/**
 * Provides a layer to process function metadata into relatively raw calls back to Excel.
 */
public class DefaultExcelCallback implements ExcelCallback {
  private static final int VARARGS_MAX_PARAMS = 32;
  private final LowLevelExcelCallback _rawCallback;

  /**
   * Create a callback adapter.
   *
   * @param rawCallback
   *          the raw callback interface to call through to
   */
  public DefaultExcelCallback(final LowLevelExcelCallback rawCallback) {
    _rawCallback = ArgumentChecker.notNull(rawCallback, "rawCallback");
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
    final String functionName = functionMetadata.getName();
    final String exportName = functionDefinition.getExportName();
    final String signature = buildFunctionSignature(functionDefinition);
    final String functionCategory = buildFunctionCategory(functionDefinition);
    final boolean isVarArgs, isLongRunning, isAutoAsynchronous, isManualAsynchronous, isCallerRequired;
    final String argumentNames, helpTopic, description;
    final String[] argsHelp;
    final Integer functionTypeInt;
    if (functionMetadata.isConstantSpec()) {
      isVarArgs = false;
      final XLConstant constantAnnotation = functionMetadata.getConstantSpec();
      isLongRunning = false;
      isAutoAsynchronous = false;
      isManualAsynchronous = false;
      isCallerRequired = false;
      argumentNames = "";
      functionTypeInt = FunctionType.FUNCTION.getExcelValue();
      helpTopic = buildHelpTopic(constantAnnotation);
      description = buildDescription(constantAnnotation);
      argsHelp = new String[0];
    } else {
      isVarArgs = functionDefinition.isVarArgs();
      if (functionMetadata.getFunctionSpec() != null) {
        final XLFunction functionAnnotation = functionMetadata.getFunctionSpec();
        isLongRunning = functionAnnotation.isLongRunning();
        isAutoAsynchronous = functionAnnotation.isAutoAsynchronous();
        isManualAsynchronous = functionAnnotation.isManualAsynchronous();
        isCallerRequired = functionAnnotation.isCallerRequired();
        argumentNames = buildArgNames(functionDefinition);
        functionTypeInt = functionAnnotation.functionType().getExcelValue();
        helpTopic = buildHelpTopic(functionAnnotation);
        description = buildDescription(functionAnnotation);
        argsHelp = buildArgsHelp(functionDefinition);
      } else if (functionMetadata.getFunctionsSpec() != null) {
        final XLFunctions functionsAnnotation = functionMetadata.getFunctionsSpec();
        isLongRunning = functionsAnnotation.isLongRunning();
        isAutoAsynchronous = functionsAnnotation.isAutoAsynchronous();
        isManualAsynchronous = functionsAnnotation.isManualAsynchronous();
        isCallerRequired = functionsAnnotation.isCallerRequired();
        argumentNames = buildArgNames(functionDefinition);
        functionTypeInt = functionsAnnotation.functionType().getExcelValue();
        helpTopic = buildHelpTopic(functionsAnnotation);
        description = buildDescription(functionsAnnotation);
        argsHelp = buildArgsHelp(functionDefinition);
      } else {
        throw new XL4JRuntimeException("Unhandled function metadata type");
      }
    }
    _rawCallback.xlfRegister(functionDefinition.getExportNumber(), exportName, isVarArgs, isLongRunning,
        isAutoAsynchronous, isManualAsynchronous, isCallerRequired, signature, functionName, argumentNames,
        functionTypeInt, functionCategory, "", helpTopic, description, argsHelp);
  }

  private static String[] buildArgsHelp(final FunctionDefinition functionDefinition) {
    final XLParameter[] argumentAnnotations = functionDefinition.getFunctionMetadata().getParameters();
    final String[] results = new String[argumentAnnotations.length];
    for (int i = 0; i < argumentAnnotations.length; i++) {
      if (argumentAnnotations[i] != null && !argumentAnnotations[i].description().isEmpty()) {
        results[i] = argumentAnnotations[i].description();
      } else {
        results[i] = null;
      }
    }
    if (functionDefinition.getJavaTypeForFunction() == CallTarget.METHOD) {
      if (!functionDefinition.isStatic()) {
        final String[] resultsWithConstructorArg = new String[results.length + 1];
        resultsWithConstructorArg[0] = "";
        System.arraycopy(results, 0, resultsWithConstructorArg, 1, results.length);
      }
    }
    return results;
  }

  private static String buildDescription(final XLFunction functionAnnotation) {
    if (!functionAnnotation.description().isEmpty()) {
      return functionAnnotation.description();
    }
    return null;
  }

  private static String buildDescription(final XLFunctions functionsAnnotation) {
    if (!functionsAnnotation.description().isEmpty()) {
      return functionsAnnotation.description();
    }
    return null;
  }

  private static String buildDescription(final XLConstant constantAnnotation) {
    if (!constantAnnotation.description().isEmpty()) {
      return constantAnnotation.description();
    }
    return null;
  }

  private static String buildHelpTopic(final XLFunction functionAnnotation) {
    if (!functionAnnotation.helpTopic().isEmpty()) {
      return functionAnnotation.helpTopic();
    }
    return null;
  }

  private static String buildHelpTopic(final XLFunctions functionsAnnotation) {
    if (!functionsAnnotation.helpTopic().isEmpty()) {
      return functionsAnnotation.helpTopic();
    }
    return null;
  }

  private static String buildHelpTopic(final XLConstant constantAnnotation) {
    if (!constantAnnotation.helpTopic().isEmpty()) {
      return constantAnnotation.helpTopic();
    }
    return null;
  }

  private static String buildFunctionCategory(final FunctionDefinition functionDefinition) {
    final FunctionMetadata functionMetadata = functionDefinition.getFunctionMetadata();
    if (functionMetadata.isConstantSpec()) {
      final XLConstant constantAnnotation = functionMetadata.getConstantSpec();
      if (!constantAnnotation.category().isEmpty()) {
        return constantAnnotation.category();
      }
      return functionMetadata.getName();
    }
    final XLFunction functionAnnotation = functionMetadata.getFunctionSpec();
    if (functionAnnotation != null && !functionAnnotation.category().isEmpty()) {
      return functionAnnotation.category();
    }
    final XLFunctions functionsAnnotation = functionMetadata.getFunctionsSpec();
    if (functionsAnnotation != null && !functionsAnnotation.category().isEmpty()) {
      return functionsAnnotation.category();
    }
    return functionMetadata.getName();
  }

  private static String buildFunctionSignature(final FunctionDefinition functionDefinition) {
    final FunctionMetadata functionMetadata = functionDefinition.getFunctionMetadata();
    if (!functionMetadata.isConstantSpec()) {
      final StringBuilder signature = new StringBuilder();
      final Class<?> excelReturnType;
      final Class<?>[] parameterTypes;
      final boolean isVarArgs;
      switch (functionDefinition.getJavaTypeForFunction()) {
        case METHOD:
          final MethodInvoker methodInvoker = functionDefinition.getMethodInvoker();
          excelReturnType = methodInvoker.getExcelReturnType();
          parameterTypes = methodInvoker.getExcelParameterTypes();
          isVarArgs = methodInvoker.isVarArgs();
          break;
        case CONSTRUCTOR:
          final ConstructorInvoker constructorInvoker = functionDefinition.getConstructorInvoker();
          excelReturnType = constructorInvoker.getExcelReturnType();
          parameterTypes = constructorInvoker.getExcelParameterTypes();
          isVarArgs = constructorInvoker.isVarArgs();
          break;
        default:
          throw new XL4JRuntimeException("Unhandled type " + functionDefinition.getJavaTypeForFunction());
      }
      final boolean isVolatile, isMTSafe, isMacroEquivalent, isAutoAsynchronous;
      final FunctionType functionType;
      if (functionMetadata.getFunctionSpec() != null) {
        final XLFunction functionAnnotation = functionMetadata.getFunctionSpec();
        isVolatile = functionAnnotation.isVolatile();
        isMTSafe = functionAnnotation.isMultiThreadSafe();
        isMacroEquivalent = functionAnnotation.isMacroEquivalent();
        isAutoAsynchronous = functionAnnotation.isAutoAsynchronous();
        functionType = functionAnnotation.functionType();
      } else if (functionMetadata.getFunctionsSpec() != null) {
        final XLFunctions functionAnnotations = functionMetadata.getFunctionsSpec();
        isVolatile = functionAnnotations.isVolatile();
        isMTSafe = functionAnnotations.isMultiThreadSafe();
        isMacroEquivalent = functionAnnotations.isMacroEquivalent();
        isAutoAsynchronous = functionAnnotations.isAutoAsynchronous();
        functionType = functionAnnotations.functionType();
      } else {
        throw new XL4JRuntimeException("Could not get XLFunction or XLFunctions information");
      }
      if (isVolatile && isMTSafe || isMTSafe && isMacroEquivalent) {
        throw new XL4JRuntimeException(
            "Illegal combination of XLFunction attributes, cannot be volatile & thread-safe or macro-equivalent & thread-safe");
      }
      // Return type character
      if (functionType == FunctionType.COMMAND) {
        if (!XLInteger.class.isAssignableFrom(excelReturnType)) {
          throw new XL4JRuntimeException("Commands must have a return type XLInteger (gets converted to type J (int))");
        }
        signature.append("J"); // means int, but we'll convert from XLInteger to make the class hierarchy cleaner.
      } else {
        if (isAutoAsynchronous) {
          signature.append(">"); // means void function is asynchronous callback handle, which we don't expose to the user.
        } else {
          if (XLLocalReference.class.isAssignableFrom(excelReturnType) || XLMultiReference.class.isAssignableFrom(excelReturnType)) {
            // REVIEW: Not sure if this is a valid thing to do.
            signature.append("U"); // XLOPER12 range/ref/array. I've no idea if this is even valid. Not clear in docs.
          } else {
            signature.append("Q"); // XLOPER12
          }
        }
      }
      // Parameters
      final XLParameter[] parameterAnnotations = functionMetadata.getParameters();
      if (parameterAnnotations.length != 0 && parameterAnnotations.length != parameterTypes.length) {
        throw new XL4JRuntimeException("Function called " + functionMetadata.getName() + " must have an XLParameter annotation for each parameter");
      }
      if (functionDefinition.getJavaTypeForFunction() == CallTarget.METHOD && !functionDefinition.isStatic()) {
        // first argument for a non-static method is the object itself
        signature.append("Q");
      }
      for (int i = 0; i < parameterTypes.length; i++) {
        if (parameterAnnotations.length == 0) { // true if someone has used a class-level @XLFunctions annotation, see FunctionRegistry
          // if they wanted anything other than the default, they wouldn't have used a class-level annotation
          signature.append("Q");
        } else {
          final XLParameter parameterAnnotation = parameterAnnotations[i];
          if (parameterAnnotation != null && parameterAnnotation.referenceType()) {
            if (!isMacroEquivalent) {
              throw new XL4JRuntimeException("Cannot register reference type parameters if not a macro equivalent: "
                  + "function annotation @XLFunction(isMacroEquivalent = true) required");
            }
            signature.append("U"); // XLOPER12 byref
          } else {
            signature.append("Q"); // XLOPER12 byval
          }
        }
      }
      if (isVarArgs) {
        if (parameterTypes.length == 0) {
          throw new IllegalStateException("Internal Error: Variable argument list function should have at least one parameter type");
        }
        // assume the default value is required for XLFunctions annotation where the parameters aren't annotated
        final boolean isLastTypeReferenceType = parameterAnnotations.length == 0 ? false : parameterAnnotations[parameterTypes.length - 1].referenceType();
        for (int i = 0; i < VARARGS_MAX_PARAMS - parameterTypes.length; i++) {
          if (isLastTypeReferenceType) {
            signature.append("U"); // XLOPER12 byref
          } else {
            signature.append("Q"); // XLOPER12 byval
          }
        }
      }
      if (isAutoAsynchronous) {
        signature.append("X"); // should we allow the other options (below)?
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
    // create signature for fields (constants)
    final StringBuilder signature = new StringBuilder();
    final FieldGetter fieldInvoker = functionDefinition.getFieldInvoker();
    final Class<?> excelReturnType = fieldInvoker.getExcelReturnType();
    if (functionDefinition.isStatic()) {
      // first argument for a non-static field is the object itself
      signature.append("Q");
    }
    // TODO check the whether or not volatile should be used in some cases
    // Return type character
    if (XLLocalReference.class.isAssignableFrom(excelReturnType) || XLMultiReference.class.isAssignableFrom(excelReturnType)) {
      // REVIEW: Not sure if this is a valid thing to do.
      signature.append("U"); // XLOPER12 range/ref/array. I've no idea if this is even valid. Not clear in docs.
    } else {
      signature.append("Q"); // XLOPER12
    }
    signature.append("$");
    return signature.toString();
  }

  /**
   * Build the string containing a list of argument annotations.
   *
   * @param definition
   *          a function definition containing an array of argument annotations, can contain nulls
   * @return the argument annotations separated by a comma
   */
  private static String buildArgNames(final FunctionDefinition definition) {
    final XLParameter[] parameterAnnotations = definition.getFunctionMetadata().getParameters();
    final StringBuilder parameterNames = new StringBuilder();
    int argCounter = 1;
    if (definition.getJavaTypeForFunction() == CallTarget.METHOD && !definition.isStatic()) {
      // add class name to start of list
      parameterNames.append(definition.getMethodInvoker().getMethodDeclaringClass().getSimpleName());
      if (parameterAnnotations.length > 0) {
        parameterNames.append(",");
      }
      argCounter++;
    }
    for (int i = 0; i < parameterAnnotations.length; i++) {
      final XLParameter parameterAnnotation = parameterAnnotations[i];
      if (parameterAnnotation != null) {
        if (!parameterAnnotation.name().isEmpty()) {
          parameterNames.append(parameterAnnotation.name());
        } else {
          // TODO: try paranamer/JavaDocs? #46
          parameterNames.append(Integer.toString(argCounter));
        }
      } else {
        // TODO: try paranamer/JavaDocs? #46
        parameterNames.append(Integer.toString(argCounter));
      }
      if (i < parameterAnnotations.length - 1) {
        parameterNames.append(",");
      }
      argCounter++;
    }
    return parameterNames.toString();
  }

}