/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigInteger;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.v1.api.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.xl4j.v1.api.values.XLInteger;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link BigIntegerXLNumberTypeConverter}.
 */
@Test
public class BigIntegerXLNumberTypeConverterTest {
  /** The expected priority */
  private static final int EXPECTED_PRIORITY = 10;
  /** Ten as an integer */
  private static final int TEN_I = 10;
  /** Ten as a double */
  private static final double TEN_D = 10d;
  // REVIEW isn't it a bit odd that there's no complaint when a double is successfully converted?
  /** XLNumber holding a double. */
  private static final XLNumber XL_NUMBER_DOUBLE = XLNumber.of(10d);
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
   * Tests that passing in a null expected Java is successful.
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
    CONVERTER.toJavaObject(BigInteger.class, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(BigInteger.class, XLInteger.of(TEN_I));
  }

  /**
   * Tests that the expected type is ignored during conversions to Java.
   */
  @Test
  public void testWrongExpectedClassToJavaConversion() {
    assertEquals(CONVERTER.toJavaObject(Integer.class, XLNumber.of(TEN_I)), BIG_INTEGER);
  }

  /**
   * Tests for the exception when {@link XLValue} to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(Integer.valueOf(TEN_I));
  }

  /**
   * Tests the conversion from a {@link BigInteger}.
   */
  @Test
  public void testConversionFromBigInteger() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(BIG_INTEGER);
    assertTrue(converted instanceof XLNumber);
    final XLNumber xlNumber = (XLNumber) converted;
    assertEquals(xlNumber.getValue(), TEN_D, 0);
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
