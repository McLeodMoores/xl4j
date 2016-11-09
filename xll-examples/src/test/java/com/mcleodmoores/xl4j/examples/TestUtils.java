/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.reflect.Array;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.heap.Heap;
import com.mcleodmoores.xl4j.util.XlDateUtils;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLBoolean;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 *
 */
public final class TestUtils {

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
    if (object instanceof Boolean) {
      return (boolean) object ? XLBoolean.TRUE : XLBoolean.FALSE;
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

  public static XLValue convertToXlType(final Object object, final Heap heap) {
    final long handle = heap.getHandle(object);
    return XLObject.of(object.getClass(), handle);
  }

  public static void assert2dXlArray(final XLArray xlArray, final double[] firstExpectedArray, final double[] secondExpectedArray) {
    assertEquals(firstExpectedArray.length, secondExpectedArray.length);
    final XLValue[][] xlValues = xlArray.getArray();
    final int n = firstExpectedArray.length;
    // array converters have same priority so different ones could be picked
    if (xlValues.length == 2) {
      assertEquals(xlValues[0].length, n);
      for (int i = 0; i < n; i++) {
        assertTrue(xlValues[0][i] instanceof XLNumber);
        assertTrue(xlValues[1][i] instanceof XLNumber);
        assertEquals(((XLNumber) xlValues[0][i]).getAsDouble(), firstExpectedArray[i], 1e-15);
        assertEquals(((XLNumber) xlValues[1][i]).getAsDouble(), secondExpectedArray[i], 1e-15);
      }
    } else if (xlValues.length == n) {
      assertEquals(xlValues[0].length, 2);
      for (int i = 0; i < n; i++) {
        assertTrue(xlValues[i][0] instanceof XLNumber);
        assertTrue(xlValues[i][1] instanceof XLNumber);
        assertEquals(((XLNumber) xlValues[i][0]).getAsDouble(), firstExpectedArray[i], 1e-15);
        assertEquals(((XLNumber) xlValues[i][1]).getAsDouble(), secondExpectedArray[i], 1e-15);
      }
    } else if (xlValues.length == 1) {
      assertEquals(xlValues[0].length, n);
      for (int i = 0; i < n; i++) {
        assertTrue(xlValues[0][i] instanceof XLArray);
        final XLArray row = (XLArray) xlValues[0][i];
        assertTrue(row.getArray()[0][0] instanceof XLNumber);
        assertTrue(row.getArray()[0][1] instanceof XLNumber);
        assertEquals(((XLNumber) row.getArray()[0][0]).getAsDouble(), firstExpectedArray[i], 1e-15);
        assertEquals(((XLNumber) row.getArray()[0][1]).getAsDouble(), secondExpectedArray[i], 1e-15);
      }
    } else {
      fail("Rows = " + xlValues.length + ", columns = " + xlValues[0].length);
    }
  }

  public static void assert2dXlArray(final XLArray xlArray, final Double[] firstExpectedArray, final Double[] secondExpectedArray) {
    assertEquals(firstExpectedArray.length, secondExpectedArray.length);
    final XLValue[][] xlValues = xlArray.getArray();
    final int n = firstExpectedArray.length;
    // array converters have same priority so different ones could be picked
    if (xlValues.length == 2) {
      assertEquals(xlValues[0].length, n);
      for (int i = 0; i < n; i++) {
        assertTrue(xlValues[0][i] instanceof XLNumber);
        assertTrue(xlValues[1][i] instanceof XLNumber);
        assertEquals(((XLNumber) xlValues[0][i]).getAsDouble(), firstExpectedArray[i], 1e-15);
        assertEquals(((XLNumber) xlValues[1][i]).getAsDouble(), secondExpectedArray[i], 1e-15);
      }
    } else if (xlValues.length == n) {
      assertEquals(xlValues[0].length, 2);
      for (int i = 0; i < n; i++) {
        assertTrue(xlValues[i][0] instanceof XLNumber);
        assertTrue(xlValues[i][1] instanceof XLNumber);
        assertEquals(((XLNumber) xlValues[i][0]).getAsDouble(), firstExpectedArray[i], 1e-15);
        assertEquals(((XLNumber) xlValues[i][1]).getAsDouble(), secondExpectedArray[i], 1e-15);
      }
    } else if (xlValues.length == 1) {
      assertEquals(xlValues[0].length, n);
      for (int i = 0; i < n; i++) {
        assertTrue(xlValues[0][i] instanceof XLArray);
        final XLArray row = (XLArray) xlValues[0][i];
        assertTrue(row.getArray()[0][0] instanceof XLNumber);
        assertTrue(row.getArray()[0][1] instanceof XLNumber);
        assertEquals(((XLNumber) row.getArray()[0][0]).getAsDouble(), firstExpectedArray[i], 1e-15);
        assertEquals(((XLNumber) row.getArray()[0][1]).getAsDouble(), secondExpectedArray[i], 1e-15);
      }
    } else {
      fail("Rows = " + xlValues.length + ", columns = " + xlValues[0].length);
    }
  }
}
