/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.invoke;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.v1.api.core.MethodInvoker;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLMissing;
import com.mcleodmoores.xl4j.v1.api.values.XLNil;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * A class holding the converters required to convert arguments into the appropriate types and convert the result.
 */
public abstract class AbstractMethodInvoker implements MethodInvoker {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMethodInvoker.class);
  private final Method _method;
  private final TypeConverter[] _argumentConverters;
  private final TypeConverter _returnConverter;
  private final TypeConverter _objectXlObjectConverter;

  /**
   * Constructor.
   *
   * @param method
   *          the method to call.
   * @param argumentConverters
   *          the converters required to call the method
   * @param returnConverter
   *          the converter required to convert the result back to an Excel type
   * @param objectXlObjectConverter
   *          converts XLObject to Object
   */
  public AbstractMethodInvoker(final Method method, final TypeConverter[] argumentConverters, final TypeConverter returnConverter,
      final TypeConverter objectXlObjectConverter) {
    _method = ArgumentChecker.notNull(method, "method");
    _argumentConverters = ArgumentChecker.notNull(argumentConverters, "argumentConverters");
    _returnConverter = ArgumentChecker.notNull(returnConverter, "returnConverter");
    _objectXlObjectConverter = ArgumentChecker.notNull(objectXlObjectConverter, "objectXlObjectConverter");
  }

  @Override
  public XLValue invoke(final Object object, final XLValue[] arguments) {
    ArgumentChecker.notNull(arguments, "arguments");
    Object[] args;
    try {
      if (_method.isVarArgs()) {
        if (arguments.length == 0) {
          try {
            // find the appropriate type for the empty array - needed for primitives
            final Class<?>[] parameterTypes = _method.getParameterTypes();
            final Class<?> varArgType = parameterTypes[parameterTypes.length - 1].getComponentType();
            if (varArgType == null) {
              LOGGER.error("Last argument for varargs method was not an array: should never happen");
              throw new XL4JRuntimeException("Error invoking method: last argument for varargs method was not an array");
            }
            LOGGER.info("invoking method {} on {} with empty array", _method, object == null ? "null" : object.getClass().getSimpleName());
            final Object result = _method.invoke(object, Array.newInstance(varArgType, 0));
            return convertResult(result, _returnConverter);
          } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new XL4JRuntimeException("Error invoking method", e);
          }
        }
        args = new Object[_method.getParameterCount()];
        final int varArgIndex = _method.getParameterCount() - 1;
        final int nVarArgs = arguments.length - varArgIndex;
        if (nVarArgs < 0) {
          throw new XL4JRuntimeException("Wrong number of arguments for " + _method + ", have " + Arrays.toString(arguments));
        }
        for (int i = 0; i < varArgIndex; i++) {
          final Type expectedClass = _method.getGenericParameterTypes()[i];
          // handle the case where nothing is passed and this should be converted to a null
          // which happens unless the method is expecting an XLValue.
          if (arguments[i] instanceof XLNil || arguments[i] instanceof XLMissing && !expectedClass.getClass().isAssignableFrom(XLValue.class)) {
            args[i] = null;
          } else {
            args[i] = _argumentConverters[i].toJavaObject(expectedClass, arguments[i]);
          }
        }
        final XLValue[] varArgs = new XLValue[nVarArgs];
        System.arraycopy(arguments, varArgIndex, varArgs, 0, nVarArgs);
        final XLValue[][] varArgsAsArray = new XLValue[][] { varArgs };
        final Type expectedClass = _method.getGenericParameterTypes()[varArgIndex];
        args[args.length - 1] = _argumentConverters[_argumentConverters.length - 1].toJavaObject(expectedClass, XLArray.of(varArgsAsArray));
      } else {
        args = new Object[arguments.length];
        for (int i = 0; i < _argumentConverters.length; i++) {
          final Type expectedClass = _method.getGenericParameterTypes()[i];
          // handle the case where nothing is passed and this should be converted to a null
          // which happens unless the method is expecting an XLValue.
          if (arguments[i] instanceof XLNil || arguments[i] instanceof XLMissing && !expectedClass.getClass().isAssignableFrom(XLValue.class)) {
            args[i] = null;
          } else {
            args[i] = _argumentConverters[i].toJavaObject(expectedClass, arguments[i]);
          }
        }
      }
      try {
        LOGGER.info("invoking method {} on {}", _method, object == null ? "null" : object.getClass().getSimpleName());
        final Object result = _method.invoke(object, args);
        if (result == null) {
          // void method
          return XLMissing.INSTANCE;
        }
        return convertResult(result, _returnConverter);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new XL4JRuntimeException("Error invoking method", e);
      }
    } catch (final ClassCastException e) {
      // TODO this is a workaround that gets the object from the heap rather than trying to perform the conversion on XLObject
      // in a converter that does not expect that type. This can happen when a XLFunction uses TypeConversionMode.OBJECT_RESULT
      if (_method.isVarArgs()) {
        args = new Object[_method.getParameterCount()];
        final int varArgIndex = _method.getParameterCount() - 1;
        final int nVarArgs = arguments.length - varArgIndex;
        for (int i = 0; i < varArgIndex; i++) {
          final Type expectedClass = _method.getGenericParameterTypes()[i];
          if (arguments[i] instanceof XLObject && !expectedClass.equals(XLObject.class)) {
            args[i] = _objectXlObjectConverter.toJavaObject(Object.class, arguments[i]);
          } else {
            args[i] = _argumentConverters[i].toJavaObject(expectedClass, arguments[i]);
          }
        }
        final XLValue[] varArgs = new XLValue[nVarArgs];
        System.arraycopy(arguments, varArgIndex, varArgs, 0, nVarArgs);
        final XLValue[][] varArgsAsArray = new XLValue[][] { varArgs };
        final Type expectedClass = _method.getGenericParameterTypes()[varArgIndex];
        args[args.length - 1] = _argumentConverters[_argumentConverters.length - 1].toJavaObject(expectedClass, XLArray.of(varArgsAsArray));
      } else {
        args = new Object[arguments.length];
        for (int i = 0; i < _argumentConverters.length; i++) {
          final Type expectedClass = _method.getGenericParameterTypes()[i];
          if (arguments[i] instanceof XLObject && !expectedClass.equals(XLObject.class)) {
            args[i] = _objectXlObjectConverter.toJavaObject(Object.class, arguments[i]);
          } else {
            args[i] = _argumentConverters[i].toJavaObject(expectedClass, arguments[i]);
          }
        }
      }
      try {
        LOGGER.info("invoking method {} on {}", _method, object == null ? "null" : object.getClass().getSimpleName());
        final Object result = _method.invoke(object, args);
        if (result == null) {
          // void method
          return XLMissing.INSTANCE;
        }
        return convertResult(result, _returnConverter);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
        throw new XL4JRuntimeException("Error invoking method", e1);
      }
    }
  }

  /**
   * Processes the result object into the object returned by excel. Could be an object or an Excel type.
   *
   * @param object
   *          the result object to process
   * @param returnConverter
   *          the simplifying return converter
   * @return an XLValue type
   */
  protected abstract XLValue convertResult(Object object, TypeConverter returnConverter);

  @Override
  public Class<?>[] getExcelParameterTypes() {
    final Class<?>[] parameterTypes = new Class[_argumentConverters.length];
    int i = 0;
    for (final TypeConverter typeConverter : _argumentConverters) {
      parameterTypes[i++] = typeConverter.getJavaToExcelTypeMapping().getExcelClass();
    }
    return parameterTypes;
  }

  @Override
  public Class<?> getExcelReturnType() {
    return _returnConverter.getJavaToExcelTypeMapping().getExcelClass();
  }

  @Override
  public Type getMethodReturnType() {
    return _method.getGenericReturnType();
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
