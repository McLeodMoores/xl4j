/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.callback;

import java.io.File;

import com.mcleodmoores.excel4j.FunctionDefinition;
import com.mcleodmoores.excel4j.FunctionMetadata;
import com.mcleodmoores.excel4j.FunctionType;
import com.mcleodmoores.excel4j.XLArgument;
import com.mcleodmoores.excel4j.XLFunction;
import com.mcleodmoores.excel4j.XLNamespace;
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
  private File _dllPath;
  private LowLevelExcelCallback _rawCallback;

  /**
   * Create a callback adapter.
   * @param dllPath  the path of the DLL implementing this XLL
   * @param rawCallback  the raw callback interface to call through to
   */
  public DefaultExcelCallback(final File dllPath, final LowLevelExcelCallback rawCallback) {
    _dllPath = dllPath;
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
    FunctionMetadata functionMetadata = functionDefinition.getFunctionMetadata();
    MethodInvoker methodInvoker = functionDefinition.getMethodInvoker();
    XLNamespace namespaceAnnotation = functionMetadata.getNamespace();
    XLFunction functionAnnotation = functionMetadata.getFunctionSpec();
    XLArgument[] argumentAnnotations = functionMetadata.getArguments();
    final String dllPath = _dllPath.getPath();
    final String exportName = functionDefinition.getExportName();
    final String functionName = buildFunctionName(methodInvoker, namespaceAnnotation, functionAnnotation);
    final String argumentNames = buildArgNames(argumentAnnotations);
    final Integer functionTypeInt = getFunctionType(functionAnnotation);
    final String signature = buildFunctionSignature(functionAnnotation, argumentAnnotations, methodInvoker);
    final String functionCategory = buildFunctionCategory(functionAnnotation, methodInvoker);
    final String helpTopic = buildHelpTopic(functionAnnotation);
    final String description = buildDescription(functionAnnotation);
    final String[] argsHelp = buildArgsHelp(argumentAnnotations);
    _rawCallback.xlfRegister(dllPath, exportName, signature, functionName, argumentNames, 
                             functionTypeInt, functionCategory, "", helpTopic, description, argsHelp);
  }
  
  private String[] buildArgsHelp(final XLArgument[] argumentAnnotations) {
    String[] results = new String[argumentAnnotations.length];
    for (int i = 0; i < argumentAnnotations.length; i++) {
      if (argumentAnnotations[i] != null && !argumentAnnotations[i].description().isEmpty()) {
        results[i] = argumentAnnotations[i].description();
      } else {
        results[i] = null;
      }
    }
    return results;
  }
  
  private String buildDescription(final XLFunction functionAnnotation) {
    if (functionAnnotation != null && !functionAnnotation.description().isEmpty()) {
      return functionAnnotation.description();
    } else {
      return null;
    }
  }
  
  private String buildHelpTopic(final XLFunction functionAnnotation) {
    if (functionAnnotation != null && !functionAnnotation.helpTopic().isEmpty()) {
      return functionAnnotation.helpTopic();
    } else {
      return null;
    }
  }
  private String buildFunctionCategory(final XLFunction functionAnnotation, final MethodInvoker methodInvoker) {
    if (functionAnnotation != null && !functionAnnotation.category().isEmpty()) {
      return functionAnnotation.category();
    } else {
      return methodInvoker.getMethodDeclaringClass().getSimpleName();
    }    
  }
  
  private String buildFunctionSignature(final XLFunction functionAnnotation, final XLArgument[] argumentAnnotations, final MethodInvoker methodInvoker) {
    StringBuilder signature = new StringBuilder();
    Class<?> excelReturnType = methodInvoker.getExcelReturnType();
    Class<?>[] parameterTypes = methodInvoker.getExcelParameterTypes();
    boolean isVolatile = (functionAnnotation != null) ? functionAnnotation.isVolatile() : false; // default
    boolean isMTSafe = (functionAnnotation != null) ? functionAnnotation.isMultiThreadSafe() : true; // default, this is the 2010s, yo.
    boolean isMacroEquivalent = (functionAnnotation != null) ? functionAnnotation.isMacroEquivalent() : false; // default
    boolean isAsynchronous = (functionAnnotation != null) ? functionAnnotation.isAsynchronous() : false; // default
    FunctionType functionType = (functionAnnotation != null) ? functionAnnotation.functionType() : FunctionType.FUNCTION; // default;
    if ((isVolatile && isMTSafe) || (isMTSafe && isMacroEquivalent)) {
      throw new Excel4JRuntimeException("Illegal combination of XLFunction attributes, cannot be volatile & thread-safe or macro-equivalent & thread-safe");
    }
    // Return type character
    if (functionType == FunctionType.COMMAND) {
      if (!excelReturnType.isAssignableFrom(XLInteger.class)) {
        throw new Excel4JRuntimeException("Commands must have a return type XLInteger (gets convertered to type J (int))");
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
      XLArgument argumentAnnotation = argumentAnnotations[i];
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

  /**
   * Build the function name string using the namespace if specified.
   * @param methodInvoker  the method invoker for this function, not null
   * @param namespaceAnnotation  the namespace annotation if there is one, or null if there isn't.
   * @param functionAnnotation  the function annoation is there is one, or null if there isn't.
   * @return the name of the function to register with Excel
   */
  private String buildFunctionName(final MethodInvoker methodInvoker, final XLNamespace namespaceAnnotation, final XLFunction functionAnnotation) {
    StringBuilder functionName = new StringBuilder();
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

  /**
   * Build the string containing a list of argument annotations.
   * @param argumentAnnotations  array of argument annotations, can contain nulls
   */
  private String buildArgNames(final XLArgument[] argumentAnnotations) {
    StringBuilder argumentNames = new StringBuilder();
    int argCounter = 1;
    
    for (int i = 0; i < argumentAnnotations.length; i++) {
      XLArgument argumentAnnotation = argumentAnnotations[i];
      if (argumentAnnotation != null) {
        if (!argumentAnnotation.name().isEmpty()) {
          argumentNames.append(argumentAnnotation.name());
        } else {
          // TODO: try paranamer/JavaDocs?
          argumentNames.append(Integer.toString(argCounter));
        }
      } else {
        // TODO: try paranamer/JavaDocs?
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
  private int getFunctionType(final XLFunction functionAnnotation) {
    if (functionAnnotation != null) {
      return functionAnnotation.functionType().getExcelValue();
    } else {
      return FunctionType.FUNCTION.getExcelValue();
    }
  }

}
