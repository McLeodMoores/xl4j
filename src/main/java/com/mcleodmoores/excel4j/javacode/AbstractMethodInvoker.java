package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * A class holding the converters required to convert arguments into the appropriate types and convert the result. 
 */
public abstract class AbstractMethodInvoker implements MethodInvoker {
  private Method _method;
  private TypeConverter[] _argumentConverters;
  private TypeConverter _returnConverter;

  /**
   * Constructor.
   * @param method  the method to call.
   * @param argumentConverters  the converters required to call the method
   * @param returnConverter  the converter required to convert he result back to an Excel type
   */
  public AbstractMethodInvoker(final Method method, final TypeConverter[] argumentConverters, 
                       final TypeConverter returnConverter) {
    _method = method;
    _argumentConverters = argumentConverters;
    _returnConverter = returnConverter;
  }
  
  @Override
  public XLValue invoke(final Object object, final XLValue[] arguments) {
    Object[] args = new Object[arguments.length];
    for (int i = 0; i < _argumentConverters.length; i++) {
      args[i] = _argumentConverters[i].toJavaObject(null, arguments[i]);
    }
    try {
      Object result = _method.invoke(object, args);
      return convertResult(result, _returnConverter);
    } catch (IllegalAccessException | IllegalArgumentException
        | InvocationTargetException e) {
      throw new Excel4JRuntimeException("Error invoking method", e);
    }
  }
  
  /**
   * Processes the result object into the object returned by excel.  Could be an object or an Excel type.
   * @param object  the result object to process
   * @param returnConverter  the simplifying return converter
   * @return an XLValue type
   */
  protected abstract XLValue convertResult(final Object object, final TypeConverter returnConverter);
  
  @Override
  public Class<? extends XLValue>[] getExcelParameterTypes() {
    @SuppressWarnings("unchecked")
    Class<? extends XLValue>[] parameterTypes = new Class[_argumentConverters.length];
    int i = 0;
    for (TypeConverter typeConverter : _argumentConverters) {
      parameterTypes[i] = typeConverter.getJavaToExcelTypeMapping().getExcelClass(); 
    }
    return parameterTypes;
  }
  
  @Override
  public Class<? extends XLValue> getExcelReturnType() {
    return _returnConverter.getJavaToExcelTypeMapping().getExcelClass();
  }
  
  @Override
  public boolean isStatic() {
    return Modifier.isStatic(_method.getModifiers());
  }
  
  @Override
  public String getMethodName() {
    return _method.getName();
  }
  
  @Override
  public Class<?> getMethodDeclaringClass() {
    return _method.getDeclaringClass();
  }
}
