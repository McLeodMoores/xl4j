/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.invoke;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import com.mcleodmoores.xl4j.v1.api.core.ConstructorInvoker;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * A class holding the converters required to convert arguments into the appropriate types and convert the result.
 */
public class PassthroughConstructorInvoker implements ConstructorInvoker {
  private final Constructor<?> _constructor;
  private final TypeConverter _objectXlObjectConverter;

  /**
   * Constructor.
   *
   * @param constructor
   *          the constructor to call
   * @param objectXlObjectConverter
   *          puts the constructed object on the heap
   */
  public PassthroughConstructorInvoker(final Constructor<?> constructor, final TypeConverter objectXlObjectConverter) {
    _constructor = ArgumentChecker.notNull(constructor, "constructor");
    _objectXlObjectConverter = ArgumentChecker.notNull(objectXlObjectConverter, "objectXlObjectConverter");
  }

  @Override
  public XLValue newInstance(final XLValue[] arguments) {
    ArgumentChecker.notNull(arguments, "arguments");
    try {
      if (_constructor.isVarArgs()) {
        if (arguments.length == 0) {
          // find the appropriate type for the empty array
          final Class<?>[] parameterTypes = _constructor.getParameterTypes();
          final Class<?> varArgType = parameterTypes[parameterTypes.length - 1].getComponentType();
          if (varArgType == null) {
            throw new XL4JRuntimeException("Error instantiating constructor: last argument for varargs method was not an array");
          }
          return (XLValue) _objectXlObjectConverter.toXLValue(_constructor.newInstance(Array.newInstance(varArgType, 0)));
        }
        // create an array for the varargs argument
        final Object[] args;
        final int nArgs = _constructor.getParameterCount();
        final int varargInputs = arguments.length - nArgs + 1;
        if (varargInputs < 0) {
          throw new XL4JRuntimeException("Wrong number of arguments for " + _constructor + ", have " + Arrays.toString(arguments));
        }
        args = new Object[nArgs];
        final Class<?>[] parameterTypes = _constructor.getParameterTypes();
        final Class<?> varArgType = parameterTypes[parameterTypes.length - 1].getComponentType();
        final Object[] varargs = (Object[]) Array.newInstance(varArgType, varargInputs);
        System.arraycopy(arguments, 0, args, 0, nArgs - 1);
        System.arraycopy(arguments, nArgs - 1, varargs, 0, varargInputs);
        args[args.length - 1] = varargs;
        return (XLValue) _objectXlObjectConverter.toXLValue(_constructor.newInstance(args));
      }
      return (XLValue) _objectXlObjectConverter.toXLValue(_constructor.newInstance((Object[]) arguments));
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
      throw new XL4JRuntimeException("Error instantiating constructor", e);
    }
  }

  /**
   * @return an array containing the Excel class of each parameter to this constructor
   */
  @Override
  public Class<?>[] getExcelParameterTypes() {
    return _constructor.getParameterTypes();
  }

  /**
   * @return the Excel class returned by this constructor (should be XLObject)
   */
  @Override
  public Class<?> getExcelReturnType() {
    return _objectXlObjectConverter.getJavaToExcelTypeMapping().getExcelClass();
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
