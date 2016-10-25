/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.javacode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLMissing;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * A class holding the converters required to convert arguments into the appropriate types and convert the result.
 */
public class ObjectConstructorInvoker implements ConstructorInvoker {
  private final Constructor<?> _constructor;
  private final TypeConverter[] _argumentConverters;
  private final TypeConverter _returnConverter;

  /**
   * Constructor.
   *
   * @param constructor
   *          the constructor to call.
   * @param argumentConverters
   *          the converters required to call the method
   * @param returnConverter
   *          the converter required to convert he result back to an Excel type
   */
  public ObjectConstructorInvoker(final Constructor<?> constructor, final TypeConverter[] argumentConverters,
      final TypeConverter returnConverter) {
    _constructor = constructor;
    _argumentConverters = argumentConverters;
    _returnConverter = returnConverter;
  }

  @Override
  public XLValue newInstance(final XLValue[] arguments) {
    Object[] args;
    if (_constructor.isVarArgs()) {
      args = new Object[_constructor.getParameterCount()];
      final int varArgIndex = _constructor.getParameterCount() - 1;
      final int nVarArgs = arguments.length - varArgIndex;
      for (int i = 0; i < varArgIndex; i++) {
        // TODO not sure about this logic - isArray() never seems to be true.
        // what was the intended use?
        if (arguments[i].getClass().isArray()) {
          args[i] = arguments[i];
        } else {
          final Type expectedClass = _constructor.getParameterTypes()[i];
          // handle the case where nothing is passed and this should be converted to a null
          // which happens unless the method is expecting an XLValue.
          if ((arguments[i] instanceof XLMissing) && 
              (!expectedClass.getClass().isAssignableFrom(XLValue.class))) {
            args[i] = null;
          } else {
            args[i] = _argumentConverters[i].toJavaObject(expectedClass, arguments[i]);
          }
        }
      }
      final XLValue[] varArgs = new XLValue[nVarArgs];
      System.arraycopy(arguments, varArgIndex, varArgs, 0, nVarArgs);
      final XLValue[][] varArgsAsArray = new XLValue[][] { varArgs };
      final Type expectedClass = _constructor.getParameterTypes()[varArgIndex];
      args[args.length - 1] = _argumentConverters[_argumentConverters.length - 1].toJavaObject(expectedClass, XLArray.of(varArgsAsArray));
    } else {
      args = new Object[arguments.length];
      for (int i = 0; i < _argumentConverters.length; i++) {
        // TODO not sure about this logic - isArray() never seems to be true.
        // what was the intended use?
        if (arguments[i].getClass().isArray()) {
          args[i] = arguments[i];
        } else {
          final Type expectedClass = _constructor.getParameterTypes()[i];
          // handle the case where nothing is passed and this should be converted to a null
          // which happens unless the method is expecting an XLValue.
          if ((arguments[i] instanceof XLMissing) && 
              (!expectedClass.getClass().isAssignableFrom(XLValue.class))) {
            args[i] = null;
          } else {
            args[i] = _argumentConverters[i].toJavaObject(expectedClass, arguments[i]);
          }
        }
      }
    }
    try {
      final Object result = _constructor.newInstance(args);
      if (result.getClass().isArray()) {
        throw new Excel4JRuntimeException("Return of array types not supported");
      }
      return (XLValue) _returnConverter.toXLValue(null, result);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
      throw new Excel4JRuntimeException("Error invoking constructor: " + e.getMessage(), e);
    }
  }

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
  public boolean isVarArgs() {
    return _constructor.isVarArgs();
  }

  @Override
  public Class<?> getDeclaringClass() {
    return _constructor.getDeclaringClass();
  }
}
