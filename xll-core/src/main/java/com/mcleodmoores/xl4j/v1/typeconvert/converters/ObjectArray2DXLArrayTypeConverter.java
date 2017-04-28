/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.ConverterUtils;
import com.mcleodmoores.xl4j.v1.util.XL4JReflectionUtils;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Type converter to convert from arrays of Object arrays to Excel arrays and back again. When converting back to Java, the most specific
 * converter that can be found is used (e.g. Boolean -&gt; XLBoolean, rather than an XLObject wrapping a boolean). This converter is higher
 * priority than {@link ObjectArray2DXLArrayTypeConverter2}, which only converts to XLObjects.
 * <p>
 * This class assumes that the input array from / to Excel is rectangular.
 */
public final class ObjectArray2DXLArrayTypeConverter extends AbstractTypeConverter {
  /** The Excel context */
  private final Excel _excel;
  /** The priority */
  private static final int PRIORITY = 11;
  /**
   * Default constructor.
   *
   * @param excel
   *          the excel context object, used to access the type converter registry, not null
   */
  public ObjectArray2DXLArrayTypeConverter(final Excel excel) {
    super(Object[][].class, XLArray.class, PRIORITY);
    ArgumentChecker.notNull(excel, "excel");
    _excel = excel;
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    final Object[][] fromArr = (Object[][]) from;
    if (fromArr.length == 0) {
      return XLArray.of(new XLValue[1][1]);
    }
    final Type componentType = from.getClass().getComponentType();
    // we know the length is > 0
    int maxColumns = fromArr[0].length;
    for (int i = 1; i < fromArr.length; i++) {
      if (maxColumns < fromArr[i].length) {
        maxColumns = fromArr[i].length;
      }
    }
    final XLValue[][] toArr = new XLValue[fromArr.length][maxColumns];
    TypeConverter lastConverter = null;
    Class<?> lastClass = null;
    final TypeConverterRegistry typeConverterRegistry = _excel.getTypeConverterRegistry();
    for (int i = 0; i < fromArr.length; i++) {
      for (int j = 0; j < fromArr[i].length; j++) {
        final Object obj = fromArr[i][j];
        if (lastConverter == null || !obj.getClass().equals(lastClass)) {
          lastClass = obj.getClass();
          lastConverter = typeConverterRegistry.findConverter(lastClass);
        }
        if (lastConverter == null) {
          // try with potentially less specific type
          lastConverter = typeConverterRegistry.findConverter(componentType);
        }
        if (lastConverter == null) {
          throw new XL4JRuntimeException("Could not find type converter for " + lastClass + " or component type " + componentType);
        }
        final XLValue value = (XLValue) lastConverter.toXLValue(obj);
        toArr[i][j] = value;
      }
    }
    return XLArray.of(toArr);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    Type componentType = null;
    if (expectedType instanceof Class) {
      // TODO making sure it's reduced to a non-array type is not nice here but otherwise the array creation produced a 3D array
      // not sure what the best way to deal with that is
      final Class<?> expectedClass = (Class<?>) expectedType;
      componentType = expectedClass.getComponentType();
      if (componentType instanceof Class && ((Class<?>) componentType).isArray()) {
        componentType = ((Class<?>) componentType).getComponentType();
      }
    } else if (expectedType instanceof GenericArrayType) {
      componentType = ConverterUtils.getComponentTypeForGenericArray((GenericArrayType) expectedType);
    } else {
      throw new XL4JRuntimeException("expectedType not array or GenericArrayType");
    }
    final XLArray xlArr = (XLArray) from;
    final XLValue[][] arr = xlArr.getArray();
    final Object[][] targetArr = (Object[][]) Array.newInstance(XL4JReflectionUtils.reduceToClass(componentType), arr.length,
        arr.length > 0 ? arr[0].length : 0);
    TypeConverter lastConverter = null;
    Class<?> lastClass = null;
    final TypeConverterRegistry typeConverterRegistry = _excel.getTypeConverterRegistry();
    for (int i = 0; i < arr.length; i++) {
      for (int j = 0; j < arr[i].length; j++) {
        final XLValue val = arr[i][j];
        // This is a rather weak attempt at optimizing converter lookup - other options seemed to have greater overhead.
        if (lastConverter == null || !val.getClass().equals(lastClass)) {
          lastClass = val.getClass();
          lastConverter = typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(lastClass, componentType));
        }
        if (lastConverter == null) {
          throw new XL4JRuntimeException("Could not find type converter for " + lastClass + " using component type " + componentType);
        }
        targetArr[i][j] = lastConverter.toJavaObject(componentType, val);
      }
    }
    return targetArr;
  }
}
