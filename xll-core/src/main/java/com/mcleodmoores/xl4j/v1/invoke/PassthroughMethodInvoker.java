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
import com.mcleodmoores.xl4j.v1.api.values.XLMissing;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * A class holding the converters required to convert arguments into the appropriate types and convert the result.
 */
public class PassthroughMethodInvoker implements MethodInvoker {
  private static final Logger LOGGER = LoggerFactory.getLogger(PassthroughMethodInvoker.class);
  private final Method _method;

  /**
   * Constructor.
   *
   * @param method
   *          the method to call.
   */
  public PassthroughMethodInvoker(final Method method) {
    _method = ArgumentChecker.notNull(method, "method");
  }

  @Override
  public XLValue invoke(final Object object, final XLValue[] arguments) {
    ArgumentChecker.notNull(arguments, "arguments");
    try {
      if (_method.isVarArgs()) {
        if (arguments.length == 0) {
          // find the appropriate type for the empty array
          final Class<?>[] parameterTypes = _method.getParameterTypes();
          final Class<?> varArgType = parameterTypes[parameterTypes.length - 1].getComponentType();
          if (varArgType == null) {
            LOGGER.error("Last argument for varargs method was not an array: should never happen");
            throw new XL4JRuntimeException("Error invoking method: last argument for varargs method was not an array");
          }
          return (XLValue) _method.invoke(object, Array.newInstance(varArgType, 0));
        }
        // create an array for the varargs argument
        final int nArgs = _method.getParameterCount();
        final int nVarargInputs = arguments.length - nArgs + 1;
        if (nVarargInputs < 0) {
          throw new XL4JRuntimeException("Wrong number of arguments for " + _method + ", have " + Arrays.toString(arguments));
        }
        final Object[] args = new Object[nArgs];
        final Class<?>[] parameterTypes = _method.getParameterTypes();
        final Class<?> varArgType = parameterTypes[parameterTypes.length - 1].getComponentType();
        final Object[] varargs = (Object[]) Array.newInstance(varArgType, nVarargInputs);
        System.arraycopy(arguments, 0, args, 0, nArgs - 1);
        System.arraycopy(arguments, nArgs - 1, varargs, 0, nVarargInputs);
        args[args.length - 1] = varargs;
        LOGGER.info("invoking method {} on {} with {}", _method.getName(), object, Arrays.toString(args));
        final XLValue result = (XLValue) _method.invoke(object, args);
        if (result == null) {
          // void method
          return XLMissing.INSTANCE;
        }
        return result;
      }
      LOGGER.info("invoking method {} on {} with {}", _method.getName(), object, Arrays.toString(arguments));
      final XLValue result = (XLValue) _method.invoke(object, (Object[]) arguments);
      if (result == null) {
        // void method
        return XLMissing.INSTANCE;
      }
      return result;
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new XL4JRuntimeException("Error invoking method", e);
    }
  }

  @Override
  public Class<?>[] getExcelParameterTypes() {
    return _method.getParameterTypes();
  }

  @Override
  public Class<?> getExcelReturnType() {
    return _method.getReturnType();
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

  @Override
  public Type getMethodReturnType() {
    return _method.getGenericReturnType();
  }
}
