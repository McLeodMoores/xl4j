package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLArray;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * A class holding the converters required to convert arguments into the appropriate types and convert the result.
 */
public abstract class AbstractMethodInvoker implements MethodInvoker {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMethodInvoker.class);
  private final Method _method;
  private final TypeConverter[] _argumentConverters;
  private final TypeConverter _returnConverter;

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
    Object[] args;
    if (_method.isVarArgs()) {
      if (arguments.length == 0) {
        //TODO not sure if this is needed, but cannot currently get Arrays.asList() to work
        try {
          LOGGER.info("invoking method {} on {}", _method, object == null ? "null" : object.getClass().getSimpleName());
          final Object result = _method.invoke(object, (Object[]) null);
          return convertResult(result, _returnConverter);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
          e.printStackTrace();
          throw new Excel4JRuntimeException("Error invoking method", e);
        }
      }
      args = new Object[_method.getParameterCount()];
      final int varArgIndex = _method.getParameterCount() - 1;
      final int nVarArgs = arguments.length - varArgIndex;
      for (int i = 0; i < varArgIndex; i++) {
        //TODO not sure about this logic - isArray() never seems to be true.
        // what was the intended use?
        if (arguments[i].getClass().isArray()) {
          args[i] = arguments[i];
        } else {
          final Type expectedClass = _method.getParameterTypes()[i];
          args[i] = _argumentConverters[i].toJavaObject(expectedClass, arguments[i]);
        }
      }
      final XLValue[] varArgs = new XLValue[nVarArgs];
      System.arraycopy(arguments, varArgIndex, varArgs, 0, nVarArgs);
      final XLValue[][] varArgsAsArray = new XLValue[][] {varArgs};
      final Type expectedClass = _method.getParameterTypes()[varArgIndex];
      args[args.length - 1] = _argumentConverters[_argumentConverters.length - 1].toJavaObject(expectedClass, XLArray.of(varArgsAsArray));
    } else {
      args = new Object[arguments.length];
      for (int i = 0; i < _argumentConverters.length; i++) {
        //TODO not sure about this logic - isArray() never seems to be true.
        // what was the intended use?
        if (arguments[i].getClass().isArray()) {
          args[i] = arguments[i];
        } else {
          final Type expectedClass = _method.getParameterTypes()[i];
          args[i] = _argumentConverters[i].toJavaObject(expectedClass, arguments[i]);
        }
      }
    }
    try {
      LOGGER.info("invoking method {} on {} with {}", _method, object == null ? "null" : object.getClass().getSimpleName(), Arrays.toString(args));
      final Object result = _method.invoke(object, args);
      return convertResult(result, _returnConverter);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      e.printStackTrace();
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
  public Class<?>[] getExcelParameterTypes() {
    final Class<?>[] parameterTypes = new Class[_argumentConverters.length];
    final int i = 0;
    for (final TypeConverter typeConverter : _argumentConverters) {
      parameterTypes[i] = typeConverter.getJavaToExcelTypeMapping().getExcelClass();
    }
    return parameterTypes;
  }

  @Override
  public Class<?> getExcelReturnType() {
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
