/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigInteger;
import java.math.BigInteger;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLBoolean;
import com.mcleodmoores.excel4j.values.XLError;
import com.mcleodmoores.excel4j.values.XLInteger;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Unit tests for {@link BigIntegerXLNumberTypeConverter}.
 */
@Test
public class BigIntegerXLNumberTypeConverterTest extends TypeConverterTests {
  private static final int EXPECTED_PRIORITY = 10;
  private static final double TEN_D = 10d;
  private static final int TEN_I = 10;
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
  /** The class name */
  private static final String CLASSNAME = "java.math.BigInteger";

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
  @Test
  public void testNullExpectedClass() {
    CONVERTER.toJavaObject(null, XL_NUMBER_DOUBLE);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
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
    CONVERTER.toXLValue(XLNumber.class, Integer.valueOf(TEN_I));
  }

  /**
   * Tests that the expected type is ignored during conversion to a XL class.
   */
  @Test
  public void testWrongExpectedClassToXLConversion() {
    assertEquals(CONVERTER.toXLValue(XLBoolean.class, BigInteger.ONE), XLNumber.of(1));
  }

  /**
   * Tests the conversion from a {@link BigInteger}.
   */
  @Test
  public void testConversionFromBigInteger() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XL_NUMBER_DOUBLE.getClass(), BIG_INTEGER);
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

  /**
   * Tests creation of BigIntegers using its constructors.
   */
  @Test
  public void testJConstruct() {
    // no no-args constructor for BigInteger
    XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME));
    assertTrue(xlValue instanceof XLError);
    // new BigInteger(String)
    xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XLString.of("10"));
    assertTrue(xlValue instanceof XLObject);
    Object bigIntegerObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(bigIntegerObject instanceof BigInteger);
    assertEquals(((BigInteger) bigIntegerObject).intValue(), 10);
    // new BigInteger(String, int)
    xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XLString.of("12"), XLNumber.of(8));
    assertTrue(xlValue instanceof XLObject);
    bigIntegerObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(bigIntegerObject instanceof BigInteger);
    assertEquals(((BigInteger) bigIntegerObject).intValue(), 10);
  }

  /**
   * Tests creation of BigIntegers using its static constructors.
   */
  @Test
  public void testJMethod() {
    // no BigInteger.valueOf(String)
    XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XLString.of("10"));
    assertTrue(xlValue instanceof XLError);
    // BigInteger.valueOf(double)
    xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XL_NUMBER_DOUBLE);
    assertTrue(xlValue instanceof XLObject);
    Object bigIntegerObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(bigIntegerObject instanceof BigInteger);
    BigInteger bigInteger = (BigInteger) bigIntegerObject;
    assertEquals(bigInteger.doubleValue(), BIG_INTEGER.doubleValue(), 0);
    // BigInteger.valueOf(long)
    xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XL_NUMBER_LONG);
    assertTrue(xlValue instanceof XLObject);
    bigIntegerObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(bigIntegerObject instanceof BigInteger);
    bigInteger = (BigInteger) bigIntegerObject;
    assertEquals(bigInteger.longValue(), BIG_INTEGER.longValue());
    // BigInteger.valueOf(long, int)
    xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XL_NUMBER_INT);
    assertTrue(xlValue instanceof XLObject);
    bigIntegerObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(bigIntegerObject instanceof BigInteger);
    bigInteger = (BigInteger) bigIntegerObject;
    assertEquals(bigInteger.intValue(), BIG_INTEGER.intValue());
  }
}
