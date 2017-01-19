/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Type converter to convert from arrays of floats to Excel arrays and back again. The input array from Excel can contain any type of
 * {@link XLValue} (e.g. <code>XLNumber</code>, <code>XLString("1.0f")</code>) and an attempt will be made to convert this value to a float.
 */
public final class PrimitiveFloatArrayXLArrayTypeConverter extends AbstractTypeConverter {
  /** The underlying converter */
  private static final TypeConverter CONVERTER = new PrimitiveFloatXLNumberTypeConverter();
  /** The underlying string converter */
  private static final TypeConverter STRING_CONVERTER = new PrimitiveFloatXLStringTypeConverter();

  /**
   * Default constructor.
   */
  public PrimitiveFloatArrayXLArrayTypeConverter() {
    super(float[].class, XLArray.class);
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (!from.getClass().isArray()) {
      throw new Excel4JRuntimeException("from parameter must be an array");
    }
    Type componentType = null;
    if (expectedType instanceof Class) {
      final Class<?> expectedClass = from.getClass();
      componentType = expectedClass.getComponentType();
      // REVIEW: this will never fail because of the isArray() test
      if (componentType == null) {
        throw new Excel4JRuntimeException("component type of from parameter is null");
      }
    } else {
      throw new Excel4JRuntimeException("expectedType not array or GenericArrayType");
    }
    final float[] fromArr = (float[]) from;
    final XLValue[][] toArr = new XLValue[1][fromArr.length];
    for (int i = 0; i < fromArr.length; i++) {
      final XLValue value = (XLValue) CONVERTER.toXLValue(componentType, fromArr[i]);
      toArr[0][i] = value;
    }
    return XLArray.of(toArr);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    final XLArray xlArr = (XLArray) from;
    final XLValue[][] arr = xlArr.getArray();
    if (arr.length == 1) { // array is a single row
      final float[] targetArr = new float[arr[0].length];
      for (int i = 0; i < arr[0].length; i++) {
        targetArr[i] = convertValue(arr[0][i]);
      }
      return targetArr;
    }
    // array is single column
    final float[] targetArr = new float[arr.length];
    for (int i = 0; i < arr.length; i++) {
      targetArr[i] = convertValue(arr[i][0]);
    }
    return targetArr;
  }

  private static float convertValue(final XLValue val) {
    if (val instanceof XLString) {
      return (float) STRING_CONVERTER.toJavaObject(Float.TYPE, val);
    } else if (val instanceof XLNumber) {
      return (float) CONVERTER.toJavaObject(Float.TYPE, val);
    }
    throw new Excel4JRuntimeException("Could not convert objects of type " + val.getClass() + " to float");
  }

}
