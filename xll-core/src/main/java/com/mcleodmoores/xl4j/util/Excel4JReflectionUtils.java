/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Some common utility methods for reflection-based operations. Called Excel4JReflectionUtils so it didn't clash with ReflectionUtils of the
 * Reflections framework.
 */
public final class Excel4JReflectionUtils {
  private Excel4JReflectionUtils() {
  }

  /**
   * Reduce a generic Type to its Class erasure.
   *
   * @param type
   *          the generic type
   * @return its underlying Class
   */
  public static Class<?> reduceToClass(final Type type) {
    if (type instanceof Class) {
      return (Class<?>) type;
    } else if (type instanceof ParameterizedType) {
      final ParameterizedType parameterizedType = (ParameterizedType) type;
      return reduceToClass(parameterizedType.getRawType());
    } else if (type instanceof GenericArrayType) {
      final GenericArrayType genericArrayType = (GenericArrayType) type;
      final Class<?> reducedComponentClass = reduceToClass(genericArrayType.getGenericComponentType());
      return Array.newInstance(reducedComponentClass, 0).getClass();
    } else if (type instanceof TypeVariable<?>) {
      return Object.class; // Don't know what else to do here?
    } else if (type instanceof WildcardType) {
      return Object.class; // Don't know what else to do here?
    } else {
      throw new Excel4JRuntimeException("This shouldn't be reached for type " + type);
    }
  }

}
