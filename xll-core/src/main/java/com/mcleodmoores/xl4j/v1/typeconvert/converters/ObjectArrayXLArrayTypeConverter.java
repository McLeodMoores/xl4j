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
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.ConverterUtils;
import com.mcleodmoores.xl4j.v1.util.XL4JReflectionUtils;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Type converter to convert from arrays of Objects to Excel arrays and back again. When converting back to Java, the most specific
 * converter that can be found is used (e.g. Boolean -&gt; XLBoolean, rather than an XLObject wrapping a boolean).This converter is higher
 * priority than {@link ObjectArrayXLArrayTypeConverter2}, which only converts to XLObjects.
 */
public final class ObjectArrayXLArrayTypeConverter extends AbstractTypeConverter {
  /** The Excel context */
  private final Excel _excel;

  /**
   * Default constructor.
   *
   * @param excel
   *          the excel context object, used to access the type converter registry, not null
   */
  public ObjectArrayXLArrayTypeConverter(final Excel excel) {
    super(Object[].class, XLArray.class);
    ArgumentChecker.notNull(excel, "excel");
    _excel = excel;
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    final Object[] fromArr = (Object[]) from;
    if (fromArr.length == 0) {
      return XLArray.of(new XLValue[1][1]);
    }
    final Type componentType = from.getClass().getComponentType();
    TypeConverter lastConverter = null;
    Class<?> lastClass = null;
    final XLValue[][] toArr = new XLValue[1][fromArr.length];
    final TypeConverterRegistry typeConverterRegistry = _excel.getTypeConverterRegistry();
    for (int i = 0; i < fromArr.length; i++) {
      final Object obj = fromArr[i];
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
      toArr[0][i] = value;
    }
    return XLArray.of(toArr);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    final XLArray xlArr = (XLArray) from;
    final XLValue[][] arr = xlArr.getArray();
    final Type componentType;
    if (expectedType instanceof Class) {
      final Class<?> expectedClass = (Class<?>) expectedType;
      componentType = expectedClass.getComponentType();
    } else if (expectedType instanceof GenericArrayType) {
      // XLArray is not empty by definition so no test for emptiness needed
      final Type componentTypeForGenericArray = ConverterUtils.getComponentTypeForGenericArray((GenericArrayType) expectedType);
      // handle generic array inputs
      if (arr[0][0] instanceof XLArray && componentTypeForGenericArray.equals(Object.class)) {
        componentType = Object[].class;
      } else {
        componentType = componentTypeForGenericArray;
      }
    } else {
      throw new XL4JRuntimeException("expectedType not array or GenericArrayType");
    }
    TypeConverter lastConverter = null;
    Class<?> lastClass = null;
    final TypeConverterRegistry typeConverterRegistry = _excel.getTypeConverterRegistry();
    final boolean isRow = arr.length == 1;
    final int n = isRow ? arr[0].length : arr.length;
    final Object[] targetArr = (Object[]) Array.newInstance(XL4JReflectionUtils.reduceToClass(componentType), n);
    for (int i = 0; i < n; i++) {
      final XLValue val = isRow ? arr[0][i] : arr[i][0];
      Class<?> valueClass;
      XLValue valueToConvert;
      if (val instanceof XLString && ((XLString) val).isXLObject()) {
        valueToConvert = ((XLString) val).toXLObject();
        valueClass = valueToConvert.getClass();
      } else {
        valueClass = val.getClass();
        valueToConvert = val;
      }
      // This is a rather weak attempt at optimizing converter lookup - other options seemed to have greater overhead.
      if (lastConverter == null || !valueClass.equals(lastClass)) {
        lastClass = valueClass;
        lastConverter = typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(lastClass, componentType));
      }
      if (lastConverter == null) {
        throw new XL4JRuntimeException("Could not find type converter for " + lastClass + " using component type " + componentType);
      }
      targetArr[i] = lastConverter.toJavaObject(componentType, valueToConvert);
    }
    return targetArr;
  }

}
