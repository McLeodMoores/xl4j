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
  /** Information about the functions */
  private final XLFunctions _functionsSpec;
  /** Information about the function */
  private final XLFunction _functionSpec;
  /** Information about the function */
  private final XLConstant _constantSpec;
  /** The arguments of the method */
  private final XLParameter[] _parameters;
  /** The name of the function */
  private final String _name;

  /**
   * Creates an instance for a function representing a method or constructor.
   *
   * @param namespace
   *          the namespace
   * @param functionSpec
   *          the function specification
   * @param parameters
   *          the parameters to the function
   * @param name
   *          the function name
   */
  private FunctionMetadata(final XLNamespace namespace, final XLFunction functionSpec, final XLParameter[] parameters,
      final String name) {
    _namespace = namespace;
    _functionSpec = functionSpec;
    _functionsSpec = null;
    _constantSpec = null;
    _parameters = parameters;
    _name = name;
  }

  /**
   * Creates an instance for a function representing a field or enum.
   *
   * @param namespace
   *          the namespace
   * @param constantSpec
   *          the constant specification
   * @param name
   *          the function name
   */
  private FunctionMetadata(final XLNamespace namespace, final XLConstant constantSpec, final String name) {
    _namespace = namespace;
    _functionSpec = null;
    _functionsSpec = null;
    _constantSpec = constantSpec;
    _parameters = null;
    _name = name;
  }

  /**
   * Creates an instance for a function representing a method or constructor.
   * @param namespace
   *          the namespace
   * @param functionsSpec
   *          the class-level specification
   * @param parameters
   *          the parameters to the function
   * @param name
   *          the function name
   */
  private FunctionMetadata(final XLNamespace namespace, final XLFunctions functionsSpec, final XLParameter[] parameters,
      final String name) {
    _namespace = namespace;
    _functionSpec = null;
    _functionsSpec = functionsSpec;
    _constantSpec = null;
    _parameters = parameters;
    _name = name;
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
   *          to signify missing XLArgument annotations
   * @param name
   *          the function name, not null
   * @return the function metadata
   */
  public static FunctionMetadata of(final XLNamespace namespace, final XLFunction functionSpec, final XLParameter[] parameters,
      final String name) {
    ArgumentChecker.notNull(functionSpec, "functionSpec");
    ArgumentChecker.notNull(parameters, "parameters");
    ArgumentChecker.notNull(name, "name");
    return new FunctionMetadata(namespace, functionSpec, parameters, name);
  }

  /**
   * Create an instance when no namespace is declared given a functionSpec and arguments.
   *
   * @param functionSpec
   *          an XLFunction annotation, not null
   * @param parameters
   *          a non-null array of XLParameter annotations, must be same length as method parameter list. The array itself may contain nulls
   *          to signify missing XLParameter annotations.
   * @param name
   *          the function name, not null
   * @return the function metadata
   */
  public static FunctionMetadata of(final XLFunction functionSpec, final XLParameter[] parameters, final String name) {
    ArgumentChecker.notNull(functionSpec, "functionSpec");
    ArgumentChecker.notNull(parameters, "parameters");
    ArgumentChecker.notNull(name, "name");
    return new FunctionMetadata(null, functionSpec, parameters, name);
  }

  /**
   * Creates an instance for a constant.
   *
   * @param namespace
   *          an XLNamespace annotation, can be null
   * @param constantSpec
   *          an XLConstant annotation, not null
   * @param name
   *          the function name, not null
   * @return the function metadata
   */
  public static FunctionMetadata of(final XLNamespace namespace, final XLConstant constantSpec, final String name) {
    ArgumentChecker.notNull(constantSpec, "constantSpec");
    ArgumentChecker.notNull(name, "name");
    return new FunctionMetadata(namespace, constantSpec, name);
  }

  /**
   * Creates an instance for a constant when no namespace is declared.
   *
   * @param constantSpec
   *          an XLConstant annotation, not null
   * @param name
   *          the function name, not null
   * @return the function metadata
   */
  public static FunctionMetadata of(final XLConstant constantSpec, final String name) {
    ArgumentChecker.notNull(constantSpec, "constantSpec");
    ArgumentChecker.notNull(name, "name");
    return new FunctionMetadata(null, constantSpec, name);
  }

  /**
   * Create an instance given a namespace, functionsSpec and arguments.
   *
   * @param namespace
   *          an XLNamespace annotation or null if no name space
   * @param functionsSpec
   *          an XLFunctions annotation, not null
   * @param parameters
   *          a non-null array of XLArgument annotations, must be same length as method parameter list. The array itself may contain nulls
   *          to signify missing XLArgument annotations
   * @param name
   *          the function name, not null
   * @return the function metadata
   */
  public static FunctionMetadata of(final XLNamespace namespace, final XLFunctions functionsSpec, final XLParameter[] parameters,
      final String name) {
    ArgumentChecker.notNull(functionsSpec, "functionsSpec");
    ArgumentChecker.notNull(parameters, "parameters");
    ArgumentChecker.notNull(name, "name");
    return new FunctionMetadata(namespace, functionsSpec, parameters, name);
  }

  /**
   * Create an instance when no namespace is declared given a functionsSpec and arguments.
   *
   * @param functionsSpec
   *          an XLFunction annotation, not null
   * @param parameters
   *          a non-null array of XLParameter annotations, must be same length as method parameter list. The array itself may contain nulls
   *          to signify missing XLParameter annotations.
   * @param name
   *          the function name, not null
   * @return the function metadata
   */
  public static FunctionMetadata of(final XLFunctions functionsSpec, final XLParameter[] parameters, final String name) {
    ArgumentChecker.notNull(functionsSpec, "functionsSpec");
    ArgumentChecker.notNull(parameters, "parameters");
    ArgumentChecker.notNull(name, "name");
    return new FunctionMetadata(null, functionsSpec, parameters, name);
  }

  /**
   * @return the XLNamespace annotation or null if no namespace
   */
  public XLNamespace getNamespace() {
    return _namespace;
  }

  /**
   * @return true if the underlying specification is for a constant, or false for a function or functions
   */
  public boolean isConstantSpec() {
    return _constantSpec != null;
  }

  /**
   * @return the XLFunction annotation, can be null
   */
  public XLFunction getFunctionSpec() {
    return _functionSpec;
  }

  /**
   * @return the XLFunctions annotation, can be null
   */
  public XLFunctions getFunctionsSpec() {
    return _functionsSpec;
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
    return _name;
  }

  /**
   * @return an array of XLParameter. For a function representing a method or constructor, this will not be null,
   * but may contain null elements, and will be same length as method parameter list.
   * @throws Excel4JRuntimeException
   *          if the function represents a constant or field
   */
  public XLParameter[] getParameters() {
    if (_functionSpec == null && _functionsSpec == null) {
      // have a XLConstant
      throw new Excel4JRuntimeException("Cannot get argument annotations for a field or enum");
    }
    return _parameters;
  }
}
