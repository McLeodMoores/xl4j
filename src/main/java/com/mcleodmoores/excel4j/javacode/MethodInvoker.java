/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import com.mcleodmoores.excel4j.values.XLValue;

/**
 * 
 */
public interface MethodInvoker {

  /**
   * Actually execute a method, performing the necessary type conversions.
   * @param object  the object on which to execute the method, or null if static
   * @param arguments  the arguments to pass to the method
   * @return the value to return to Excel
   */
  XLValue invoke(Object object, XLValue[] arguments);

  /**
   * @return an array containing the Excel class of each parameter to this method
   */
  Class<? extends XLValue>[] getExcelParameterTypes();

  /**
   * @return the Excel class returned by this method
   */
  Class<? extends XLValue> getExcelReturnType();

  /**
   * @return true, if the underlying method is static
   */
  boolean isStatic();

  /**
   * @return the method name
   */
  String getMethodName();

  /**
   * @return the Class the method is declared in
   */
  Class<?> getMethodDeclaringClass();

}