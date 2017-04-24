/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.v1.api.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.xl4j.v1.api.values.XLBoolean;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link ObjectXLBooleanTypeConverter}.
 */
@Test
public class ObjectXLBooleanTypeConverterTest {
  /** The converter. */
  private static final AbstractTypeConverter CONVERTER = new ObjectXLBooleanTypeConverter();

  /**
   * Tests that the java type is {@link Object}.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLBoolean.class, Object.class));
  }

  /**
   * Tests that the excel type is {@link XLBoolean}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(Object.class, XLBoolean.class));
  }

  /**
   * Tests the expected priority.
   */
  @Test
  public void testPriority() {
    assertEquals(CONVERTER.getPriority(), -7);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullObject() {
    CONVERTER.toXLValue(null);
  }

  /**
   * Tests that passing in a null expected Java class is successful.
   */
  @Test
  public void testNullExpectedClass() {
    CONVERTER.toJavaObject(null, XLBoolean.FALSE);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullXLValue() {
    CONVERTER.toJavaObject(Boolean.class, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(Boolean.class, XLNumber.of(10));
  }

  /**
   * Tests that the expected type is ignored during conversions to Java.
   */
  @Test
  public void testWrongExpectedClassToJavaConversion() {
    assertEquals(CONVERTER.toJavaObject(Integer.class, XLBoolean.FALSE), Boolean.FALSE);
  }

  /**
   * Tests for the exception when the {@link XLValue} to convert is the wrong type.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(Integer.valueOf(10));
  }

  /**
   * Tests the conversion from a {@link Boolean}.
   */
  @Test
  public void testConversionFromBoolean() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(Boolean.FALSE);
    assertTrue(converted instanceof XLBoolean);
    final XLBoolean xlBoolean = (XLBoolean) converted;
    assertFalse(xlBoolean.getValue());
  }

  /**
   * Tests the conversion from a {@link XLBoolean}.
   */
  @Test
  public void testConversionFromXLBoolean() {
    Object converted = CONVERTER.toJavaObject(Boolean.class, XLBoolean.FALSE);
    assertTrue(converted instanceof Boolean);
    Boolean bool = (Boolean) converted;
    assertFalse(bool);
    converted = CONVERTER.toJavaObject(Boolean.class, XLBoolean.TRUE);
    assertTrue(converted instanceof Boolean);
    bool = (Boolean) converted;
    assertTrue(bool);
  }
}
