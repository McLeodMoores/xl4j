package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * A class holding the converters required to convert arguments into the appropriate types and convert the result. 
 */
public class MethodTypeConverters {
  private Method _method;
  private TypeConverter[] _argumentConverters;
  private TypeConverter _returnConverter;
  private Type[] _parameterTypes;
  private Type _returnType;
  private Class<XLValue> _excelResultType;

  /**
   * Constructor.
   * @param method the method to call.
   * @param argumentConverters the converters required to call the method
   * @param returnConverter the converter required to convert he result back to an Excel type
   * @param excelResultType the type to convert the result to
   */
  public MethodTypeConverters(final Method method, final TypeConverter[] argumentConverters, 
                              final TypeConverter returnConverter, final Class<XLValue> excelResultType) {
    _method = method;
    _argumentConverters = argumentConverters;
    _returnConverter = returnConverter;
    _excelResultType = excelResultType;
    _parameterTypes = method.getGenericParameterTypes();
    _returnType = method.getGenericReturnType();
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
      args[i] = _argumentConverters[i].toJavaObject(ExcelToJavaTypeMapping.of(_parameterTypes[i], arguments[i].getClass()), arguments[i]);
    }
    try {
      Object result = _method.invoke(object, args);
      return _returnConverter.toXLValue(JavaToExcelTypeMapping.of(_returnType, _excelResultType), result);
    } catch (IllegalAccessException | IllegalArgumentException
        | InvocationTargetException e) {
      throw new Excel4JRuntimeException("Error invoking method", e);
    }
  }
  
}
