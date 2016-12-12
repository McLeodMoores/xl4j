/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.ExcelFactory;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLString;

/**
 *
 */
public class JFieldTest {
  private static final XLString CLASS_NAME = XLString.of("com.mcleodmoores.xl4j.testutil.TestObject");

  @Test
  public void testField() {
    final XLObject object = (XLObject) JMethod.jStaticMethod(CLASS_NAME, XLString.of("of"), XLNumber.of(12345));
    Object field = JField.jField(object, XLString.of("_number"));
    assertTrue(field instanceof XLNumber);
    assertEquals(((XLNumber) field).getAsDouble(), -4000.);
    field = JField.jField(object, XLString.of("_name"));
    assertTrue(field instanceof XLString);
    assertEquals(((XLString) field).getValue(), "static Double constructor");
    field = JField.jField(object, XLString.of("_doubles"));
    assertTrue(field instanceof XLArray);
    assertEquals(((XLArray) field).getArray().length, 1);
    assertEquals(((XLArray) field).getArray()[0].length, 1);
    assertEquals(((XLNumber) ((XLArray) field).getArray()[0][0]).getAsDouble(), 12345.);
  }

  @Test
  public void testFieldX() {
    final XLObject object = (XLObject) JMethod.jStaticMethod(CLASS_NAME, XLString.of("of"), XLNumber.of(12345));
    Object field = JField.jFieldX(object, XLString.of("_number"));
    assertEquals(getValueFromHeap(field, Number.class).doubleValue(), -4000.);
    field = JField.jFieldX(object, XLString.of("_name"));
    assertEquals(getValueFromHeap(field, String.class), "static Double constructor");
    field = JField.jFieldX(object, XLString.of("_doubles"));
    final Double[] array = getValueFromHeap(field, Double[].class);
    assertEquals(array.length, 1);
    assertEquals(array[0], 12345.);
  }

  @Test
  public void testStaticField() {
    final Object field = JField.jStaticField(CLASS_NAME, XLString.of("MAGIC_NUMBER"));
    assertTrue(field instanceof XLNumber);
    assertEquals(((XLNumber) field).getAsDouble(), 1.234567);
  }

  @Test
  public void testStaticFieldX() {
    final Object field = JField.jStaticFieldX(CLASS_NAME, XLString.of("MAGIC_NUMBER"));
    assertEquals(getValueFromHeap(field, Double.class), 1.234567);
  }

  private static <T> T getValueFromHeap(final Object object, final Class<T> expectedClass) {
    assertTrue(object instanceof XLObject);
    return expectedClass.cast(ExcelFactory.getInstance().getHeap().getObject(((XLObject) object).getHandle()));
  }
}
