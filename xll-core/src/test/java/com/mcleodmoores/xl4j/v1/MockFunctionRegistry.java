/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.mcleodmoores.xl4j.v1.api.core.ExcelCallback;
import com.mcleodmoores.xl4j.v1.api.core.FunctionDefinition;
import com.mcleodmoores.xl4j.v1.api.core.InvokerFactory;
import com.mcleodmoores.xl4j.v1.core.AbstractFunctionRegistry;

/**
 *
 */
public final class MockFunctionRegistry extends AbstractFunctionRegistry {

  public static Builder builder() {
    return new Builder();
  }

  public final static class Builder {
    private final Collection<Class<?>> _xlConstantClasses = new HashSet<>();
    private final Collection<Class<?>> _xlFunctionsClasses = new HashSet<>();
    private final Collection<Field> _xlConstantFields = new HashSet<>();
    private final Collection<Method> _xlFunctionMethods = new HashSet<>();
    private final Collection<Constructor<?>> _xlFunctionConstructors = new HashSet<>();

    Builder() {
    }

    public Builder xlConstant(final Class<?> clazz) {
      _xlConstantClasses.add(clazz);
      return this;
    }

    public Builder xlFunctions(final Class<?> clazz) {
      _xlFunctionsClasses.add(clazz);
      return this;
    }

    public Builder xlConstant(final Field field) {
      _xlConstantFields.add(field);
      return this;
    }

    public Builder xlFunction(final Method method) {
      _xlFunctionMethods.add(method);
      return this;
    }

    public Builder xlFunction(final Constructor<?> constructor) {
      _xlFunctionConstructors.add(constructor);
      return this;
    }

    public MockFunctionRegistry build() {
      return new MockFunctionRegistry(_xlConstantClasses, _xlFunctionsClasses, _xlConstantFields, _xlFunctionMethods, _xlFunctionConstructors);
    }
  }

  private final AtomicInteger _exportCounter = new AtomicInteger();
  private final Collection<Class<?>> _xlConstantClasses;
  private final Collection<Class<?>> _xlFunctionsClasses;
  private final Collection<Field> _xlConstantFields;
  private final Collection<Method> _xlFunctionMethods;
  private final Collection<Constructor<?>> _xlFunctionConstructors;
  private final Map<Integer, FunctionDefinition> _definitions;

  MockFunctionRegistry(final Collection<Class<?>> xlConstantClasses, final Collection<Class<?>> xlFunctionsClasses,
      final Collection<Field> xlConstantFields, final Collection<Method> xlFunctionMethods,
      final Collection<Constructor<?>> xlFunctionConstructors) {
    _xlConstantClasses = Collections.unmodifiableCollection(xlConstantClasses);
    _xlFunctionsClasses = Collections.unmodifiableCollection(xlFunctionsClasses);
    _xlConstantFields = Collections.unmodifiableCollection(xlConstantFields);
    _xlFunctionMethods = Collections.unmodifiableCollection(xlFunctionMethods);
    _xlFunctionConstructors = Collections.unmodifiableCollection(xlFunctionConstructors);
    _definitions = new HashMap<>();
  }

  @Override
  public void registerFunctions(final ExcelCallback callback) {
    for (final Map.Entry<Integer, FunctionDefinition> entry : _definitions.entrySet()) {
      callback.registerFunction(entry.getValue());
    }
  }

  @Override
  public FunctionDefinition getFunctionDefinition(final int exportNumber) {
    return _definitions.get(exportNumber);
  }

  @Override
  protected int allocateExport() {
    final int exportNumber = _exportCounter.getAndIncrement();
    return exportNumber;
  }

  @Override
  public void createAndRegisterFunctions(final InvokerFactory invokerFactory) {
    addDefinitions(getFunctionsForMethods(invokerFactory, _xlFunctionMethods));
    addDefinitions(getFunctionsForConstructors(invokerFactory, _xlFunctionConstructors));
    addDefinitions(getConstantsForFields(invokerFactory, _xlConstantFields));
    addDefinitions(getFunctionsForTypes(invokerFactory, _xlFunctionsClasses));
    addDefinitions(getConstantsForTypes(invokerFactory, _xlConstantClasses));
  }

  public Collection<FunctionDefinition> getFunctionDefinitions() {
    return Collections.unmodifiableCollection(_definitions.values());
  }

  private void addDefinitions(final List<FunctionDefinition> definitions) {
    for (final FunctionDefinition definition : definitions) {
      _definitions.put(definition.getExportNumber(), definition);
    }
  }

}
