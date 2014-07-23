/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigInteger;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLBoolean;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Unit tests for {@link BigIntegerXLNumberTypeConverter}.
 */
@Test
public class BigIntegerXLNumberTypeConverterTest {
  // REVIEW isn't it a bit odd that there's no complaint when a double is successfully converted?
  /** XLNumber holding a double. */
  private static final XLNumber XL_NUMBER_DOUBLE = XLNumber.of(10.);
  /** XLNumber holding a long. */
  private static final XLNumber XL_NUMBER_LONG = XLNumber.of(10L);
  /** XLNumber holding an int. */
  private static final XLNumber XL_NUMBER_INT = XLNumber.of(10);
  /** BigInteger. */
  private static final BigInteger BIG_INTEGER = BigInteger.valueOf(10);
  /** The converter. */
  private static final AbstractTypeConverter CONVERTER = new BigIntegerXLNumberTypeConverter();

  /**
   * Tests that the java type is {@link BigInteger}.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLNumber.class, BigInteger.class));
  }

  /**
   * Tests that the Excel type is {@link XLNumber}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(BigInteger.class, XLNumber.class));
  }

  /**
   * Tests the expected priority.
   */
  @Test
  public void testPriority() {
    assertEquals(CONVERTER.getPriority(), 10);
  }

  /**
   * Tests that passing in a null expected {@link XLValue} is successful.
   */
  @Test
  public void testNullExpectedXLValueClass() {
    CONVERTER.toXLValue(null, BIG_INTEGER);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullObject() {
    CONVERTER.toXLValue(XLNumber.class, null);
  }

  /**
   * Tests that passing in a null expected Java is successful.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullExpectedClass() {
    CONVERTER.toJavaObject(null, XL_NUMBER_DOUBLE);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullXLValue() {
    CONVERTER.toJavaObject(BigInteger.class, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(BigInteger.class, XLNumber.of(10.));
  }

  /**
   * Tests for the exception when the expected class is wrong.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongExpectedClassToJavaConversion() {
    CONVERTER.toJavaObject(Integer.class, XLNumber.of(10));
  }

  /**
   * Tests for the exception when {@link XLValue} to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(XLNumber.class, Integer.valueOf(10));
  }

  /**
   * Tests for the exception when the expected class is wrong.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongExpectedClassToXLConversion() {
    CONVERTER.toXLValue(XLBoolean.class, BigInteger.ONE);
  }

  /**
   * Tests the conversion from a {@link BigInteger}.
   */
  @Test
  public void testConversionFromBigInteger() {
    final XLValue converted = CONVERTER.toXLValue(XL_NUMBER_DOUBLE.getClass(), BIG_INTEGER);
    assertTrue(converted instanceof XLNumber);
    final XLNumber xlNumber = (XLNumber) converted;
    assertEquals(xlNumber.getValue(), 10., 0);
  }

  /**
   * Tests the conversion from a {@link XLNumber}.
   */
  @Test
  public void testConversionFromXLNumber() {
    Object converted = CONVERTER.toJavaObject(BigInteger.class, XL_NUMBER_INT);
    assertTrue(converted instanceof BigInteger);
    BigInteger bigInteger = (BigInteger) converted;
    assertEquals(bigInteger, BIG_INTEGER);
    converted = CONVERTER.toJavaObject(BigInteger.class, XL_NUMBER_LONG);
    assertTrue(converted instanceof BigInteger);
    bigInteger = (BigInteger) converted;
    assertEquals(bigInteger, BIG_INTEGER);
    converted = CONVERTER.toJavaObject(BigInteger.class, XL_NUMBER_DOUBLE);
    assertTrue(converted instanceof BigInteger);
    bigInteger = (BigInteger) converted;
    assertEquals(bigInteger, BIG_INTEGER);
  }
}
