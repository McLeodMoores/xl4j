/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import com.mcleodmoores.xl4j.javacode.ConstructorInvoker;
import com.mcleodmoores.xl4j.javacode.FieldInvoker;
import com.mcleodmoores.xl4j.javacode.JavaTypeForFunction;
import com.mcleodmoores.xl4j.javacode.MethodInvoker;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.util.ExportUtils;

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
  private final FieldInvoker _fieldInvoker;
  /** Method, constructor, field or enum */
  private final JavaTypeForFunction _functionType;
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
    _fieldInvoker = null;
    _functionType = JavaTypeForFunction.METHOD;
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
    _fieldInvoker = null;
    _functionType = JavaTypeForFunction.CONSTRUCTOR;
    _name = name;
  }

  private FunctionDefinition(final FunctionMetadata functionMetadata, final FieldInvoker fieldInvoker, final int exportNumber,
      final String name) {
    _functionMetadata = functionMetadata;
    _fieldInvoker = fieldInvoker;
    _exportNumber = exportNumber;
    _methodInvoker = null;
    _constructorInvoker = null;
    _functionType = JavaTypeForFunction.FIELD;
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
   *          the type conversion and constructor instantiation binding for this function, not null
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
   * Static factory method to create an instance for a field or enum.
   *
   * @param functionMetadata
   *          the annotation-based meta-data about the function, not null
   * @param fieldInvoker
   *          the type conversion binding for this function, not null
   * @param exportNumber
   *          the number of the DLL export that handles this function (only unique to the number of parameters used)
   * @param name
   *          the name of the function, not null
   * @return an instance of a FunctionDefinition
   */
  public static FunctionDefinition of(final FunctionMetadata functionMetadata, final FieldInvoker fieldInvoker, final int exportNumber,
      final String name) {
    ArgumentChecker.notNull(functionMetadata, "functionMetadata");
    ArgumentChecker.notNull(fieldInvoker, "fieldInvoker");
    return new FunctionDefinition(functionMetadata, fieldInvoker, exportNumber, name);
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
  public JavaTypeForFunction getJavaTypeForFunction() {
    return _functionType;
  }

  /**
   * @return  the name of the function, either user defined, taken from the annotation name() property or taken from the constructor or method name
   */
  public String getFunctionName() {
    if (_name != null) {
      return _name;
    }
    if (_functionMetadata.getName().isEmpty()) {
      switch (_functionType) {
        case METHOD:
          return _methodInvoker.getMethodName();
        case CONSTRUCTOR:
          return _constructorInvoker.getDeclaringClass().getName();
        case FIELD:
          return _fieldInvoker.getFieldName();
        default:
          throw new Excel4JRuntimeException("Unhandled type " + _functionType);
      }
    }
    return _functionMetadata.getName();
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
        throw new Excel4JRuntimeException("isVarArgs() cannot be called for " + _functionType);
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
        throw new Excel4JRuntimeException("isStatic() is not a valid method to call on a constructor");
    }
  }

  /**
   * @return the method invoker, not null. Throws an exception if the function does not refer to a method.
   */
  public MethodInvoker getMethodInvoker() {
    if (_methodInvoker == null) {
      throw new Excel4JRuntimeException("Method invoker not set for function " + _functionMetadata.getName());
    }
    return _methodInvoker;
  }

  /**
   * @return the constructor invoker, not null. Throws an exception if the function does not refer to a constructor.
   */
  public ConstructorInvoker getConstructorInvoker() {
    if (_constructorInvoker == null) {
      throw new Excel4JRuntimeException("Constructor invoker not set for function " + _functionMetadata.getName());
    }
    return _constructorInvoker;
  }

  /**
   * @return the field invoker, not null. Throws an exception if the function does not refer to a field or enum.
   */
  public FieldInvoker getFieldInvoker() {
    if (_fieldInvoker == null) {
      throw new Excel4JRuntimeException("Field invoker not set for function " + _functionMetadata.getName());
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
