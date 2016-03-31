/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import com.mcleodmoores.excel4j.javacode.MethodInvoker;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.util.ExportUtils;

/**
 * Class to store meta-data and type conversion information about excel functions.
 */
public final class FunctionDefinition {
  /** Meta data about the function */
  private final FunctionMetadata _functionMetadata;
  /** The method invoker */
  private final MethodInvoker _methodInvoker;
  /** Function export number in registry */
  private final int _exportNumber;

  /**
   * Creates an instance.
   * @param functionMetadata  the meta-data
   * @param methodInvoker  the method invoker
   * @param exportNumber  the export number
   */
  private FunctionDefinition(final FunctionMetadata functionMetadata, final MethodInvoker methodInvoker, final int exportNumber) {
    _functionMetadata = functionMetadata;
    _methodInvoker = methodInvoker;
    _exportNumber = exportNumber;
  }

  /**
   * Static factory method to create an instance.
   * @param functionMetadata  the annotation-based meta-data about the function, not null
   * @param methodInvoker  the type conversion and method invocation binding for this function, not null
   * @param exportNumber  the number of the DLL export that handles this function (only unique to the number of parameters used)
   * @return an instance of a FunctionDefinition
   */
  public static FunctionDefinition of(final FunctionMetadata functionMetadata, final MethodInvoker methodInvoker, final int exportNumber) {
    ArgumentChecker.notNull(functionMetadata, "functionMetadata");
    ArgumentChecker.notNull(methodInvoker, "methodInvoker");
    return new FunctionDefinition(functionMetadata, methodInvoker, exportNumber);
  }

  /**
   * @return the function meta-data, not null
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
    return ExportUtils.buildExportName(_exportNumber);
  }

  /**
   * @return the export number
   */
  public int getExportNumber() {
    return _exportNumber;
  }
}
