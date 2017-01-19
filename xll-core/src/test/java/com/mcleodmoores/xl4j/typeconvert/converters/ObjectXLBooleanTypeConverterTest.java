/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLBoolean;
import com.mcleodmoores.xl4j.values.XLInteger;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLValue;

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
   * Tests that passing in a null expected {@link XLBoolean} is successful.
   */
  @Test
  public void testNullExpectedXLValueClass() {
    CONVERTER.toXLValue(null, Boolean.FALSE);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullObject() {
    CONVERTER.toXLValue(XLBoolean.class, null);
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
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullXLValue() {
    CONVERTER.toJavaObject(Boolean.class, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
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
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(XLBoolean.class, Integer.valueOf(10));
  }

  /**
   * Tests that the expected type is ignored during conversion to a XL class.
   */
  @Test
  public void testWrongExpectedClassToXLConversion() {
    assertEquals(CONVERTER.toXLValue(XLInteger.class, Boolean.FALSE), XLBoolean.FALSE);
  }

  /**
   * Tests the conversion from a {@link Boolean}.
   */
  @Test
  public void testConversionFromBoolean() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XLBoolean.class, Boolean.FALSE);
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
