/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLInteger;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Unit tests for {@link PrimitiveLongXLNumberTypeConverter}.
 */
@Test
public class PrimitiveLongXLNumberTypeConverterTest {
  /** The expected priority */
  private static final int EXPECTED_PRIORITY = 10;
  /** Integer */
  private static final int TEN_I = 10;
  /** Double */
  private static final double TEN_D = 10d;
  // REVIEW isn't it a bit odd that there's no complaint when there's an upcast or downcast to Long?
  /** XLNumber holding a double. */
  private static final XLNumber XL_NUMBER_DOUBLE = XLNumber.of(10.);
  /** XLNumber holding a long. */
  private static final XLNumber XL_NUMBER_LONG = XLNumber.of(10L);
  /** XLNumber holding an int. */
  private static final XLNumber XL_NUMBER_INT = XLNumber.of(10);
  /** Long. */
  private static final Long LONG = 10L;
  /** The converter. */
  private static final AbstractTypeConverter CONVERTER = new PrimitiveLongXLNumberTypeConverter();

  /**
   * Tests that the java type is {@link Long}.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLNumber.class, Long.TYPE));
  }

  /**
   * Tests that the excel type is {@link XLNumber}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(Long.TYPE, XLNumber.class));
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
  @Test(expectedExceptions = Excel4JRuntimeException.class)
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
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullXLValue() {
    CONVERTER.toJavaObject(Long.TYPE, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(Long.TYPE, XLInteger.of(TEN_I));
  }

  /**
   * Tests that the expected type is ignored during conversions to Java.
   */
  @Test
  public void testWrongExpectedClassToJavaConversion() {
    assertEquals(CONVERTER.toJavaObject(BigDecimal.class, XLNumber.of(TEN_D)), LONG);
  }

  /**
   * Tests for the exception when {@link XLValue} to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(TEN_D);
  }

  /**
   * Tests the conversion from a {@link Long}.
   */
  @Test
  public void testConversionFromLong() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(LONG);
    assertTrue(converted instanceof XLNumber);
    final XLNumber xlNumber = (XLNumber) converted;
    assertEquals(xlNumber.getValue(), TEN_I, 0);
  }

  /**
   * Tests the conversion from a {@link XLNumber}.
   */
  @Test
  public void testConversionFromXLNumber() {
    Object converted = CONVERTER.toJavaObject(Long.class, XL_NUMBER_INT);
    assertTrue(converted instanceof Long);
    Long lon = (Long) converted;
    assertEquals(lon, LONG);
    converted = CONVERTER.toJavaObject(Long.class, XL_NUMBER_LONG);
    assertTrue(converted instanceof Long);
    lon = (Long) converted;
    assertEquals(lon, LONG);
    converted = CONVERTER.toJavaObject(Long.class, XL_NUMBER_DOUBLE);
    assertTrue(converted instanceof Long);
    lon = (Long) converted;
    assertEquals(lon, LONG);
  }
}
