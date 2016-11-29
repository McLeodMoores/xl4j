/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.javacode;

import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Common interface for invoker of constructors.
 */
public interface ConstructorInvoker {

  /**
   * Create an instance of an object, performing the necessary type conversions.
   *
   * @param arguments
   *          the arguments to pass to the method
   * @return the value to return to Excel
   */
  XLValue newInstance(XLValue[] arguments);

  /**
   * @return an array containing the Excel class of each parameter to this constructor
   */
  Class<?>[] getExcelParameterTypes();

  /**
   * @return the Excel class returned by this constructor (should be XLObject)
   */
  Class<?> getExcelReturnType();

  /**
   * @return true if the arguments to the constructors are varargs
   */
  boolean isVarArgs();

  /**
   * @return the declaring class
   */
  Class<?> getDeclaringClass();
}