/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLBoolean;
import com.mcleodmoores.excel4j.values.XLInteger;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Unit tests for {@link PrimitiveBooleanXLBooleanTypeConverter}.
 */
@Test
public class PrimitiveBooleanXLBooleanTypeConverterTest {
  /** The expected priority */
  private static final int EXPECTED_PRIORITY = 10;
  /** 10 as an integer */
  private static final int TEN_I = 10;
  /** 10 as a double */
  private static final double TEN_D = 10d;
  /** The converter */
  private static final AbstractTypeConverter CONVERTER = new PrimitiveBooleanXLBooleanTypeConverter();

  /**
   * Tests that the java type is {@link Boolean#TYPE}.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLBoolean.class, Boolean.TYPE));
  }

  /**
   * Tests that the excel type is {@link XLBoolean}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(Boolean.TYPE, XLBoolean.class));
  }

  /**
   * Tests the expected priority.
   */
  @Test
  public void testPriority() {
    assertEquals(CONVERTER.getPriority(), EXPECTED_PRIORITY);
  }

  /**
   * Tests that passing in a null expected {@link XLBoolean} is successful.
   */
  @Test
  public void testNullExpectedXLValueClass() {
    CONVERTER.toXLValue(null, false);
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
    CONVERTER.toJavaObject(Boolean.TYPE, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(Boolean.TYPE, XLNumber.of(TEN_D));
  }

  /**
   * Tests for the exception when the expected class is wrong.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongExpectedClassToJavaConversion() {
    CONVERTER.toJavaObject(Integer.TYPE, XLBoolean.FALSE);
  }

  /**
   * Tests for the exception when {@link XLValue} to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(XLBoolean.class, Integer.valueOf(TEN_I));
  }

  /**
   * Tests for the exception when the expected class is wrong.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongExpectedClassToXLConversion() {
    CONVERTER.toXLValue(XLInteger.class, false);
  }

  /**
   * Tests the conversion from a {@link Boolean}.
   */
  @Test
  public void testConversionFromBoolean() {
    final XLValue converted = CONVERTER.toXLValue(XLBoolean.class, false);
    assertTrue(converted instanceof XLBoolean);
    final XLBoolean xlBoolean = (XLBoolean) converted;
    assertFalse(xlBoolean.getValue());
  }

  /**
   * Tests the conversion from a {@link XLBoolean}.
   */
  @Test
  public void testConversionFromXLBoolean() {
    Object converted = CONVERTER.toJavaObject(Boolean.TYPE, XLBoolean.FALSE);
    boolean bool = (boolean) converted;
    assertFalse(bool);
    converted = CONVERTER.toJavaObject(Boolean.TYPE, XLBoolean.TRUE);
    bool = (boolean) converted;
    assertTrue(bool);
  }
}
