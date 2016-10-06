package com.mcleodmoores.excel4j.javacode;

import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Common interface for invoker of constructors.
 */
public interface ConstructorInvoker {

  /**
   * Actually execute a method, performing the necessary type conversions.
   * @param arguments the arguments to pass to the method
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

  boolean isVarArgs();

  Class<?> getDeclaringClass();
}