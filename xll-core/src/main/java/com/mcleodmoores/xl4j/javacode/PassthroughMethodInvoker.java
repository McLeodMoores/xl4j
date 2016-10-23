/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.javacode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLValue;

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
    _method = method;
  }

  @Override
  public XLValue invoke(final Object object, final XLValue[] arguments) {
    // note that the seemingly obvious invariant of arguments.length == _argumentConverters.length is not
    // always true because of a VarArgs might have no arguments to its converter may be surplus to
    // requirements. For this reason we base the conversion on the length of arguments.

    try {
      LOGGER.info("invoking " + object + " with " + Arrays.toString(arguments));
      return (XLValue) _method.invoke(object, new Object[] { arguments });
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new Excel4JRuntimeException("Error invoking method", e);
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
}
