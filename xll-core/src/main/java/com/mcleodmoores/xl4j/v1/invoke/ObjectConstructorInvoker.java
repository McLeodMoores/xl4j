/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.invoke;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.v1.api.core.ConstructorInvoker;
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
public class ObjectConstructorInvoker implements ConstructorInvoker {
  private static final Logger LOGGER = LoggerFactory.getLogger(ObjectConstructorInvoker.class);
  private final Constructor<?> _constructor;
  private final TypeConverter[] _argumentConverters;
  private final TypeConverter _returnConverter;
  private final TypeConverter _objectXlObjectConverter;

  /**
   * Constructor.
   *
   * @param constructor
   *          the constructor to call
   * @param argumentConverters
   *          the converters required to call the method
   * @param returnConverter
   *          the converter required to convert the result back to an Excel type
   * @param objectXlObjectConverter
   *          converts XLObject to Object
   */
  public ObjectConstructorInvoker(final Constructor<?> constructor, final TypeConverter[] argumentConverters,
      final TypeConverter returnConverter, final TypeConverter objectXlObjectConverter) {
    _constructor = ArgumentChecker.notNull(constructor, "constructor");
    _argumentConverters = ArgumentChecker.notNull(argumentConverters, "argumentConverters");
    _returnConverter = ArgumentChecker.notNull(returnConverter, "returnConverter");
    _objectXlObjectConverter = ArgumentChecker.notNull(objectXlObjectConverter, "objectXlObjectConverter");
  }

  @Override
  public XLValue newInstance(final XLValue[] arguments) {
    ArgumentChecker.notNull(arguments, "arguments");
    Object[] args;
    try {
      if (_constructor.isVarArgs()) {
        if (arguments.length == 0) {
          try {
            // find the appropriate type for the empty array
            final Class<?>[] parameterTypes = _constructor.getParameterTypes();
            final Class<?> varArgType = parameterTypes[parameterTypes.length - 1].getComponentType();
            if (varArgType == null) {
              LOGGER.error("Last argument for varargs constructor was not an array: should never happen");
              throw new XL4JRuntimeException("Error invoking constructor: last argument for varargs was not an array");
            }
            LOGGER.info("invoking constructor on {} with empty array", _constructor);
            final Object result = _constructor.newInstance(Array.newInstance(varArgType, 0));
            return (XLValue) _returnConverter.toXLValue(result);
          } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new XL4JRuntimeException("", e);
          }
        }
        args = new Object[_constructor.getParameterCount()];
        final int varArgIndex = _constructor.getParameterCount() - 1;
        final int nVarArgs = arguments.length - varArgIndex;
        if (nVarArgs < 0) {
          throw new XL4JRuntimeException("Wrong number of arguments for " + _constructor + ", have " + Arrays.toString(arguments));
        }
        for (int i = 0; i < varArgIndex; i++) {
          final Type expectedClass = _constructor.getParameterTypes()[i];
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
        final Type expectedClass = _constructor.getParameterTypes()[varArgIndex];
        args[args.length - 1] = _argumentConverters[_argumentConverters.length - 1].toJavaObject(expectedClass, XLArray.of(varArgsAsArray));
      } else {
        args = new Object[arguments.length];
        for (int i = 0; i < _argumentConverters.length; i++) {
          final Type expectedClass = _constructor.getParameterTypes()[i];
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
        final Object result = _constructor.newInstance(args);
        return (XLValue) _returnConverter.toXLValue(result);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
        throw new XL4JRuntimeException("Error invoking constructor: " + e.getMessage(), e);
      }
    } catch (final ClassCastException e) {
      // TODO this is a workaround that gets the object from the heap rather than trying to perform the conversion on XLObject
      // in a converter that does not expect that type. This can happen when a XLFunction uses TypeConversionMode.OBJECT_RESULT
      if (_constructor.isVarArgs()) {
        args = new Object[_constructor.getParameterCount()];
        final int varArgIndex = _constructor.getParameterCount() - 1;
        final int nVarArgs = arguments.length - varArgIndex;
        if (nVarArgs < 0) {
          throw new XL4JRuntimeException("Wrong number of arguments for " + _constructor + ", have " + Arrays.toString(arguments));
        }
        for (int i = 0; i < varArgIndex; i++) {
          final Type expectedClass = _constructor.getParameterTypes()[i];
          if (arguments[i] instanceof XLObject && !expectedClass.equals(XLObject.class)) {
            args[i] = _objectXlObjectConverter.toJavaObject(Object.class, arguments[i]);
          } else {
            args[i] = _argumentConverters[i].toJavaObject(expectedClass, arguments[i]);
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
          final Type expectedClass = _constructor.getParameterTypes()[i];
          if (arguments[i] instanceof XLObject && !expectedClass.equals(XLObject.class)) {
            args[i] = _objectXlObjectConverter.toJavaObject(Object.class, arguments[i]);
          } else {
            args[i] = _argumentConverters[i].toJavaObject(expectedClass, arguments[i]);
          }
        }
      }
      try {
        final Object result = _constructor.newInstance(args);
        return (XLValue) _returnConverter.toXLValue(result);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e1) {
        throw new XL4JRuntimeException("Error invoking constructor: " + e.getMessage(), e1);
      }
    }
  }

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
  public boolean isVarArgs() {
    return _constructor.isVarArgs();
  }

  @Override
  public Class<?> getDeclaringClass() {
    return _constructor.getDeclaringClass();
  }
}
