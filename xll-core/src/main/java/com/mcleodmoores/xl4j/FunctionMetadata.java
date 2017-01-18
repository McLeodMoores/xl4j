/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import com.mcleodmoores.xl4j.util.ArgumentChecker;

/**
 * Stores meta-data about an Excel function for the purposes of argument checking and function registration.
 */
public final class FunctionMetadata {
  /** The prefix for function names */
  private final XLNamespace _namespace;
  /** Information about the function */
  private final XLFunction _functionSpec;
  /** The arguments of the method */
  private final XLParameter[] _parameters;

  /**
   * Creates an instance.
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
    _parameters = parameters;
  }

  /**
   * Create an instance given a namespace, functionSpec and parameters.
   * 
   * @param namespace
   *          an XLNamespace annotation or null if no name space
   * @param functionSpec
   *          an XLFunction annotation, not null
   * @param parameters
   *          a non-null array of XLParameter annotations, must be same length as method parameter list. The array itself may contain nulls
   *          to signify missing XLParameter annotations.
   * @return an instance of a FunctionSpec
   */
  public static FunctionMetadata of(final XLNamespace namespace, final XLFunction functionSpec, final XLParameter[] parameters) {
    ArgumentChecker.notNull(functionSpec, "functionSpec");
    ArgumentChecker.notNull(parameters, "parameters");
    return new FunctionMetadata(namespace, functionSpec, parameters);
  }

  /**
   * Create an instance when no namespace is declared given a functionSpec and parameters.
   * 
   * @param functionSpec
   *          an XLFunction annotation, not null
   * @param parameters
   *          a non-null array of XLParameter annotations, must be same length as method parameter list. The array itself may contain nulls
   *          to signify missing XLParameter annotations.
   * @return an instance of a FunctionSpec
   */
  public static FunctionMetadata of(final XLFunction functionSpec, final XLParameter[] parameters) {
    ArgumentChecker.notNull(functionSpec, "functionSpec");
    ArgumentChecker.notNull(parameters, "parameters");
    return new FunctionMetadata(null, functionSpec, parameters);
  }

  /**
   * @return the XLNamespace annotation or null if no namespace
   */
  public XLNamespace getNamespace() {
    return _namespace;
  }

  /**
   * @return the XLFunction annotation, not null
   */
  public XLFunction getFunctionSpec() {
    return _functionSpec;
  }

  /**
   * @return an array of XLParameter, not null, but possibly containing null elements. Will be same length as method parameter list.
   */
  public XLParameter[] getParameters() {
    return _parameters;
  }
}
