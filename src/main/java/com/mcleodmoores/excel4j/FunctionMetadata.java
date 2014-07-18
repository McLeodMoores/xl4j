/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import com.mcleodmoores.excel4j.util.ArgumentChecker;

/**
 * Stores meta-data about an Excel function for the purposes of argument checking and function registration.
 */
public final class FunctionMetadata {
  
  private XLNamespace _namespace;
  private XLFunction _functionSpec;
  private XLArgument[] _arguments;
  
  private FunctionMetadata(final XLNamespace namespace, final XLFunction functionSpec, final XLArgument[] arguments) {
    _namespace = namespace;
    _functionSpec = functionSpec;
    _arguments = arguments;
  }
  
  /**
   * Create an instance given a namespace, functionSpec and arguments.
   * @param namespace  an XLNamespace annotation or null if no name space
   * @param functionSpec  an XLFunction annotation, not null
   * @param arguments  a non-null array of XLArgument annotations, must be same length as method parameter list.
   *                   the array itself may contain nulls to signify missing XLArgument annotations.
   * @return an instanec of a FunctionSpec
   */
  public static FunctionMetadata of(final XLNamespace namespace, final XLFunction functionSpec, final XLArgument[] arguments) {
    ArgumentChecker.notNull(functionSpec, "functionSpec");
    ArgumentChecker.notNull(arguments, "arguments");
    return new FunctionMetadata(namespace, functionSpec, arguments);
  }
  
  /**
   * Create an instance when no namespace is declared given a functionSpec and arguments.
   * @param functionSpec  an XLFunction annotation, not null
   * @param arguments  a non-null array of XLArgument annotations, must be same length as method parameter list.
   *                   the array itself may contain nulls to signify missing XLArgument annotations.
   * @return an instanec of a FunctionSpec
   */
  public static FunctionMetadata of(final XLFunction functionSpec, final XLArgument[] arguments) {
    ArgumentChecker.notNull(functionSpec, "functionSpec");
    ArgumentChecker.notNull(arguments, "arguments");
    return new FunctionMetadata(null, functionSpec, arguments);
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
   * @return an array of XLArgument, not null, but possibly containing null elements.  Will be same length as method argument list.
   */
  public XLArgument[] getArguments() {
    return _arguments;
  }
}
