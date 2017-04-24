/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.v1.api.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.xl4j.v1.api.values.XLInteger;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link BigDecimalXLNumberTypeConverter}.
 */
@Test
public class BigDecimalXLNumberTypeConverterTest {
  /** XLNumber holding a double */
  private static final XLNumber XL_NUMBER_DOUBLE = XLNumber.of(10d);
  /** XLNumber holding a long */
  private static final XLNumber XL_NUMBER_LONG = XLNumber.of(10L);
  /** XLNumber holding an int */
  private static final XLNumber XL_NUMBER_INT = XLNumber.of(10);
  /** BigDecimal */
  private static final BigDecimal BIG_DECIMAL = BigDecimal.valueOf(10d);
  /** The converter */
  private static final AbstractTypeConverter CONVERTER = new BigDecimalXLNumberTypeConverter();

  /**
   * Tests that the java type is {@link BigDecimal}.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLNumber.class, BigDecimal.class));
  }

  /**
   * Tests that the excel type is {@link XLNumber}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(BigDecimal.class, XLNumber.class));
  }

  /**
   * Tests the expected priority.
   */
  @Test
  public void testPriority() {
    assertEquals(CONVERTER.getPriority(), 10);
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
    CONVERTER.toJavaObject(BigDecimal.class, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(BigDecimal.class, XLInteger.of((int) 10.));
  }

  /**
   * Tests for the exception when the {@link XLValue} to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(Double.valueOf(10));
  }

  /**
   * Tests that the expected type is ignored during conversion to a XL class.
   */
  @Test
  public void testWrongExpectedClassToXLConversion() {
    assertEquals(CONVERTER.toXLValue(BigDecimal.ONE), XLNumber.of(1));
  }

  /**
   * Tests the conversion from a {@link BigDecimal}.
   */
  @Test
  public void testConversionFromBigDecimal() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(BIG_DECIMAL);
    assertTrue(converted instanceof XLNumber);
    final XLNumber xlNumber = (XLNumber) converted;
    assertEquals(xlNumber.getValue(), 10, 0);
  }

  /**
   * Tests the conversion from a {@link XLNumber}.
   */
  @Test
  public void testConversionFromXLNumber() {
    Object converted = CONVERTER.toJavaObject(BigDecimal.class, XL_NUMBER_INT);
    assertTrue(converted instanceof BigDecimal);
    BigDecimal bigDecimal = (BigDecimal) converted;
    assertEquals(bigDecimal, BIG_DECIMAL);
    converted = CONVERTER.toJavaObject(BigDecimal.class, XL_NUMBER_LONG);
    assertTrue(converted instanceof BigDecimal);
    bigDecimal = (BigDecimal) converted;
    assertEquals(bigDecimal, BIG_DECIMAL);
    converted = CONVERTER.toJavaObject(BigDecimal.class, XL_NUMBER_DOUBLE);
    assertTrue(converted instanceof BigDecimal);
    bigDecimal = (BigDecimal) converted;
    assertEquals(bigDecimal, BIG_DECIMAL);
  }

}
