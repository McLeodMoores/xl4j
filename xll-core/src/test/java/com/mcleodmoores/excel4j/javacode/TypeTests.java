/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 *
 */
public class TypeTests {
  public static void paramTest(final XLString normal, final Class<? extends XLValue> clazz, final Collection<String>[] genericArray) {

  }

  @Test
  public void testArrayTypes() throws NoSuchMethodException, SecurityException {
    final Method[] methods = this.getClass().getMethods();
    for (final Method method : methods) {
      if (method.getName().equals("paramTest")) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        for (final Class<?> parameterType : parameterTypes) {
          System.err.println(parameterType);
        }

      }
      System.err.println();
    }

    final Method method = this.getClass().getMethod("paramTest", XLString.class, Class.class, Collection[].class);
    final Type[] genericParameterTypes = method.getGenericParameterTypes();
    Assert.assertTrue(genericParameterTypes[0] instanceof Class);
    Assert.assertTrue(genericParameterTypes[1] instanceof ParameterizedType);
    final ParameterizedType parameterizedType = (ParameterizedType) genericParameterTypes[1];
    System.err.println(parameterizedType.getRawType());
    Assert.assertTrue(genericParameterTypes[2] instanceof GenericArrayType);
    final GenericArrayType genericArrayType = (GenericArrayType) genericParameterTypes[2];
    System.err.println(genericArrayType.getGenericComponentType());
    final Class<?> class1 = Array.newInstance((Class<?>) ((ParameterizedType) genericArrayType.getGenericComponentType()).getRawType(), 0).getClass();
    System.err.println(class1);
  }
}
