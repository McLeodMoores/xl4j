/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.excel4j.javacode.ConstructorInvoker;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLError;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * The default Excel call handler for constructors.
 */
public class DefaultExcelConstructorCallHandler implements ExcelConstructorCallHandler {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExcelConstructorCallHandler.class);
  /** The registry */
  private final FunctionRegistry _registry;

  /**
   * Create a default call handler.
   * @param registry  the function and constructor registry, not null
   */
  public DefaultExcelConstructorCallHandler(final FunctionRegistry registry) {
    ArgumentChecker.notNull(registry, "registry");
    _registry = registry;
  }

  @Override
  public XLValue newInstance(final int exportNumber, final XLValue... args) {
    LOGGER.info("newInstance called with {}", exportNumber);
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
    final ConstructorDefinition constructorDefinition = _registry.getConstructorDefinition(exportNumber);
    LOGGER.info("constructorDefinition = {}", constructorDefinition.getConstructorMetadata().getConstructorSpec().name());
    final ConstructorInvoker constructorInvoker = constructorDefinition.getConstructorInvoker();
    LOGGER.info("constructor invoker = {}", constructorInvoker.getDeclaringClass().getSimpleName());
    try {
      return constructorInvoker.newInstance(args);
    } catch (final Exception e) {
      e.printStackTrace();
      LOGGER.info("Exception occurred while instantiating class, returning XLError: {}", e.getMessage());
      return XLError.Null;
    }
  }
}
