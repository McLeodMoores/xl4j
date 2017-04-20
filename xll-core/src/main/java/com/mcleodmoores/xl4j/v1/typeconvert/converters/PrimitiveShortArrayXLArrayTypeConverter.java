/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Type converter to convert from arrays of shorts to Excel arrays and back again. The input array from Excel can contain any type of
 * {@link XLValue} (e.g. <code>XLNumber</code>) and an attempt will be made to convert this value to a short.
 */
public final class PrimitiveShortArrayXLArrayTypeConverter extends AbstractTypeConverter {
  /** The underlying converter */
  private static final TypeConverter CONVERTER = new PrimitiveShortXLNumberTypeConverter();
  /** The underlying string converter */
  private static final TypeConverter STRING_CONVERTER = new PrimitiveShortXLStringTypeConverter();

  /**
   * Default constructor.
   */
  public PrimitiveShortArrayXLArrayTypeConverter() {
    super(short[].class, XLArray.class);
  }

  @Override
  public Object toXLValue(final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (!from.getClass().isArray()) {
      throw new XL4JRuntimeException("\"from\" parameter must be an array");
    }
    final short[] fromArr = (short[]) from;
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
      final short[] targetArr = new short[arr[0].length];
      for (int i = 0; i < arr[0].length; i++) {
        targetArr[i] = convertValue(arr[0][i]);
      }
      return targetArr;
    }
    // array is single column
    final short[] targetArr = new short[arr.length];
    for (int i = 0; i < arr.length; i++) {
      targetArr[i] = convertValue(arr[i][0]);
    }
    return targetArr;
  }

  private static short convertValue(final XLValue val) {
    if (val instanceof XLString) {
      return (short) STRING_CONVERTER.toJavaObject(Short.TYPE, val);
    } else if (val instanceof XLNumber) {
      return (short) CONVERTER.toJavaObject(Short.TYPE, val);
    }
    throw new XL4JRuntimeException("Could not convert objects of type " + val.getClass() + " to short");
  }
}
