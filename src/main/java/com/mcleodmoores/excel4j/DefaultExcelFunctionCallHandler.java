/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.excel4j.heap.Heap;
import com.mcleodmoores.excel4j.javacode.MethodInvoker;
import com.mcleodmoores.excel4j.values.XLError;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * The default Excel call handler.
 */
public class DefaultExcelFunctionCallHandler implements ExcelFunctionCallHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExcelFunctionCallHandler.class);
  private final FunctionRegistry _functionRegistry;
  private final Heap _heap;

  /**
   * Create a default call handler.
   * @param functionRegistry  the function registry
   * @param heap  the heap
   */
  public DefaultExcelFunctionCallHandler(final FunctionRegistry functionRegistry, final Heap heap) {
    _functionRegistry = functionRegistry;
    _heap = heap;
  }

  @Override
  public XLValue invoke(final int exportNumber, final XLValue... args) {
    LOGGER.info("invoke called with {}", exportNumber);
    for (int i = 0; i < args.length; i++) {
      LOGGER.info("arg = {}", args[i]);
      if (args[i] instanceof XLString) {
        final XLString xlString = (XLString) args[i];
        if (xlString.isXLObject()) {
          args[i] = xlString.toXLObject();
          LOGGER.info("converted arg to XLObject");
        }
      }
    }
    final FunctionDefinition functionDefinition = _functionRegistry.getFunctionDefinition(exportNumber);
    LOGGER.info("functionDefinition = {}", functionDefinition.getFunctionMetadata().getFunctionSpec().name());
    final MethodInvoker methodInvoker = functionDefinition.getMethodInvoker();
    LOGGER.info("method invoker = {}", methodInvoker.getMethodName());
    try {
      if (methodInvoker.isStatic()) {
        return methodInvoker.invoke(null, args);
      }
      final XLObject object = (XLObject) args[0];
      final Object obj = _heap.getObject(object.getHandle());
      final XLValue[] newArgs = new XLValue[args.length - 1];
      System.arraycopy(args, 1, newArgs, 0, args.length - 1);
      return methodInvoker.invoke(obj, newArgs);
    } catch (final Exception e) {
      LOGGER.info("Exception occurred while invoking method, returning XLError: {}", e.getMessage());
      return XLError.Null;
    }
  }
}
