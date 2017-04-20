/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.core;

import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.xl4j.v1.xll.LowLevelExcelCallback;

/**
 * Interface to provide access to Excel services and callbacks.
 */
public interface Excel {

  /**
   * Get the local invoker factory to allow Excel types to be bound to java constructors, methods and fields.
   *
   * @return an instance of an invoker factory
   */
  InvokerFactory getInvokerFactory();

  /**
   * @return the heap for storing sheet hosted objects
   */
  Heap getHeap();

  /**
   * @return the function registry
   */
  FunctionRegistry getFunctionRegistry();

  /**
   * @return the ExcelCallback interface
   */
  ExcelCallback getExcelCallback();

  /**
   * @return the function call handler for dispatching from Excel.
   */
  // TODO REVIEW: this should be somewhere else.
  ExcelFunctionCallHandler getExcelCallHandler();

  /**
   * @return the type converter registry
   */
  TypeConverterRegistry getTypeConverterRegistry();

  /**
   * @return the low-level callback
   */
  LowLevelExcelCallback getLowLevelExcelCallback();
}
