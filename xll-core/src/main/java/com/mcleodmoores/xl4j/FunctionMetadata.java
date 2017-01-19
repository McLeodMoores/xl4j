/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;

/**
 * Stores meta-data about an Excel function for the purposes of argument checking and function registration.
 */
public final class FunctionMetadata {
  /** The prefix for function names */
  private final XLNamespace _namespace;
  /** Information about the function */
  private final XLFunction _functionSpec;
  /** Information about the function */
  private final XLConstant _constantSpec;
  /** The arguments of the method */
  private final XLParameter[] _parameters;

  /**
   * Creates an instance for a function representing a method or constructor.
   *
   * @param namespace
   *          the namespace
   * @param functionSpec
   *          the function specification
   * @param parameters
   *          the parameters to the function
   */
  private FunctionMetadata(final XLNamespace namespace, final XLFunction functionSpec, final XLParameter[] parameters) {
    _namespace = namespace;
    _functionSpec = functionSpec;
    _constantSpec = null;
    _parameters = parameters;
  }

  /**
   * Creates an instance for a function representing a field or enum.
   *
   * @param namespace
   *          the namespace
   * @param constantSpec
   *          the constant specification
   */
  private FunctionMetadata(final XLNamespace namespace, final XLConstant constantSpec) {
    _namespace = namespace;
    _functionSpec = null;
    _constantSpec = constantSpec;
    _parameters = null;
  }

  /**
   * Create an instance given a namespace, functionSpec and arguments.
   *
   * @param namespace
   *          an XLNamespace annotation or null if no name space
   * @param functionSpec
   *          an XLFunction annotation, not null
   * @param parameters
   *          a non-null array of XLArgument annotations, must be same length as method parameter list. The array itself may contain nulls
   *          to signify missing XLArgument annotations.
   * @return the function metadata
   */
  public static FunctionMetadata of(final XLNamespace namespace, final XLFunction functionSpec, final XLParameter[] parameters) {
    ArgumentChecker.notNull(functionSpec, "functionSpec");
    ArgumentChecker.notNull(parameters, "parameters");
    return new FunctionMetadata(namespace, functionSpec, parameters);
  }

  /**
   * Create an instance when no namespace is declared given a functionSpec and arguments.
   *
   * @param functionSpec
   *          an XLFunction annotation, not null
   * @param parameters
   *          a non-null array of XLParameter annotations, must be same length as method parameter list. The array itself may contain nulls
   *          to signify missing XLParameter annotations.
   * @return the function metadata
   */
  public static FunctionMetadata of(final XLFunction functionSpec, final XLParameter[] parameters) {
    ArgumentChecker.notNull(functionSpec, "functionSpec");
    ArgumentChecker.notNull(parameters, "parameters");
    return new FunctionMetadata(null, functionSpec, parameters);
  }

  /**
   * Creates an instance for a constant.
   *
   * @param namespace
   *          an XLNamespace annotation, can be null
   * @param constantSpec
   *          an XLConstant annotation, not null
   * @return the function metadata
   */
  public static FunctionMetadata of(final XLNamespace namespace, final XLConstant constantSpec) {
    ArgumentChecker.notNull(constantSpec, "constantSpec");
    return new FunctionMetadata(namespace, constantSpec);
  }

  /**
   * Creates an instance for a constaint when no nameapace is declared.
   *
   * @param constantSpec
   *          an XLConstant annotation, not null
   * @return the function metadata
   */
  public static FunctionMetadata of(final XLConstant constantSpec) {
    ArgumentChecker.notNull(constantSpec, "constantSpec");
    return new FunctionMetadata(null, constantSpec);
  }

  /**
   * @return the XLNamespace annotation or null if no namespace
   */
  public XLNamespace getNamespace() {
    return _namespace;
  }

  /**
   * @return true if the underlying specification is for a function, or false for a constant
   */
  public boolean isFunctionSpec() {
    return _functionSpec != null;
  }

  /**
   * @return the XLFunction annotation, can be null
   */
  public XLFunction getFunctionSpec() {
    return _functionSpec;
  }

  /**
   * @return the XLConstant annotation, can be null
   */
  public XLConstant getConstantSpec() {
    return _constantSpec;
  }

  /**
   * @return the name of the function
   */
  public String getName() {
    return _functionSpec == null ? _constantSpec.name() : _functionSpec.name();
  }
  /**
   * @return an array of XLParameter. For a function representing a method or constructor, this will not be null,
   * but may contain null elements, and will be same length as method parameter list.
   * @throws Excel4JRuntimeException
   *          if the function represents a constant or field
   */
  public XLParameter[] getParameters() {
    if (_functionSpec == null) {
      // have a XLConstant
      throw new Excel4JRuntimeException("Cannot get argument annotations for a field or enum");
    }
    return _parameters;
  }
}
