package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * A class holding the converters required to convert arguments into the appropriate types and convert the result.
 */
public class PassthroughConstructorInvoker implements ConstructorInvoker {
  private final Constructor<?> _constructor;

  /**
   * Constructor.
   * @param constructor the constructor to call.
   */
  public PassthroughConstructorInvoker(final Constructor<?> constructor) {
    _constructor = constructor;
  }

  /**
   * Actually execute a method, performing the necessary type conversions.
   * @param arguments the arguments to pass to the method
   * @return the value to return to Excel
   */
  @Override
  public XLValue newInstance(final XLValue[] arguments) {
    try {
      return (XLValue) _constructor.newInstance(new Object[] { arguments });
     } catch (IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | InstantiationException e) {
      throw new Excel4JRuntimeException("Error invoking constructor", e);
    }
  }

  /**
   * @return an array containing the Excel class of each parameter to this constructor
   */
  @Override
  public Class<?>[] getExcelParameterTypes() {
    return _constructor.getParameterTypes();
  }

  /**
   * @return the Excel class returned by this constructor (should be XLObject)
   */
  @Override
  public Class<?> getExcelReturnType() {
    return _constructor.getDeclaringClass();
  }

  @Override
  public boolean isVarArgs() {
    return _constructor.isVarArgs();
  }

  @Override
  public Class<?> getDeclaringClass() {
    return _constructor.getDeclaringClass();
  }
}
