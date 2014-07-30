/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import com.mcleodmoores.excel4j.heap.WorksheetHeap;
import com.mcleodmoores.excel4j.javacode.MethodInvoker;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * The default Excel call handler.
 */
public class DefaultExcelCallHandler implements ExcelCallHandler {
  private FunctionRegistry _functionRegistry;
  private WorksheetHeap _heap;
  
  /**
   * Create a default call handler.
   * @param functionRegistry  the function registry
   * @param heap  the heap
   */
  public DefaultExcelCallHandler(final FunctionRegistry functionRegistry, final WorksheetHeap heap) {
    _functionRegistry = functionRegistry;
    _heap = heap;
  }
  
  @Override
  public XLValue invoke(final int exportNumber, final XLValue... args) {
    final int numParams = args.length; 
    final FunctionDefinition functionDefinition = _functionRegistry.getFunctionDefinition(numParams, exportNumber);
    final MethodInvoker methodInvoker = functionDefinition.getMethodInvoker();
    if (methodInvoker.isStatic()) {
      return methodInvoker.invoke(null, args);  
    } else {
      XLObject object = (XLObject) args[0];
      final Object obj = _heap.getObject(object.getHandle());
      XLValue[] newArgs = new XLValue[args.length - 1];
      System.arraycopy(args, 1, newArgs, 0, args.length - 1);
      return methodInvoker.invoke(object, newArgs);
    }
  }
}
