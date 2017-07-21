/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.core;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.v1.api.core.ExcelCallback;
import com.mcleodmoores.xl4j.v1.api.core.FunctionDefinition;
import com.mcleodmoores.xl4j.v1.api.core.InvokerFactory;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 *
 */
public class ResourceBundleFunctionRegistry extends AbstractFunctionRegistry {
  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceBundleFunctionRegistry.class);
  private final Set<FunctionDefinition> _functionDefinitions =
      Collections.synchronizedSet(new TreeSet<>(new Comparator<FunctionDefinition>() {

        @Override
        public int compare(final FunctionDefinition arg0, final FunctionDefinition arg1) {
          return arg1.getFunctionMetadata().getName().compareTo(arg0.getFunctionMetadata().getName());
        }
      }));
  private final AtomicInteger _exportCounter = new AtomicInteger();
  private final ConcurrentMap<Integer, FunctionDefinition> _functionDefinitionLookup = new ConcurrentHashMap<>();

  @Override
  public void registerFunctions(final ExcelCallback callback) {

  }

  @Override
  protected void createAndRegisterFunctions(final InvokerFactory invokerFactory) {

  }

  @Override
  protected int allocateExport() {
    final int exportNumber = _exportCounter.getAndIncrement();
    return exportNumber;
  }

  @Override
  public FunctionDefinition getFunctionDefinition(final int exportNumber) {
    final FunctionDefinition functionDefinition = _functionDefinitionLookup.get(exportNumber);
    if (functionDefinition != null) {
      return functionDefinition;
    }
    throw new XL4JRuntimeException("Cannot find function definition with export number " + exportNumber);
  }

  @Override
  public Set<FunctionDefinition> getFunctionDefinitions() {
    return new LinkedHashSet<>(_functionDefinitionLookup.values());
  }
}
