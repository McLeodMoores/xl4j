/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.core;

import com.mcleodmoores.xl4j.v1.core.FunctionMetadata;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.ExportUtils;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Class to store meta-data and type conversion information about Excel functions. The functions can
 * be created from methods, constructors, fields or enums.
 */
public final class FunctionDefinition {
  /** Meta data about the function */
  private final FunctionMetadata _functionMetadata;
  /** The method invoker */
  private final MethodInvoker _methodInvoker;
  /** The constructor invoker */
  private final ConstructorInvoker _constructorInvoker;
  /** The field invoker */
  private final FieldGetter _fieldInvoker;
  /** Method, constructor, field or enum */
  private final CallTarget _functionType;
  /** Function export number in registry */
  private final int _exportNumber;

  /**
   * Creates an instance for a method.
   *
   * @param functionMetadata
   *          the meta-data
   * @param methodInvoker
   *          the method invoker
   * @param exportNumber
   *          the export number
   */
  private FunctionDefinition(final FunctionMetadata functionMetadata, final MethodInvoker methodInvoker, final int exportNumber) {
    _functionMetadata = functionMetadata;
    _methodInvoker = methodInvoker;
    _exportNumber = exportNumber;
    _constructorInvoker = null;
    _fieldInvoker = null;
    _functionType = CallTarget.METHOD;
  }

  /**
   * Creates an instance for a constructor.
   * @param functionMetadata
   *          the meta-data
   * @param constructorInvoker
   *          the constructor invoker
   * @param exportNumber
   *          the export number
   */
  private FunctionDefinition(final FunctionMetadata functionMetadata, final ConstructorInvoker constructorInvoker, final int exportNumber) {
    _functionMetadata = functionMetadata;
    _constructorInvoker = constructorInvoker;
    _exportNumber = exportNumber;
    _methodInvoker = null;
    _fieldInvoker = null;
    _functionType = CallTarget.CONSTRUCTOR;
  }

  /**
   * Creates an instance for a field.
   * @param functionMetadata
   *          the meta-data
   * @param fieldInvoker
   *          the field invoker
   * @param exportNumber
   *          the export number
   */
  private FunctionDefinition(final FunctionMetadata functionMetadata, final FieldGetter fieldInvoker, final int exportNumber) {
    _functionMetadata = functionMetadata;
    _fieldInvoker = fieldInvoker;
    _exportNumber = exportNumber;
    _methodInvoker = null;
    _constructorInvoker = null;
    _functionType = CallTarget.FIELD;
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
    return new FunctionDefinition(functionMetadata, methodInvoker, exportNumber);
  }

  /**
   * Static factory method to create an instance for a constructor.
   *
   * @param functionMetadata
   *          the annotation-based meta-data about the function, not null
   * @param constructorInvoker
   *          the type conversion and constructor instantiation binding for this function, not null
   * @param exportNumber
   *          the number of the DLL export that handles this function (only unique to the number of parameters used)
   * @return an instance of a FunctionDefinition
   */
  public static FunctionDefinition of(final FunctionMetadata functionMetadata, final ConstructorInvoker constructorInvoker, final int exportNumber) {
    ArgumentChecker.notNull(functionMetadata, "functionMetadata");
    ArgumentChecker.notNull(constructorInvoker, "constructorInvoker");
    return new FunctionDefinition(functionMetadata, constructorInvoker, exportNumber);
  }

  /**
   * Static factory method to create an instance for a field or enum.
   *
   * @param functionMetadata
   *          the annotation-based meta-data about the function, not null
   * @param fieldInvoker
   *          the type conversion binding for this function, not null
   * @param exportNumber
   *          the number of the DLL export that handles this function (only unique to the number of parameters used)
   * @return an instance of a FunctionDefinition
   */
  public static FunctionDefinition of(final FunctionMetadata functionMetadata, final FieldGetter fieldInvoker, final int exportNumber) {
    ArgumentChecker.notNull(functionMetadata, "functionMetadata");
    ArgumentChecker.notNull(fieldInvoker, "fieldInvoker");
    return new FunctionDefinition(functionMetadata, fieldInvoker, exportNumber);
  }

  /**
   * @return the function meta-data, not null
   */
  public FunctionMetadata getFunctionMetadata() {
    return _functionMetadata;
  }

  /**
   * @return the call target (field, method or constructor)
   */
  public CallTarget getCallTargetForFunction() {
    return _functionType;
  }

  /**
   * @return  true if the arguments to the method or constructor are varargs. Always returns false if the function
   * was created from a field or enum.
   */
  public boolean isVarArgs() {
    switch (_functionType) {
      case METHOD:
        return _methodInvoker.isVarArgs();
      case CONSTRUCTOR:
        return _constructorInvoker.isVarArgs();
      case FIELD:
      default:
        throw new XL4JRuntimeException("isVarArgs() cannot be called for " + _functionType);
    }
  }

  /**
   * @return  true if a method or function is static. Throws an exception if the function refers to a constructor.
   * Always returns true if the function was created from an enum.
   */
  public boolean isStatic() {
    switch (_functionType) {
      case METHOD:
        return _methodInvoker.isStatic();
      case FIELD:
        return _fieldInvoker.isStatic();
      default:
        throw new XL4JRuntimeException("isStatic() is not a valid method to call on a constructor");
    }
  }

  /**
   * @return the method invoker, not null. Throws an exception if the function does not refer to a method.
   */
  public MethodInvoker getMethodInvoker() {
    if (_methodInvoker == null) {
      throw new XL4JRuntimeException("Method invoker not set for function " + _functionMetadata.getName());
    }
    return _methodInvoker;
  }

  /**
   * @return the constructor invoker, not null. Throws an exception if the function does not refer to a constructor.
   */
  public ConstructorInvoker getConstructorInvoker() {
    if (_constructorInvoker == null) {
      throw new XL4JRuntimeException("Constructor invoker not set for function " + _functionMetadata.getName());
    }
    return _constructorInvoker;
  }

  /**
   * @return the field invoker, not null. Throws an exception if the function does not refer to a field or enum.
   */
  public FieldGetter getFieldInvoker() {
    if (_fieldInvoker == null) {
      throw new XL4JRuntimeException("Field invoker not set for function " + _functionMetadata.getName());
    }
    return _fieldInvoker;
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
