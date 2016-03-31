/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import com.mcleodmoores.excel4j.util.ArgumentChecker;

/**
 * Stores meta-data about a constructor that can be accessed from Excel. This is used for argument checking and registration.
 */
public final class ConstructorMetadata {
  /** The prefix for constructor names */
  private final XLNamespace _namespace;
  /** Infomation about the constructor */
  private final XLConstructor _constructorSpec;
  /** The arguments of the constructor */
  private final XLArgument[] _arguments;

  /**
   * Creates an instance.
   * @param namespace  the namespace
   * @param constructorSpec  the constructor specification
   * @param arguments  the arguments to the constructor
   */
  private ConstructorMetadata(final XLNamespace namespace, final XLConstructor constructorSpec, final XLArgument[] arguments) {
    _namespace = namespace;
    _constructorSpec = constructorSpec;
    _arguments = arguments;
  }

  /**
   * Create an instance given a namespace, constructor specification and arguments.
   * @param namespace  an XLNamespace annotation or null if no name space
   * @param constructorSpec  an XLConstructor annotation, not null
   * @param arguments  a non-null array of XLArgument annotations, must be same length as method parameter list.
   * The array itself may contain nulls to signify missing XLArgument annotations.
   * @return  an instance of a ConstructorDefinition
   */
  public static ConstructorMetadata of(final XLNamespace namespace, final XLConstructor constructorSpec, final XLArgument[] arguments) {
    ArgumentChecker.notNull(constructorSpec, "constructorSpec");
    ArgumentChecker.notNull(arguments, "arguments");
    return new ConstructorMetadata(namespace, constructorSpec, arguments);
  }

  /**
   * Create an instance given a constructor specification and arguments.
   * @param constructorSpec  an XLConstructor annotation, not null
   * @param arguments  a non-null array of XLArgument annotations, must be same length as method parameter list.
   * The array itself may contain nulls to signify missing XLArgument annotations.
   * @return  an instance of a ConstructorDefinition
   */
  public static ConstructorMetadata of(final XLConstructor constructorSpec, final XLArgument[] arguments) {
    ArgumentChecker.notNull(constructorSpec, "constructorSpec");
    ArgumentChecker.notNull(arguments, "arguments");
    return new ConstructorMetadata(null, constructorSpec, arguments);
  }

  /**
   * @return  the XLNamespace annotation or null if no namespace
   */
  public XLNamespace getNamespace() {
    return _namespace;
  }

  /**
   * @return  the XLConstructor annotation, not null
   */
  public XLConstructor getConstructorSpec() {
    return _constructorSpec;
  }

  /**
   * @return  an array of XLArgument, not null, but possibly containing null elements.  Will be same length as method argument list.
   */
  public XLArgument[] getArguments() {
    return _arguments;
  }
}
