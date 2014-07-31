/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.mock;

import com.mcleodmoores.excel4j.XLFunctionType;
import com.mcleodmoores.excel4j.XLResultType;

/**
 * Class to hold some meta-data about a worksheet function/macro.
 */
public final class FunctionAttributes {
  private XLFunctionType _functionType;
  private boolean _asynchronous;
  private boolean _volatile;
  private boolean _macroEquivalent;
  private boolean _multiThreadSafe;
  private XLResultType _resultType;
  
  /**
   * Private constructor.
   * @param functionType  whether this function is a function or command
   * @param asynchronous  whether this function is to be executed asynchronously
   * @param volatile1  whether this function is volatile
   * @param macroEquivalent  whether this function is macro-equivalent and so can receive non-value references
   * @param multiThreadSafe  whether this function is declared as being multi-thread safe
   * @param resultType  whether this function should simplify it's results to a base Excel type if possible
   */
  private FunctionAttributes(final XLFunctionType functionType, final boolean asynchronous, final boolean volatile1, 
      final boolean macroEquivalent, final boolean multiThreadSafe, final XLResultType resultType) {
    super();
    _functionType = functionType;
    _asynchronous = asynchronous;
    _volatile = volatile1;
    _macroEquivalent = macroEquivalent;
    _multiThreadSafe = multiThreadSafe;
    _resultType = resultType;
  }
  
  /**
   * Public static factory method for creating an instance of FunctionAttributes.
   * @param functionType  whether this function is a function or command
   * @param asynchronous  whether this function is to be executed asynchronously
   * @param volatile1  whether this function is volatile
   * @param macroEquivalent  whether this function is macro-equivalent and so can receive non-value references
   * @param multiThreadSafe  whether this function is declared as being multi-thread safe
   * @param resultType  whether this function should simplify it's results to a base Excel type if possible
   * @return an instance
   */
  public static FunctionAttributes of(final XLFunctionType functionType, final boolean asynchronous, final boolean volatile1, 
      final boolean macroEquivalent, final boolean multiThreadSafe, final XLResultType resultType) {
    return new FunctionAttributes(functionType, asynchronous, volatile1, macroEquivalent, multiThreadSafe, resultType);
  }
  /**
   * @return the functionType
   */
  public XLFunctionType getFunctionType() {
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
  public XLResultType getResultType() {
    return _resultType;
  }
}
