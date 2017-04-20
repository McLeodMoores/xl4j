/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.simulator;

import com.mcleodmoores.xl4j.v1.api.annotations.FunctionType;
import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Class to hold some meta-data about a worksheet function/macro.
 */
public final class FunctionAttributes {
  private final FunctionType _functionType;
  private final boolean _asynchronous;
  private final boolean _volatile;
  private final boolean _macroEquivalent;
  private final boolean _multiThreadSafe;
  private final TypeConversionMode _resultType;

  /**
   * Private constructor.
   *
   * @param functionType
   *          whether this function is a function or command
   * @param asynchronous
   *          whether this function is to be executed asynchronously
   * @param volatile1
   *          whether this function is volatile
   * @param macroEquivalent
   *          whether this function is macro-equivalent and so can receive non-value references
   * @param multiThreadSafe
   *          whether this function is declared as being multi-thread safe
   * @param resultType
   *          whether this function should simplify its results to a base Excel type if possible
   */
  private FunctionAttributes(final FunctionType functionType, final boolean asynchronous, final boolean volatile1,
      final boolean macroEquivalent, final boolean multiThreadSafe, final TypeConversionMode resultType) {
    _functionType = functionType;
    _asynchronous = asynchronous;
    _volatile = volatile1;
    _macroEquivalent = macroEquivalent;
    _multiThreadSafe = multiThreadSafe;
    _resultType = resultType;
  }

  /**
   * Public static factory method for creating an instance of FunctionAttributes.
   *
   * @param functionType
   *          whether this function is a function or command, not null
   * @param asynchronous
   *          whether this function is to be executed asynchronously
   * @param volatile1
   *          whether this function is volatile
   * @param macroEquivalent
   *          whether this function is macro-equivalent and so can receive non-value references
   * @param multiThreadSafe
   *          whether this function is declared as being multi-thread safe
   * @param resultType
   *          whether this function should simplify it's results to a base Excel type if possible, not null
   * @return an instance
   */
  public static FunctionAttributes of(final FunctionType functionType, final boolean asynchronous, final boolean volatile1,
      final boolean macroEquivalent, final boolean multiThreadSafe, final TypeConversionMode resultType) {
    ArgumentChecker.notNull(functionType, "functionType");
    ArgumentChecker.notNull(resultType, "resultType");
    return new FunctionAttributes(functionType, asynchronous, volatile1, macroEquivalent, multiThreadSafe, resultType);
  }

  /**
   * @return the functionType
   */
  public FunctionType getFunctionType() {
    return _functionType;
  }

  /**
   * @return the asynchronous
   */
  public boolean isAsynchronous() {
    return _asynchronous;
  }

  /**
   * @return the volatile
   */
  public boolean isVolatile() {
    return _volatile;
  }

  /**
   * @return the macroEquivalent
   */
  public boolean isMacroEquivalent() {
    return _macroEquivalent;
  }

  /**
   * @return the multiThreadSafe
   */
  public boolean isMultiThreadSafe() {
    return _multiThreadSafe;
  }

  /**
   * @return the resultType
   */
  public TypeConversionMode getResultType() {
    return _resultType;
  }
}
