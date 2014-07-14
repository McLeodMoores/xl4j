package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * A class holding the converters required to convert arguments into the appropriate types and convert the result. 
 */
public class MethodInvoker {
  private Method _method;
  private TypeConverter[] _argumentConverters;
  private TypeConverter _returnConverter;

  /**
   * Constructor.
   * @param method the method to call.
   * @param argumentConverters the converters required to call the method
   * @param returnConverter the converter required to convert he result back to an Excel type
   */
  public MethodInvoker(final Method method, final TypeConverter[] argumentConverters, 
                       final TypeConverter returnConverter) {
    _method = method;
    _argumentConverters = argumentConverters;
    _returnConverter = returnConverter;
  }
  
  /**
   * Actually execute a method, performing the necessary type conversions.
   * @param object the object on which to execute the method, or null if static
   * @param arguments the arguments to pass to the method
   * @return the value to return to Excel
   */
  public XLValue invoke(final Object object, final XLValue[] arguments) {
    Object[] args = new Object[arguments.length];
    for (int i = 0; i < _argumentConverters.length; i++) {
      args[i] = _argumentConverters[i].toJavaObject(arguments[i]);
    }
    try {
      Object result = _method.invoke(object, args);
      return _returnConverter.toXLValue(result);
    } catch (IllegalAccessException | IllegalArgumentException
        | InvocationTargetException e) {
      throw new Excel4JRuntimeException("Error invoking method", e);
    }
  }
  
}
