package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * A class holding the converters required to convert arguments into the appropriate types and convert the result. 
 */
public abstract class AbstractMethodInvoker implements MethodInvoker {
  private static Logger s_logger = LoggerFactory.getLogger(AbstractMethodInvoker.class);
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
    // note that the seemingly obvious invariant of arguments.length == _argumentConverters.length is not
    // always true because of a VarArgs might have no arguments to it's converter may be surplus to 
    // requirements.  For this reason we base the conversion on the length of arguments.
    
    Object[] args = new Object[_argumentConverters.length];
    if (isVarArgs()) {
      if (arguments.length < _argumentConverters.length) { // var args is empty
        for (int i = 0; i < arguments.length; i++) {
          args[i] = _argumentConverters[i].toJavaObject(null, arguments[i]);
        }
        args[_argumentConverters.length - 1] = createVarArgsArray(_method, 0); // empty varargs to pass on
      } else { // args args non-empty
        for (int i = 0; i < _argumentConverters.length - 1; i++) {  // last arg converter used for var args.
          args[i] = _argumentConverters[i].toJavaObject(null, arguments[i]);
        }
        Object[] varArgs = createVarArgsArray(_method, args.length - (_argumentConverters.length - 1)); // so if converters == args, we have 1.
        for (int i = 0; i < varArgs.length; i++) {
          varArgs[i] = _argumentConverters[_argumentConverters.length - 1].toJavaObject(null, arguments[i + (_argumentConverters.length - 1)]);
        }
        args[_argumentConverters.length - 1] = varArgs; // non-empty varargs to pass on
      }
    } else {
      for (int i = 0; i < arguments.length; i++) { 
        args[i] = _argumentConverters[i].toJavaObject(null, arguments[i]);
      }
    }
    try {
      s_logger.info("invoking " + object + " with " + Arrays.toString(args));
      Object result = _method.invoke(object, args);
      return convertResult(result, _returnConverter);
    } catch (IllegalAccessException | IllegalArgumentException
        | InvocationTargetException e) {
      throw new Excel4JRuntimeException("Error invoking method", e);
    }
  }
  
  private Object[] createVarArgsArray(final Method method, final int size) {
    Class<?>[] parameterTypes = method.getParameterTypes();
    return (Object[]) Array.newInstance(parameterTypes[parameterTypes.length - 1].getComponentType(), size);
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
  public boolean isVarArgs() {
    return _method.isVarArgs();
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
