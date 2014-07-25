/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import java.io.File;

import com.mcleodmoores.excel4j.javacode.MethodInvoker;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;

/**
 * Provides a layer to process function metadata into relatively raw calls back to Excel.
 */
public class ExcelCallbackAdapter implements ExcelCallback {
  private File _dllPath;
  private RawExcelCallback _rawCallback;

  /**
   * Create a callback adapter.
   * @param dllPath  the path of the DLL implementing this XLL
   * @param rawCallback  the raw callback interface to call through to
   */
  public ExcelCallbackAdapter(final File dllPath, final RawExcelCallback rawCallback) {
    _dllPath = dllPath;
    _rawCallback = rawCallback;
  }
  
  @Override
  public void registerFunction(final FunctionDefinition functionDefinition) {
    FunctionMetadata functionMetadata = functionDefinition.getFunctionMetadata();
    MethodInvoker methodInvoker = functionDefinition.getMethodInvoker();
    XLNamespace namespaceAnnotation = functionMetadata.getNamespace();
    XLFunction functionAnnotation = functionMetadata.getFunctionSpec();
    XLArgument[] argumentAnnotations = functionMetadata.getArguments();
    
    final String functionName = buildFunctionName(methodInvoker, namespaceAnnotation, functionAnnotation);
    final String argumentNames = buildArgNames(argumentAnnotations);
    final int functionType = getFunctionType(functionAnnotation);
  }
  
  private String buildFunctionSignature(XLFunction functionAnnotation, MethodInvoker methodInvoker) {
    StringBuilder signature = new StringBuilder();
    boolean isVolatile = (functionAnnotation != null) ? functionAnnotation.isVolatile() : false; // default
    boolean isMTSafe = (functionAnnotation != null) ? functionAnnotation.isMultiThreadSafe() : true; // default, this is the 2010s, yo.
    boolean isMacroEquivalent = (functionAnnotation != null) ? functionAnnotation.isMacroEquivalent() : false; // default
    boolean isAsynchronous = (functionAnnotation != null) ? functionAnnotation.isAsynchronous() : false; // default
    XLFunctionType functionType = (functionAnnotation != null) ? functionAnnotation.functionType() : XLFunctionType.FUNCTION; // default;
    if ((isVolatile && isMTSafe) || (isMTSafe && isMacroEquivalent)) {
      throw new Excel4JRuntimeException("Illegal combination of XLFunction attributes, cannot be volatile & thread-safe or macro-equivalent & thread-safe");
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
      if (functionAnnotation.name() != null) {
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
        if (argumentAnnotation.name() != null) {
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
      return XLFunctionType.FUNCTION.getExcelValue();
    }
  }

}
