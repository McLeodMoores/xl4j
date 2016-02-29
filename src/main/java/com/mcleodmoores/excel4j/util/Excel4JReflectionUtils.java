/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.typeconvert.ScanningTypeConverterRegistry;
import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.typeconvert.TypeConverterRegistry;

/**
 * Some common utility methods for reflection-based operations.
 * Called Excel4JReflectionUtils so it didn't clash with ReflectionUtils of the Reflections framework.
 */
public final class Excel4JReflectionUtils {
  private Excel4JReflectionUtils() {
  }
  /**
   * Reduce a generic Type to its Class erasure.
   * @param type the generic type
   * @return it's underlying Class
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
      throw new Excel4JRuntimeException("This shouldn't be reached.");
    }
  }

  @SuppressWarnings("unused")
  private static Class<?>[] getExpectedExcelTypes(final Excel excel, final Method method) {
    final Type[] genericParameterTypes = method.getGenericParameterTypes();
    final Class<?>[] excelTypes = new Class[genericParameterTypes.length];
    final TypeConverterRegistry typeConverterRegistry = new ScanningTypeConverterRegistry(excel);
    int i = 0;
    for (final Type parameterType : genericParameterTypes) {
      final TypeConverter converter = typeConverterRegistry.findConverter(parameterType);
      if (converter != null) {
        final Class<?> excelClass = converter.getJavaToExcelTypeMapping().getExcelClass();
        excelTypes[i] = excelClass;
      } else {
        throw new Excel4JRuntimeException("Can't find Java->Excel converter for parameter type " + parameterType + " (arg " + i + ") of method " + method);
      }
      i++;
    }
    return excelTypes;
  }
}
