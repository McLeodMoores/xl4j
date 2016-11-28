/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import com.mcleodmoores.xl4j.javacode.ConstructorInvoker;
import com.mcleodmoores.xl4j.javacode.MethodInvoker;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.util.ExportUtils;

/**
 * Class to store meta-data and type conversion information about Excel functions. The functions can
 * be created from either methods or constructors.
 */
public final class FunctionDefinition {
  /** Meta data about the function */
  private final FunctionMetadata _functionMetadata;
  /** The method invoker */
  private final MethodInvoker _methodInvoker;
  /** The constructor invoker */
  private final ConstructorInvoker _constructorInvoker;
  /** True if the function calls a method, false for a constructor */
  private final boolean _isMethod;
  /** Function export number in registry */
  private final int _exportNumber;
  /** The name of the function */
  private final String _name;

  /**
   * Creates an instance for a method.
   *
   * @param functionMetadata
   *          the meta-data
   * @param methodInvoker
   *          the method invoker
   * @param exportNumber
   *          the export number
   * @param name
   *          the function name
   */
  private FunctionDefinition(final FunctionMetadata functionMetadata, final MethodInvoker methodInvoker, final int exportNumber,
      final String name) {
    _functionMetadata = functionMetadata;
    _methodInvoker = methodInvoker;
    _exportNumber = exportNumber;
    _constructorInvoker = null;
    _isMethod = true;
    _name = name;
  }

  /**
   * Creates an instance for a constructor.
   * @param functionMetadata
   *          the meta-data
   * @param constructorInvoker
   *          the constructor invoker
   * @param exportNumber
   *          the export number
   * @param name
   *          the function name
   */
  private FunctionDefinition(final FunctionMetadata functionMetadata, final ConstructorInvoker constructorInvoker, final int exportNumber,
      final String name) {
    _functionMetadata = functionMetadata;
    _constructorInvoker = constructorInvoker;
    _exportNumber = exportNumber;
    _methodInvoker = null;
    _isMethod = false;
    _name = name;
  }

  /**
   * Static factory method to create an instance for a method.
   *
   * @param functionMetadata
   *          the annotation-based meta-data about the function, not null
   * @param methodInvoker
   *          the type conversion and method invocation binding for this function, not null
   * @param exportNumber
   *          the number of the DLL export that handles this function (only unique to the number of parameters used)
   * @return an instance of a FunctionDefinition
   */
  public static FunctionDefinition of(final FunctionMetadata functionMetadata, final MethodInvoker methodInvoker, final int exportNumber) {
    ArgumentChecker.notNull(functionMetadata, "functionMetadata");
    ArgumentChecker.notNull(methodInvoker, "methodInvoker");
    return new FunctionDefinition(functionMetadata, methodInvoker, exportNumber, null);
  }

  /**
   * Static factory method to create an instance for a method.
   *
   * @param functionMetadata
   *          the annotation-based meta-data about the function, not null
   * @param methodInvoker
   *          the type conversion and method invocation binding for this function, not null
   * @param exportNumber
   *          the number of the DLL export that handles this function (only unique to the number of parameters used)
   * @param name
   *          the name of the function, not null
   * @return an instance of a FunctionDefinition
   */
  public static FunctionDefinition of(final FunctionMetadata functionMetadata, final MethodInvoker methodInvoker, final int exportNumber,
      final String name) {
    ArgumentChecker.notNull(functionMetadata, "functionMetadata");
    ArgumentChecker.notNull(methodInvoker, "methodInvoker");
    ArgumentChecker.notNull(name, "name");
    return new FunctionDefinition(functionMetadata, methodInvoker, exportNumber, name);
  }

  /**
   * Static factory method to create an instance for a constructor.
   *
   * @param functionMetadata
   *          the annotation-based meta-data about the function, not null
   * @param constructorInvoker
   *          the type conversion and constructor invocation binding for this function, not null
   * @param exportNumber
   *          the number of the DLL export that handles this function (only unique to the number of parameters used)
   * @return an instance of a FunctionDefinition
   */
  public static FunctionDefinition of(final FunctionMetadata functionMetadata, final ConstructorInvoker constructorInvoker, final int exportNumber) {
    ArgumentChecker.notNull(functionMetadata, "functionMetadata");
    ArgumentChecker.notNull(constructorInvoker, "constructorInvoker");
    return new FunctionDefinition(functionMetadata, constructorInvoker, exportNumber, null);
  }

  /**
   * Static factory method to create an instance for a constructor.
   *
   * @param functionMetadata
   *          the annotation-based meta-data about the function, not null
   * @param constructorInvoker
   *          the type conversion and constructor invocation binding for this function, not null
   * @param exportNumber
   *          the number of the DLL export that handles this function (only unique to the number of parameters used)
   * @param name
   *          the name of the function, not null
   * @return an instance of a FunctionDefinition
   */
  public static FunctionDefinition of(final FunctionMetadata functionMetadata, final ConstructorInvoker constructorInvoker, final int exportNumber,
      final String name) {
    ArgumentChecker.notNull(functionMetadata, "functionMetadata");
    ArgumentChecker.notNull(constructorInvoker, "constructorInvoker");
    ArgumentChecker.notNull(name, "name");
    return new FunctionDefinition(functionMetadata, constructorInvoker, exportNumber, name);
  }

  /**
   * @return the function meta-data, not null
   */
  public FunctionMetadata getFunctionMetadata() {
    return _functionMetadata;
  }

  /**
   * @return true if the function represents a method
   */
  public boolean isMethod() {
    return _isMethod;
  }

  /**
   * @return  the name of the function, either user defined or taken from the constructor or method name
   */
  public String getMethodOrConstructorName() {
    if (_name != null) {
      return _name;
    }
    return _isMethod ? _methodInvoker.getMethodName() : _constructorInvoker.getDeclaringClass().getSimpleName();
  }

  /**
   * @return  true if the arguments to the method or constructor are varargs
   */
  public boolean isVarArgs() {
    return _isMethod ? _methodInvoker.isVarArgs() : _constructorInvoker.isVarArgs();
  }

  /**
   * @return  true if the method is static. Throws an exception if the function refers to a constructor.
   */
  public boolean isStatic() {
    if (_isMethod) {
      return _methodInvoker.isStatic();
    }
    throw new Excel4JRuntimeException("isStatic() is not a valid method to call on a constructor");
  }

  /**
   * @return the method invoker, not null. Throws an exception if the function refers to a constructor.
   */
  public MethodInvoker getMethodInvoker() {
    if (_methodInvoker == null) {
      throw new Excel4JRuntimeException("Method invoker not set for function " + _functionMetadata.getFunctionSpec().name());
    }
    return _methodInvoker;
  }

  /**
   * @return the constructor invoker, not null. Throws an exception if the function refers to a method.
   */
  public ConstructorInvoker getConstructorInvoker() {
    if (_constructorInvoker == null) {
      throw new Excel4JRuntimeException("Constructor invoker not set for function " + _functionMetadata.getFunctionSpec().name());
    }
    return _constructorInvoker;
  }

  /**
   * @return the export name, not null
   */
  public String getExportName() {
    return ExportUtils.buildExportName(_exportNumber);
  }

  /**
   * @return the export number
   */
  public int getExportNumber() {
    return _exportNumber;
  }
}
