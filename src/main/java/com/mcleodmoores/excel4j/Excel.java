package com.mcleodmoores.excel4j;

import com.mcleodmoores.excel4j.callback.ExcelCallback;
import com.mcleodmoores.excel4j.heap.WorksheetHeap;
import com.mcleodmoores.excel4j.javacode.InvokerFactory;

/**
 * Interface to provide access to Excel services and callbacks.
 */
public interface Excel {
  /**
   * Get the local invoker factory to allow Excel types to be bound to java constructors, methods and fields.
   * @return an instance of an invoker factory
   */
  InvokerFactory getInvokerFactory();
  /**
   * @return the heap for storing sheet hosted objects
   */
  WorksheetHeap getWorksheetHeap();
  /**
   * @return the function registry
   */
  FunctionRegistry getFunctionRegistry();
  /**
   * @return the ExcelCallback interface
   */
  ExcelCallback getExcelCallback();
  /**
   * REVIEW: this should be somewhere else.
   * @return the ExcelCallHandler for dispatching from Excel.
   */
  ExcelFunctionCallHandler getExcelCallHandler();
}
