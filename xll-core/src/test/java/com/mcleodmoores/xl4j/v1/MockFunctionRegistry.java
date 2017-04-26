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
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.mcleodmoores.xl4j.v1.api.core.ExcelCallback;
import com.mcleodmoores.xl4j.v1.api.core.FunctionDefinition;
import com.mcleodmoores.xl4j.v1.api.core.InvokerFactory;
import com.mcleodmoores.xl4j.v1.core.AbstractFunctionRegistry;

/**
 * A {@link com.mcleodmoores.xl4j.v1.api.core.FunctionRegistry} for use in tests where functions are registered
 * by the user.
 */
public final class MockFunctionRegistry extends AbstractFunctionRegistry {

  /**
   * Gets a builder.
   * @return
   *          the builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builds a function registry.
   */
  public static final class Builder {
    private final Collection<Class<?>> _xlConstantClasses = new HashSet<>();
    private final Collection<Class<?>> _xlFunctionsClasses = new HashSet<>();
    private final Collection<Field> _xlConstantFields = new HashSet<>();
    private final Collection<Method> _xlFunctionMethods = new HashSet<>();
    private final Collection<Constructor<?>> _xlFunctionConstructors = new HashSet<>();

    /**
     * Default constructor.
     */
    Builder() {
    }

    /**
     * Adds all fields from a class.
     * @param clazz
     *        the class
     * @return
     *        the builder
     */
    public Builder xlConstant(final Class<?> clazz) {
      _xlConstantClasses.add(clazz);
      return this;
    }

    /**
     * Adds all constructors and methods from a class.
     * @param clazz
     *        the class
     * @return
     *        the builder
     */
    public Builder xlFunctions(final Class<?> clazz) {
      _xlFunctionsClasses.add(clazz);
      return this;
    }

    /**
     * Adds a field.
     * @param field
     *        the field
     * @return
     *        the builder
     */
    public Builder xlConstant(final Field field) {
      _xlConstantFields.add(field);
      return this;
    }

    /**
     * Adds a method.
     * @param method
     *        the method
     * @return
     *        the builder
     */
    public Builder xlFunction(final Method method) {
      _xlFunctionMethods.add(method);
      return this;
    }

    /**
     * Adds a constructor.
     * @param constructor
     *        the constructor
     * @return
     *        the builder
     */
    public Builder xlFunction(final Constructor<?> constructor) {
      _xlFunctionConstructors.add(constructor);
      return this;
    }

    /**
     * Builds a mock function registry.
     * @return
     *        the registry
     */
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

  /**
   * Registers the classes, fields, methods and constructors as functions.
   * @param xlConstantClasses
   *          the classes from which to get all visible fields
   * @param xlFunctionsClasses
   *          the classes from which to get all visible methods and constructors
   * @param xlConstantFields
   *          the fields
   * @param xlFunctionMethods
   *          the methods
   * @param xlFunctionConstructors
   *          the constructors
   */
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

  /**
   * Gets the functions definitions.
   * @return
   *          the function definitions
   */
  public Set<FunctionDefinition> getFunctionDefinitions() {
    return Collections.unmodifiableSet(new HashSet<>(_definitions.values()));
  }

  private void addDefinitions(final List<FunctionDefinition> definitions) {
    for (final FunctionDefinition definition : definitions) {
      _definitions.put(definition.getExportNumber(), definition);
    }
  }

}
