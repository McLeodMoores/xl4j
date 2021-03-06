/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.simulator;

import java.lang.reflect.Method;

import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * An class representing a worksheet function on the mock excel side.
 */
public final class FunctionEntry {
  private final String _functionName;
  private final String[] _argumentNames;
  private final Class<?>[] _argumentTypes;
  private final Class<?> _returnType;
  private final String[] _argumentsHelp;
  private final String _description;
  private final FunctionAttributes _functionAttributes;
  private final Method _entryPointMethod;

  /**
   * @param functionName
   *          the name of the function or command on the worksheet
   * @param argumentNames
   *          an array of the argument names in order, not null
   * @param argumentTypes
   *          an array of the types of each argument in order, not null
   * @param returnType
   *          the type returned by this function, not null
   * @param argumentsHelp
   *          an array of help text for each argument in order, not null
   * @param description
   *          a description of this function
   * @param functionAttributes
   *          the function attributes
   * @param entryPointMethod
   *          the method used to handle this function call
   */
  private FunctionEntry(final String functionName, final String[] argumentNames, final Class<?>[] argumentTypes, final Class<?> returnType,
      final String[] argumentsHelp, final String description, final FunctionAttributes functionAttributes, final Method entryPointMethod) {
    _functionName = functionName;
    _argumentNames = argumentNames;
    _argumentTypes = argumentTypes;
    _returnType = returnType;
    _argumentsHelp = argumentsHelp;
    _description = description;
    _functionAttributes = functionAttributes;
    _entryPointMethod = entryPointMethod;
  }

  /**
   * Static factory method for creating a FunctionEntry.
   * 
   * @param functionName
   *          the name of the function or command on the worksheet, not null
   * @param argumentNames
   *          an array of the argument names in order, not null and no null elements
   * @param argumentTypes
   *          an array of the types of each argument in order, not null an no null elements
   * @param returnType
   *          the type returned by this function, not null
   * @param argumentsHelp
   *          an array of help text for each argument in order, not null and no null elements
   * @param description
   *          a description of this function, not null
   * @param functionAttributes
   *          the function attributes, not null
   * @param entryPointMethod
   *          the method used to handle this function call, not null
   * @return an instance
   */
  public static FunctionEntry of(final String functionName, final String[] argumentNames, final Class<?>[] argumentTypes,
      final Class<?> returnType, final String[] argumentsHelp, final String description, final FunctionAttributes functionAttributes,
      final Method entryPointMethod) {
    ArgumentChecker.notNull(functionName, "functionName");
    ArgumentChecker.notNullArray(argumentNames, "argumentNames");
    ArgumentChecker.notNullArray(argumentTypes, "argumentTypes");
    ArgumentChecker.notNull(returnType, "returnType");
    ArgumentChecker.notNullArray(argumentsHelp, "argumentsHelp");
    ArgumentChecker.notNull(description, "description");
    ArgumentChecker.notNull(functionAttributes, "functionAttributes");
    ArgumentChecker.notNull(entryPointMethod, "entryPointMethod");
    return new FunctionEntry(functionName, argumentNames, argumentTypes, returnType, argumentsHelp, description, functionAttributes,
        entryPointMethod);
  }

  /**
   * @return the function name
   */
  public String getFunctionName() {
    return _functionName;
  }

  /**
   * @return the argumentNames
   */
  public String[] getArgumentNames() {
    return _argumentNames;
  }

  /**
   * @return the argumentTypes
   */
  public Class<?>[] getArgumentTypes() {
    return _argumentTypes;
  }

  /**
   * @return the returnType
   */
  public Class<?> getReturnType() {
    return _returnType;
  }

  /**
   * @return the argumentsHelp
   */
  public String[] getArgumentsHelp() {
    return _argumentsHelp;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return _description;
  }

  /**
   * @return the functionAttributes
   */
  public FunctionAttributes getFunctionAttributes() {
    return _functionAttributes;
  }

  /**
   * @return the entryPointMethod
   */
  public Method getEntryPointMethod() {
    return _entryPointMethod;
  }

}