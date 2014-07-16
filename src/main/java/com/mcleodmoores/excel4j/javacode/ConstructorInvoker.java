package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * A class holding the converters required to convert arguments into the appropriate types and convert the result. 
 */
public class ConstructorInvoker {
  private Constructor<?> _constructor;
  private TypeConverter[] _argumentConverters;
  private TypeConverter _returnConverter;

  /**
   * Constructor.
   * @param constructor the constructor to call.
   * @param argumentConverters the converters required to call the method
   * @param returnConverter the converter required to convert he result back to an Excel type
   */
  public ConstructorInvoker(final Constructor<?> constructor, final TypeConverter[] argumentConverters, 
                                   final TypeConverter returnConverter) {
    _constructor = constructor;
    _argumentConverters = argumentConverters;
    _returnConverter = returnConverter;
  }
  
  /**
   * Actually execute a method, performing the necessary type conversions.
   * @param arguments the arguments to pass to the method
   * @return the value to return to Excel
   */
  public XLValue invoke(final XLValue[] arguments) {
    Object[] args = new Object[arguments.length];
    for (int i = 0; i < _argumentConverters.length; i++) {
      args[i] = _argumentConverters[i].toJavaObject(null, arguments[i]);
    }
    try {
      Object result = _constructor.newInstance(args);
      return _returnConverter.toXLValue(null, result);
    } catch (IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | InstantiationException e) {
      throw new Excel4JRuntimeException("Error invoking constructor", e);
    }
  }
  
}
