/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import com.mcleodmoores.excel4j.javacode.MethodInvoker;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.util.ExportUtils;

/**
 * Class to store meta data and type conversion information about excel functions.
 */
public final class FunctionDefinition {
  private FunctionMetadata _functionMetadata;
  private MethodInvoker _methodInvoker;
  private int _exportNumber;
  private int _exportParams;

  private FunctionDefinition(final FunctionMetadata functionMetadata, final MethodInvoker methodInvoker, final int exportNumber) {
    _functionMetadata = functionMetadata;
    _methodInvoker = methodInvoker;
    int exportParams = methodInvoker.getExcelParameterTypes().length;
    if (functionMetadata.getFunctionSpec() != null) {
      if (functionMetadata.getFunctionSpec().isAsynchronous()) {
        exportParams++; // account for async callback handle that's hidden from Java. 
      }
    }
    _exportParams = exportParams;
  }
  
  /**
   * Static factory method to create an instance.
   * @param functionMetadata  the annotation-based metadata about the function
   * @param methodInvoker  the type conversion and method invocation binding for this function
   * @param exportNumber  the number of the DLL export that handles this function (only unique to the number of parameters used)
   * @return an instance of a FunctionDefinition
   */
  public static FunctionDefinition of(final FunctionMetadata functionMetadata, final MethodInvoker methodInvoker, final int exportNumber) {
    ArgumentChecker.notNull(functionMetadata, "functionMetadata");
    ArgumentChecker.notNull(methodInvoker, "methodInvoker");
    ArgumentChecker.notNull(exportNumber, "exportNumber");
    return new FunctionDefinition(functionMetadata, methodInvoker, exportNumber);
  }

  /**
   * @return the function metadata, not null
   */
  public FunctionMetadata getFunctionMetadata() {
    return _functionMetadata;
  }

  /**
   * @return the method invoker, not null
   */
  public MethodInvoker getMethodInvoker() {
    return _methodInvoker;
  }
  
  /**
   * @return the export name, not null
   */
  public String getExportName() {
    return ExportUtils.buildExportName(_exportParams, _exportNumber);
  }
}
