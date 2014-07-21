/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.excel4j.values.XLBoolean;
import com.mcleodmoores.excel4j.values.XLInteger;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Unit tests for {@link FloatXLNumberTypeConverter}.
 */
@Test
public class FloatXLNumberTypeConverterTest {
  // REVIEW isn't it a bit odd that there's no complaint when there's an upcast to Float?
  /** XLNumber holding a float. */
  private static final XLNumber XL_NUMBER_FLOAT = XLNumber.of(10.F);
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
    assertEquals(CONVERTER.getPriority(), 10);
  }

  /**
   * Tests that passing in a null expected {@link XLValue} class gives the expected exception.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullExpectedXLValueClass() {
    CONVERTER.toXLValue(null, FLOAT);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullObject() {
    CONVERTER.toXLValue(XLNumber.class, null);
  }

  /**
   * Tests that passing in a null expected Java class gives the expected exception.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullExpectedClass() {
    CONVERTER.toJavaObject(null, XL_NUMBER_FLOAT);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullXLValue() {
    CONVERTER.toJavaObject(Float.class, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(Float.class, XLInteger.of(10));
  }

  /**
   * Tests for the exception when the expected class is wrong.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongExpectedClassToJavaConversion() {
    CONVERTER.toJavaObject(BigDecimal.class, XLNumber.of(10.));
  }

  /**
   * Tests for the exception when {@link XLValue} to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(XLNumber.class, Double.valueOf(10.));
  }

  /**
   * Tests for the exception when the expected class is wrong.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongExpectedClassToXLConversion() {
    CONVERTER.toXLValue(XLBoolean.class, 1.F);
  }

  /**
   * Tests the conversion from a {@link Float}.
   */
  @Test
  public void testConversionFromFloat() {
    final XLValue converted = CONVERTER.toXLValue(XL_NUMBER_FLOAT.getClass(), FLOAT);
    assertTrue(converted instanceof XLNumber);
    final XLNumber xlNumber = (XLNumber) converted;
    assertEquals(xlNumber.getValue(), 10., 0);
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
}
