/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.heap.Heap;
import com.mcleodmoores.xl4j.javacode.ConstructorInvoker;
import com.mcleodmoores.xl4j.javacode.FieldInvoker;
import com.mcleodmoores.xl4j.javacode.MethodInvoker;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLError;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

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
    ArgumentChecker.notNull(functionRegistry, "functionRegistry");
    ArgumentChecker.notNull(heap, "heap");
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
    try {
      final FunctionDefinition functionDefinition = _functionRegistry.getFunctionDefinition(exportNumber);
      switch (functionDefinition.getJavaTypeForFunction()) {
        case METHOD: {
          LOGGER.info("functionDefinition = {}", functionDefinition.getFunctionMetadata().getFunctionSpec().name());
          final MethodInvoker methodInvoker = functionDefinition.getMethodInvoker();
          LOGGER.info("method invoker = {}", methodInvoker.getMethodName());
          if (methodInvoker.isStatic()) {
            return methodInvoker.invoke(null, args);
          }
          final XLObject object = (XLObject) args[0];
          final Object obj = _heap.getObject(object.getHandle());
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
          final FieldInvoker fieldInvoker = functionDefinition.getFieldInvoker();
          LOGGER.info("field invoker = {}", fieldInvoker.getFieldDeclaringClass());
          if (fieldInvoker.isStatic()) {
            return fieldInvoker.invoke(null);
          }
          final XLObject object = (XLObject) args[0];
          final Object obj = _heap.getObject(object.getHandle());
          if (obj == null) {
            LOGGER.error("Object handle was invalid, returning XLError.Ref");
            return XLError.Ref;
          }
          return fieldInvoker.invoke(obj);
        }
        default:
          throw new Excel4JRuntimeException("Unhandled type " + functionDefinition.getJavaTypeForFunction());
      }
    } catch (final Exception e) {
      LOGGER.info("Exception occurred while invoking method, returning XLError", e);
      return XLError.Null;
    }
  }
}
