package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.mcleodmoores.excel4j.typeconvert.AbstractScalarTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * A class holding the converters required to convert arguments into the appropriate types and convert the result. 
 */
public class ConstructorInvoker {
  private Constructor<?> _constructor;
  private TypeConverter<?, ?, ?>[] _argumentConverters;
  private TypeConverter<?, ?, ?> _returnConverter;
  
  /**
   * Constructor.
   * @param constructor the constructor to call.
   * @param argumentConverters the converters required to call the method
   * @param returnConverter the converter required to convert he result back to an Excel type
   */
  public ConstructorInvoker(final Constructor<?> constructor, final TypeConverter<?, ?, ?>[] argumentConverters, 
                            final TypeConverter<?, ?, ?> returnConverter) {
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
      if (arguments[i].getClass().isArray()) {
        args[i] = arguments[i];
      } else {
        AbstractScalarTypeConverter scalarTypeConverter = (AbstractScalarTypeConverter) _argumentConverters[i];
        args[i] = scalarTypeConverter.toJavaObject(null, arguments[i]);
      }
    }
    try {
      Object result = _constructor.newInstance(args);
      if (result.getClass().isArray()) {
        throw new Excel4JRuntimeException("Return of array types not supported");
      } else {
        AbstractScalarTypeConverter scalarTypeConverter = (AbstractScalarTypeConverter) _returnConverter;
        return scalarTypeConverter.toXLValue(null, result);
      }
     } catch (IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | InstantiationException e) {
      throw new Excel4JRuntimeException("Error invoking constructor", e);
    }
  }
  
  /**
   * @return an array containing the Excel class of each parameter to this constructor
   */
  public Class<?>[] getExcelParameterTypes() {
    Class<?>[] parameterTypes = new Class[_argumentConverters.length];
    int i = 0;
    for (TypeConverter<?, ?, ?> typeConverter : _argumentConverters) {
      parameterTypes[i] = typeConverter.getJavaToExcelTypeMapping().getExcelClass(); 
    }
    return parameterTypes;
  }
  
  /**
   * @return the Excel class returned by this constructor (should be XLObject)
   */
  public Class<?> getExcelReturnType() {
    return _returnConverter.getJavaToExcelTypeMapping().getExcelClass();
  }
  
}
