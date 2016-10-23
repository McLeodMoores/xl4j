/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import com.mcleodmoores.xl4j.callback.ExcelCallback;
import com.mcleodmoores.xl4j.heap.Heap;
import com.mcleodmoores.xl4j.javacode.InvokerFactory;
import com.mcleodmoores.xl4j.lowlevel.LowLevelExcelCallback;
import com.mcleodmoores.xl4j.typeconvert.TypeConverterRegistry;

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
   * @return the constructor call handler for dispatching from Excel.
   */
  ExcelConstructorCallHandler getExcelConstructorCallHandler();

  /**
   * @return the type converter registry
   */
  TypeConverterRegistry getTypeConverterRegistry();

  /**
   * @return the low-level callback
   */
  LowLevelExcelCallback getLowLevelExcelCallback();
}
