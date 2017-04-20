/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.v1.api.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.xl4j.v1.api.values.XLInteger;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.ShortXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link ShortXLNumberTypeConverter}.
 */
@Test
public class ShortXLNumberTypeConverterTest {
  /** The expected priority */
  private static final int EXPECTED_PRIORITY = 10;
  /** Integer */
  private static final int TEN_I = 10;
  /** Long */
  private static final long TEN_L = 10L;
  /** Double */
  private static final double TEN_D = 10d;
  // REVIEW isn't it a bit odd that there's no complaint when there's a downcast to Short?
  /** XLNumber holding a double. */
  private static final XLNumber XL_NUMBER_DOUBLE = XLNumber.of(10.);
  /** XLNumber holding a short. */
  private static final XLNumber XL_NUMBER_SHORT = XLNumber.of((short) 10);
  /** XLNumber holding an int. */
  private static final XLNumber XL_NUMBER_INT = XLNumber.of(10);
  /** Short. */
  private static final Short SHORT = Short.valueOf((short) 10);
  /** The converter. */
  private static final AbstractTypeConverter CONVERTER = new ShortXLNumberTypeConverter();

  /**
   * Tests that the java type is {@link Short}.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLNumber.class, Short.class));
  }

  /**
   * Tests that the excel type is {@link XLNumber}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(Short.class, XLNumber.class));
  }

  /**
   * Tests the expected priority.
   */
  @Test
  public void testPriority() {
    assertEquals(CONVERTER.getPriority(), EXPECTED_PRIORITY);
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
    CONVERTER.toJavaObject(null, XL_NUMBER_DOUBLE);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullXLValue() {
    CONVERTER.toJavaObject(Short.class, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(Short.class, XLInteger.of(TEN_I));
  }

  /**
   * Tests that the expected type is ignored during conversions to Java.
   */
  @Test
  public void testWrongExpectedClassToJavaConversion() {
    assertEquals(CONVERTER.toJavaObject(Long.class, XLNumber.of(TEN_D)), SHORT);
  }

  /**
   * Tests for the exception when {@link XLValue} to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(TEN_L);
  }

  /**
   * Tests the conversion from a {@link Short}.
   */
  @Test
  public void testConversionFromShort() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(SHORT);
    assertTrue(converted instanceof XLNumber);
    final XLNumber xlNumber = (XLNumber) converted;
    assertEquals(xlNumber.getValue(), TEN_I, 0);
  }

  /**
   * Tests the conversion from a {@link XLNumber}.
   */
  @Test
  public void testConversionFromXLNumber() {
    Object converted = CONVERTER.toJavaObject(Short.class, XL_NUMBER_INT);
    assertTrue(converted instanceof Short);
    Short shor = (Short) converted;
    assertEquals(shor, SHORT);
    converted = CONVERTER.toJavaObject(Short.class, XL_NUMBER_SHORT);
    assertTrue(converted instanceof Short);
    shor = (Short) converted;
    assertEquals(shor, SHORT);
    converted = CONVERTER.toJavaObject(Short.class, XL_NUMBER_DOUBLE);
    assertTrue(converted instanceof Short);
    shor = (Short) converted;
    assertEquals(shor, SHORT);
  }

}
