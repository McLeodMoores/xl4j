/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.callback;

import com.mcleodmoores.xl4j.FunctionDefinition;
import com.mcleodmoores.xl4j.FunctionMetadata;
import com.mcleodmoores.xl4j.FunctionType;
import com.mcleodmoores.xl4j.XLArgument;
import com.mcleodmoores.xl4j.XLConstant;
import com.mcleodmoores.xl4j.XLFunction;
import com.mcleodmoores.xl4j.javacode.ConstructorInvoker;
import com.mcleodmoores.xl4j.javacode.FieldInvoker;
import com.mcleodmoores.xl4j.javacode.MethodInvoker;
import com.mcleodmoores.xl4j.lowlevel.LowLevelExcelCallback;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLInteger;
import com.mcleodmoores.xl4j.values.XLLocalReference;
import com.mcleodmoores.xl4j.values.XLMultiReference;

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
    final String functionName = functionDefinition.getFunctionName();
    final String exportName = functionDefinition.getExportName();
    final String signature = buildFunctionSignature(functionDefinition);
    final String functionCategory = buildFunctionCategory(functionDefinition);
    if (functionMetadata.isFunctionSpec()) {
      final XLFunction functionAnnotation = functionMetadata.getFunctionSpec();
      final XLArgument[] argumentAnnotations = functionMetadata.getArguments();
      final boolean isVarArgs = functionDefinition.isVarArgs();
      final boolean isLongRunning = functionAnnotation.isLongRunning();
      final boolean isAutoAsynchronous = functionAnnotation.isAutoAsynchronous();
      final boolean isManualAsynchronous = functionAnnotation.isManualAsynchronous();
      final boolean isCallerRequired = functionAnnotation.isCallerRequired();
      final String argumentNames = buildArgNames(argumentAnnotations);
      final Integer functionTypeInt = getFunctionType(functionAnnotation);
      final String helpTopic = buildHelpTopic(functionAnnotation);
      final String description = buildDescription(functionAnnotation);
      final String[] argsHelp = buildArgsHelp(argumentAnnotations);
      _rawCallback.xlfRegister(functionDefinition.getExportNumber(), exportName, isVarArgs, isLongRunning,
          isAutoAsynchronous, isManualAsynchronous, isCallerRequired, signature, functionName, argumentNames,
          functionTypeInt, functionCategory, "", helpTopic, description, argsHelp);
    } else {
      final XLConstant constantAnnotation = functionMetadata.getConstantSpec();
      final String helpTopic = buildHelpTopic(constantAnnotation);
      final String description = buildDescription(constantAnnotation);
      final String[] argsHelp = new String[0];
      _rawCallback.xlfRegister(functionDefinition.getExportNumber(), exportName, false, false,
          false, false, false, signature, functionName, "", FunctionType.FUNCTION.getExcelValue(),
          functionCategory, "", helpTopic, description, argsHelp);
    }
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

  private static String buildDescription(final XLConstant constantAnnotation) {
    if (constantAnnotation != null && !constantAnnotation.description().isEmpty()) {
      return constantAnnotation.description();
    }
    return null;
  }

  private static String buildHelpTopic(final XLFunction functionAnnotation) {
    if (functionAnnotation != null && !functionAnnotation.helpTopic().isEmpty()) {
      return functionAnnotation.helpTopic();
    }
    return null;
  }

  private static String buildHelpTopic(final XLConstant constantAnnotation) {
    if (constantAnnotation != null && !constantAnnotation.helpTopic().isEmpty()) {
      return constantAnnotation.helpTopic();
    }
    return null;
  }

  private static String buildFunctionCategory(final FunctionDefinition functionDefinition) {
    if (functionDefinition.getFunctionMetadata().isFunctionSpec()) {
      final XLFunction functionAnnotation = functionDefinition.getFunctionMetadata().getFunctionSpec();
      if (functionAnnotation != null && !functionAnnotation.category().isEmpty()) {
        return functionAnnotation.category();
      }
      return functionDefinition.getFunctionName();
    }
    final XLConstant constantAnnotation = functionDefinition.getFunctionMetadata().getConstantSpec();
    if (constantAnnotation != null && !constantAnnotation.category().isEmpty()) {
      return constantAnnotation.category();
    }
    return functionDefinition.getFunctionName();
  }

  private static String buildFunctionSignature(final FunctionDefinition functionDefinition) {
    final FunctionMetadata functionMetadata = functionDefinition.getFunctionMetadata();
    if (functionMetadata.isFunctionSpec()) {
      final XLFunction functionAnnotation = functionMetadata.getFunctionSpec();
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
          throw new Excel4JRuntimeException("Unhandled type " + functionDefinition.getJavaTypeForFunction());
      }
      final boolean isVolatile = functionAnnotation != null ? functionAnnotation.isVolatile() : false; // default
      final boolean isMTSafe = functionAnnotation != null ? functionAnnotation.isMultiThreadSafe() : true; // default, this is the 2010s, yo.
      final boolean isMacroEquivalent = functionAnnotation != null ? functionAnnotation.isMacroEquivalent() : false; // default
      final boolean isAutoAsynchronous = functionAnnotation != null ? functionAnnotation.isAutoAsynchronous() : false; // default
      final FunctionType functionType = functionAnnotation != null ? functionAnnotation.functionType() : FunctionType.FUNCTION; // default;
      if (isVolatile && isMTSafe || isMTSafe && isMacroEquivalent) {
        throw new Excel4JRuntimeException(
            "Illegal combination of XLFunction attributes, cannot be volatile & thread-safe or macro-equivalent & thread-safe");
      }
      // Return type character
      if (functionType == FunctionType.COMMAND) {
        if (!excelReturnType.isAssignableFrom(XLInteger.class)) {
          throw new Excel4JRuntimeException("Commands must have a return type XLInteger (gets converted to type J (int))");
        }
        signature.append("J"); // means int, but we'll convert from XLInteger to make the class hierarchy cleaner.
      } else {
        if (isAutoAsynchronous) {
          signature.append(">"); // means void function is asynchronous callback handle, which we don't expose to the user.
        } else {
          if (excelReturnType.isAssignableFrom(XLLocalReference.class) || excelReturnType.isAssignableFrom(XLMultiReference.class)) {
            // REVIEW: Not sure if this is a valid thing to do.
            signature.append("U"); // XLOPER12 range/ref/array. I've no idea if this is even valid. Not clear in docs.
          } else {
            signature.append("Q"); // XLOPER12
          }
        }
      }
      // Parameters
      final XLArgument[] argumentAnnotations = functionMetadata.getArguments();
      for (int i = 0; i < parameterTypes.length; i++) {
        if (argumentAnnotations.length == 0) { // true if someone has used a class-level @XLFunction annotation, see FunctionRegistry
          // if they wanted anything other than the default, they wouldn't have used a class-level annotation
          signature.append("Q");
        } else {
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
      }
      if (isVarArgs) {
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
    final FieldInvoker fieldInvoker = functionDefinition.getFieldInvoker();
    final Class<?> excelReturnType = fieldInvoker.getExcelReturnType();
    // TODO check the whether or not volatile should be used in some cases
    // Return type character
    if (excelReturnType.isAssignableFrom(XLLocalReference.class) || excelReturnType.isAssignableFrom(XLMultiReference.class)) {
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
   * @param argumentAnnotations
   *          array of argument annotations, can contain nulls
   * @return the argument annotations separated by a comma
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
   *
   * @param functionAnnotation
   *          the function annotation if there is one, null otherwise
   * @return the type, defaults to 1 (FUNCTION)
   */
  private static int getFunctionType(final XLFunction functionAnnotation) {
    if (functionAnnotation != null) {
      return functionAnnotation.functionType().getExcelValue();
    }
    return FunctionType.FUNCTION.getExcelValue();
  }

}