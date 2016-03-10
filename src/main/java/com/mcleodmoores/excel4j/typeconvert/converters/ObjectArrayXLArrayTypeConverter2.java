/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.util.Excel4JReflectionUtils;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLArray;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from arrays of Objects to Excel arrays and back again. This converter converts
 * every Java object to an XLObject. If the most specific type is required, {@link ObjectArrayXLArrayTypeConverter}
 * should be used, which is at a higher priority.
 */
public final class ObjectArrayXLArrayTypeConverter2 extends AbstractTypeConverter {
  /** The priority */
  private static final int PRIORITY = 6;
  /** The Excel context */
  private final Excel _excel;

  /**
   * Default constructor.
   * @param excel  the excel context object, used to access the type converter registry, not null
   */
  public ObjectArrayXLArrayTypeConverter2(final Excel excel) {
    super(Object[].class, XLArray.class, PRIORITY);
    ArgumentChecker.notNull(excel, "excel");
    _excel = excel;
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (!from.getClass().isArray()) {
      throw new Excel4JRuntimeException("\"from\" parameter must be an array");
    }
    Type componentType = null;
    if (expectedType instanceof Class) {
      final Class<?> expectedClass = from.getClass();
      componentType = expectedClass.getComponentType();
      // REVIEW: this will never fail because of the isArray() test
      if (componentType == null) {
        throw new Excel4JRuntimeException("component type of \"from\" parameter is null");
        //REVIEW: why was this commented out? it isn't in the other array tests
        //      } else if (expectedType instanceof GenericArrayType) {
        //        final GenericArrayType genericArrayType = (GenericArrayType) expectedType;
        //        componentType = genericArrayType.getGenericComponentType();
      }
    } else {
      throw new Excel4JRuntimeException("expectedType not array or GenericArrayType");
    }
    final Object[] fromArr = (Object[]) from;
    final XLValue[][] toArr = new XLValue[1][fromArr.length];
    final TypeConverter converter = _excel.getTypeConverterRegistry().findConverter(componentType);
    for (int i = 0; i < fromArr.length; i++) {
      final XLValue value = (XLValue) converter.toXLValue(componentType, fromArr[i]);
      toArr[0][i] = value;
    }
    return XLArray.of(toArr);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    final XLArray xlArr = (XLArray) from;
    Type componentType = null;
    if (expectedType instanceof Class) {
      final Class<?> expectedClass = (Class<?>) expectedType;
      componentType = expectedClass.getComponentType();
    } else if (expectedType instanceof GenericArrayType) {
      final GenericArrayType genericArrayType = (GenericArrayType) expectedType;
      componentType = genericArrayType.getGenericComponentType();
    } else {
      throw new Excel4JRuntimeException("expectedType not array or GenericArrayType");
    }

    final XLValue[][] arr = xlArr.getArray();
    TypeConverter lastConverter = null;
    Class<?> lastClass = null;
    final TypeConverterRegistry typeConverterRegistry = _excel.getTypeConverterRegistry();
    if (arr.length == 1) { // array is a single row
      final Object[] targetArr = (Object[]) Array.newInstance(Excel4JReflectionUtils.reduceToClass(componentType), arr[0].length);
      for (int i = 0; i < arr[0].length; i++) {
        final XLValue val = arr[0][i];
        // This is a rather weak attempt at optimizing converter lookup - other options seemed to have greater overhead.
        if (lastConverter == null || !val.getClass().equals(lastClass)) {
          lastClass = val.getClass();
          lastConverter = typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(lastClass, componentType));
        }
        if (lastConverter == null) {
          throw new Excel4JRuntimeException("Could not find type converter for " + lastClass + " using component type " + componentType);
        }
        targetArr[i] = lastConverter.toJavaObject(componentType, val);
      }
      return targetArr;
    }
    // array is single column
    final Object[] targetArr = (Object[]) Array.newInstance(Excel4JReflectionUtils.reduceToClass(componentType), arr.length);
    for (int i = 0; i < arr.length; i++) {
      final XLValue val = arr[i][0];
      // This is a rather weak attempt at optimizing converter lookup - other options seemed to have greater overhead.
      if (lastConverter == null || !val.getClass().equals(lastClass)) {
        lastClass = val.getClass();
        lastConverter = typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(lastClass, componentType));
      }
      if (lastConverter == null) {
        throw new Excel4JRuntimeException("Could not find type converter for " + lastClass + " using component type " + componentType);
      }
      targetArr[i] = lastConverter.toJavaObject(componentType, val);
    }
    return targetArr;
  }
}
