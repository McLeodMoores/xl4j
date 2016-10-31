/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples;

import java.lang.reflect.Array;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.util.XlDateUtils;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 *
 */
public class TestUtils {

  public static XLValue convertToXlType(final Object object) {
    if (object instanceof Number) {
      return XLNumber.of(((Number) object).doubleValue());
    }
    if (object instanceof String) {
      return XLString.of((String) object);
    }
    if (object instanceof LocalDate) {
      return XLNumber.of(XlDateUtils.getDaysFromXlEpoch((LocalDate) object));
    }
    if (object.getClass().isArray()) {
      //TODO only works for 1D arrays
      XLValue[][] values = new XLValue[0][];
      if (object.getClass().getComponentType().isPrimitive()) {
        final int n = Array.getLength(object);
        values = new XLValue[][] { new XLValue[n] };
        for (int i = 0; i < n; i++) {
          values[0][i] = convertToXlType(Array.getDouble(object, i));
        }
      } else {
        final Object[] array = (Object[]) object;
        final int n = array.length;
        values = new XLValue[][] { new XLValue[n] };
        for (int i = 0; i < n; i++) {
          values[0][i] = convertToXlType(array[i]);
        }
      }
      return XLArray.of(values);
    }
    throw new IllegalArgumentException("Unsupported object type " + object);
  }
}
