/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import com.mcleodmoores.excel4j.javacode.MethodInvoker;
import com.mcleodmoores.excel4j.util.ArgumentChecker;

/**
 * Class to store meta data and type conversion information about excel functions.
 */
public final class FunctionDefinition {
  private FunctionMetadata _functionMetadata;
  private MethodInvoker _methodInvoker;

  private FunctionDefinition(final FunctionMetadata functionMetadata, final MethodInvoker methodInvoker) {
    _functionMetadata = functionMetadata;
    _methodInvoker = methodInvoker;
  }
  
  /**
   * Static factory method to create an instance.
   * @param functionMetadata  the annotation-based metadata about the function
   * @param methodInvoker  the type conversion and method invocation binding for this function
   * @return an instance of a FunctionDefinition
   */
  public static FunctionDefinition of(final FunctionMetadata functionMetadata, final MethodInvoker methodInvoker) {
    ArgumentChecker.notNull(functionMetadata, "functionMetadata");
    ArgumentChecker.notNull(methodInvoker, "methodInvoker");
    return new FunctionDefinition(functionMetadata, methodInvoker);
  }

  /**
   * @return the function metadata, not null
   */
  public FunctionMetadata getFunctionMetadata() {
    return _functionMetadata;
  }

  /**
   * @return the method invoker, not null
   */
  public MethodInvoker getMethodInvoker() {
    return _methodInvoker;
  }
}
