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
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLInteger;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link FloatXLNumberTypeConverter}.
 */
@Test
public class FloatXLNumberTypeConverterTest {
  /** The expected priority */
  private static final int EXPECTED_PRIORITY = 10;
  /** Integer */
  private static final int TEN_I = 10;
  /** Double */
  private static final double TEN_D = 10d;
  // REVIEW isn't it a bit odd that there's no complaint when there's an upcast to Float?
  /** XLNumber holding a float. */
  private static final XLNumber XL_NUMBER_FLOAT = XLNumber.of(10f);
  /** XLNumber holding a long. */
  private static final XLNumber XL_NUMBER_LONG = XLNumber.of(10L);
  /** XLNumber holding an int. */
  private static final XLNumber XL_NUMBER_INT = XLNumber.of(10);
  /** Float. */
  private static final Float FLOAT = 10.F;
  /** The converter. */
  private static final AbstractTypeConverter CONVERTER = new FloatXLNumberTypeConverter();

  /**
   * Tests that the java type is {@link Float}.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLNumber.class, Float.class));
  }

  /**
   * Tests that the excel type is {@link XLNumber}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(Float.class, XLNumber.class));
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
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullXLValue() {
    CONVERTER.toJavaObject(Float.class, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(Float.class, XLInteger.of(TEN_I));
  }

  /**
   * Tests for the exception when {@link XLValue} to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(Double.valueOf(TEN_D));
  }

  /**
   * Tests the conversion from a {@link Float}.
   */
  @Test
  public void testConversionFromFloat() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(FLOAT);
    assertTrue(converted instanceof XLNumber);
    final XLNumber xlNumber = (XLNumber) converted;
    assertEquals(xlNumber.getValue(), TEN_D, 0);
  }

  /**
   * Tests the conversion from a {@link XLNumber}.
   */
  @Test
  public void testConversionFromXLNumber() {
    Object converted = CONVERTER.toJavaObject(Float.class, XL_NUMBER_INT);
    assertTrue(converted instanceof Float);
    Float floa = (Float) converted;
    assertEquals(floa, FLOAT);
    converted = CONVERTER.toJavaObject(Float.class, XL_NUMBER_LONG);
    assertTrue(converted instanceof Float);
    floa = (Float) converted;
    assertEquals(floa, FLOAT);
    converted = CONVERTER.toJavaObject(Float.class, XL_NUMBER_FLOAT);
    assertTrue(converted instanceof Float);
    floa = (Float) converted;
    assertEquals(floa, FLOAT);
  }

  /**
   * Tests the behaviour when the Float is infinite.
   */
  @Test
  public void testInfinite() {
    Object converted = CONVERTER.toXLValue(Float.POSITIVE_INFINITY);
    assertEquals(converted, XLError.Div0);
    converted = CONVERTER.toXLValue(Float.NEGATIVE_INFINITY);
    assertEquals(converted, XLError.Div0);
  }

  /**
   * Tests the behaviour when the Float is a NaN.
   */
  @Test
  public void testNaN() {
    final Object converted = CONVERTER.toXLValue(Float.NaN);
    assertEquals(converted, XLError.NA);
  }

}
