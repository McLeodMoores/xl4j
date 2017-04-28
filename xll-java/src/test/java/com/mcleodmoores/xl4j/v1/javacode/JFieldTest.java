/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;

/**
 * Unit tests for {@link JField}.
 */
public class JFieldTest {
  private static final XLString CLASS_NAME = XLString.of("com.mcleodmoores.xl4j.v1.javacode.testutils.TestObject");

  /**
   * Tests the case where the field does not exist.
   */
  @Test
  public void testNoField1() {
    final XLObject object = (XLObject) JMethod.jStaticMethod(CLASS_NAME, XLString.of("ofDouble"), XLNumber.of(12345));
    final Object result = JField.jField(object, XLString.of("noFieldOfThisName"));
    assertSame(result, XLError.Null);
  }

  /**
   * Tests the case where the field does not exist.
   */
  @Test
  public void testNoField2() {
    final XLObject object = (XLObject) JMethod.jStaticMethod(CLASS_NAME, XLString.of("ofDouble"), XLNumber.of(12345));
    final Object result = JField.jFieldX(object, XLString.of("noFieldOfThisName"));
    assertSame(result, XLError.Null);
  }

  /**
   * Tests the case where the field does not exist.
   */
  @Test
  public void testNoField3() {
    final Object result = JField.jStaticField(XLString.of("com.mcleodmoores.xl4j.v1.javacode.testutils.TestObject"),
        XLString.of("noFieldOfThisName"));
    assertSame(result, XLError.Null);
  }

  /**
   * Tests the case where the field does not exist.
   */
  @Test
  public void testNoField4() {
    final Object result = JField.jStaticFieldX(XLString.of("com.mcleodmoores.xl4j.v1.javacode.testutils.TestObject"),
        XLString.of("noFieldOfThisName"));
    assertSame(result, XLError.Null);
  }

  /**
   * Tests the case where the field is not visible.
   */
  @Test
  public void testPrivateField1() {
    final XLObject object = (XLObject) JMethod.jStaticMethod(CLASS_NAME, XLString.of("ofDouble"), XLNumber.of(12345));
    final Object result = JField.jField(object, XLString.of("_privateField"));
    assertSame(result, XLError.Null);
  }

  /**
   * Tests the case where the field is not visible.
   */
  @Test
  public void testPrivateField2() {
    final XLObject object = (XLObject) JMethod.jStaticMethod(CLASS_NAME, XLString.of("ofDouble"), XLNumber.of(12345));
    final Object result = JField.jFieldX(object, XLString.of("_privateField"));
    assertSame(result, XLError.Null);
  }

  /**
   * Tests the case where the field is not visible.
   */
  @Test
  public void testPrivateField3() {
    final Object result = JField.jStaticField(XLString.of("com.mcleodmoores.xl4j.v1.javacode.testutils.TestObject"),
        XLString.of("PRIVATE_FIELD"));
    assertSame(result, XLError.Null);
  }

  /**
   * Tests the case where the field is not visible.
   */
  @Test
  public void testPrivateField4() {
    final Object result = JField.jStaticFieldX(XLString.of("com.mcleodmoores.xl4j.v1.javacode.testutils.TestObject"),
        XLString.of("PRIVATE_FIELD"));
    assertSame(result, XLError.Null);
  }

  /**
   *
   */
  @Test
  public void testField() {
    final XLObject object = (XLObject) JMethod.jStaticMethod(CLASS_NAME, XLString.of("ofDouble"), XLNumber.of(12345));
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

  /**
   *
   */
  @Test
  public void testFieldX() {
    final XLObject object = (XLObject) JMethod.jStaticMethod(CLASS_NAME, XLString.of("ofDouble"), XLNumber.of(12345));
    Object field = JField.jFieldX(object, XLString.of("_number"));
    assertEquals(getValueFromHeap(field, Number.class).doubleValue(), -4000.);
    field = JField.jFieldX(object, XLString.of("_name"));
    assertEquals(getValueFromHeap(field, String.class), "static Double constructor");
    field = JField.jFieldX(object, XLString.of("_doubles"));
    final double[] array = getValueFromHeap(field, double[].class);
    assertEquals(array.length, 1);
    assertEquals(array[0], 12345.);
  }

  /**
   *
   */
  @Test
  public void testStaticField() {
    final Object field = JField.jStaticField(CLASS_NAME, XLString.of("MAGIC_NUMBER"));
    assertTrue(field instanceof XLNumber);
    assertEquals(((XLNumber) field).getAsDouble(), 1.234567);
  }

  /**
   *
   */
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
