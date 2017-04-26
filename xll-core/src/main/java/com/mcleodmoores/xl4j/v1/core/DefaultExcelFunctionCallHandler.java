/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.v1.api.core.ConstructorInvoker;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFunctionCallHandler;
import com.mcleodmoores.xl4j.v1.api.core.FieldGetter;
import com.mcleodmoores.xl4j.v1.api.core.FunctionDefinition;
import com.mcleodmoores.xl4j.v1.api.core.FunctionRegistry;
import com.mcleodmoores.xl4j.v1.api.core.Heap;
import com.mcleodmoores.xl4j.v1.api.core.MethodInvoker;
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * The default Excel call handler for functions.
 */
public class DefaultExcelFunctionCallHandler implements ExcelFunctionCallHandler {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExcelFunctionCallHandler.class);
  /** The registry */
  private final FunctionRegistry _functionRegistry;
  /** The heap */
  private final Heap _heap;

  /**
   * Create a default call handler.
   *
   * @param functionRegistry
   *          the function registry, not null
   * @param heap
   *          the heap, not null
   */
  public DefaultExcelFunctionCallHandler(final FunctionRegistry functionRegistry, final Heap heap) {
    _functionRegistry = ArgumentChecker.notNull(functionRegistry, "functionRegistry");
    _heap = ArgumentChecker.notNull(heap, "heap");
  }

  @Override
  public XLValue invoke(final int exportNumber, final XLValue... args) {
    ArgumentChecker.notNull(args, "args");
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
    try {
      final FunctionDefinition functionDefinition = _functionRegistry.getFunctionDefinition(exportNumber);
      if (functionDefinition == null) {
        LOGGER.error("Could not get function definition with export number {}", exportNumber);
        return XLError.Null;
      }
      LOGGER.info("functionDefinition = {}", functionDefinition.getFunctionMetadata().getName());
      switch (functionDefinition.getCallTargetForFunction()) {
        case METHOD: {
          final MethodInvoker methodInvoker = functionDefinition.getMethodInvoker();
          LOGGER.info("method invoker = {}", methodInvoker.getMethodName());
          if (methodInvoker.isStatic()) {
            return methodInvoker.invoke(null, args);
          }
          final Object obj;
          final XLObject object = (XLObject) args[0];
          obj = _heap.getObject(object.getHandle());
          if (obj == null) {
            LOGGER.error("Object handle was invalid, returning XLError.Ref");
            return XLError.Ref;
          }
          final XLValue[] newArgs = new XLValue[args.length - 1];
          System.arraycopy(args, 1, newArgs, 0, args.length - 1);
          final XLValue retVal = methodInvoker.invoke(obj, newArgs);
          LOGGER.trace("Return value from Java to C++ layer is {}", retVal);
          return retVal;
        }
        case CONSTRUCTOR: {
          final ConstructorInvoker constructorInvoker = functionDefinition.getConstructorInvoker();
          LOGGER.info("constructor invoker = {}", constructorInvoker.getDeclaringClass().getSimpleName());
          return constructorInvoker.newInstance(args);
        }
        case FIELD: {
          final FieldGetter fieldInvoker = functionDefinition.getFieldInvoker();
          LOGGER.info("field invoker = {}", fieldInvoker.getFieldDeclaringClass());
          if (fieldInvoker.isStatic()) {
            return fieldInvoker.get(null);
          }
          final XLObject object = (XLObject) args[0];
          final Object obj = _heap.getObject(object.getHandle());
          if (obj == null) {
            LOGGER.error("Object handle was invalid, returning XLError.Ref");
            return XLError.Ref;
          }
          return fieldInvoker.get(obj);
        }
        default:
          throw new XL4JRuntimeException("Unhandled type " + functionDefinition.getCallTargetForFunction());
      }
    } catch (final Exception e) {
      LOGGER.info("Exception occurred while invoking method, returning XLError", e);
      return XLError.Null;
    }
  }
}
