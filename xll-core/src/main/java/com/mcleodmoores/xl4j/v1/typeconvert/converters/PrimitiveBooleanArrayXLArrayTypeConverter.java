/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLBoolean;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Type converter to convert from arrays of booleans to Excel arrays and back again. The input array from Excel can contain any type of
 * {@link XLValue} (e.g. <code>XLBoolean</code>, <code>XLString("true")</code>) and an attempt will be made to convert this value to a
 * boolean.
 */
public final class PrimitiveBooleanArrayXLArrayTypeConverter extends AbstractTypeConverter {
  /** The underlying converter */
  private static final TypeConverter CONVERTER = new BooleanXLBooleanTypeConverter();
  /** The underlying string converter */
  private static final TypeConverter STRING_CONVERTER = new BooleanXLStringTypeConverter();

  /**
   * Default constructor.
   */
  public PrimitiveBooleanArrayXLArrayTypeConverter() {
    super(boolean[].class, XLArray.class);
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    final boolean[] fromArr = (boolean[]) from;
    final XLValue[][] toArr = new XLValue[1][fromArr.length];
    for (int i = 0; i < fromArr.length; i++) {
      final XLValue value = (XLValue) CONVERTER.toXLValue(fromArr[i]);
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
      final boolean[] targetArr = new boolean[arr[0].length];
      for (int i = 0; i < arr[0].length; i++) {
        targetArr[i] = convertValue(arr[0][i]);
      }
      return targetArr;
    }
    // array is single column
    final boolean[] targetArr = new boolean[arr.length];
    for (int i = 0; i < arr.length; i++) {
      targetArr[i] = convertValue(arr[i][0]);
    }
    return targetArr;
  }

  private static boolean convertValue(final XLValue val) {
    if (val instanceof XLString) {
      return (boolean) STRING_CONVERTER.toJavaObject(Boolean.TYPE, val);
    } else if (val instanceof XLBoolean) {
      return (boolean) CONVERTER.toJavaObject(Boolean.TYPE, val);
    }
    throw new XL4JRuntimeException("Could not convert objects of type " + val.getClass() + " to boolean");
  }

}
