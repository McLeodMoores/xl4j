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
 * Unit tests for {@link DoubleXLNumberTypeConverter}.
 */
@Test
public class DoubleXLNumberTypeConverterTest {
  // REVIEW isn't it a bit odd that there's no complaint when there's an upcast to Double?
  /** XLNumber holding a double. */
  private static final XLNumber XL_NUMBER_DOUBLE = XLNumber.of(10.);
  /** XLNumber holding a long. */
  private static final XLNumber XL_NUMBER_LONG = XLNumber.of(10L);
  /** XLNumber holding an int. */
  private static final XLNumber XL_NUMBER_INT = XLNumber.of(10);
  /** Double. */
  private static final Double DOUBLE = 10.;
  /** The converter. */
  private static final AbstractTypeConverter CONVERTER = new DoubleXLNumberTypeConverter();

  /**
   * Tests that the java type is {@link Double}.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLNumber.class, Double.class));
  }

  /**
   * Tests that the excel type is {@link XLNumber}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(Double.class, XLNumber.class));
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
    CONVERTER.toXLValue(null, DOUBLE);
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
    CONVERTER.toJavaObject(null, XL_NUMBER_DOUBLE);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullXLValue() {
    CONVERTER.toJavaObject(Double.class, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(Double.class, XLInteger.of(10));
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
    CONVERTER.toXLValue(XLNumber.class, BigDecimal.valueOf(10.));
  }

  /**
   * Tests for the exception when the expected class is wrong.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongExpectedClassToXLConversion() {
    CONVERTER.toXLValue(XLBoolean.class, 1.);
  }

  /**
   * Tests the conversion from a {@link Double}.
   */
  @Test
  public void testConversionFromDouble() {
    final XLValue converted = CONVERTER.toXLValue(XL_NUMBER_DOUBLE.getClass(), DOUBLE);
    assertTrue(converted instanceof XLNumber);
    final XLNumber xlNumber = (XLNumber) converted;
    assertEquals(xlNumber.getValue(), 10., 0);
  }

  /**
   * Tests the conversion from a {@link XLNumber}.
   */
  @Test
  public void testConversionFromXLNumber() {
    Object converted = CONVERTER.toJavaObject(Double.class, XL_NUMBER_INT);
    assertTrue(converted instanceof Double);
    Double doub = (Double) converted;
    assertEquals(doub, DOUBLE);
    converted = CONVERTER.toJavaObject(Double.class, XL_NUMBER_LONG);
    assertTrue(converted instanceof Double);
    doub = (Double) converted;
    assertEquals(doub, DOUBLE);
    converted = CONVERTER.toJavaObject(Double.class, XL_NUMBER_DOUBLE);
    assertTrue(converted instanceof Double);
    doub = (Double) converted;
    assertEquals(doub, DOUBLE);
  }
}
