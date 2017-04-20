/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.v1.api.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.xl4j.v1.api.values.XLBoolean;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveBooleanXLBooleanTypeConverter;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link PrimitiveBooleanXLBooleanTypeConverter}.
 */
@Test
public class PrimitiveBooleanXLBooleanTypeConverterTest {
  /** The expected priority */
  private static final int EXPECTED_PRIORITY = 10;
  /** Integer */
  private static final int TEN_I = 10;
  /** Double */
  private static final double TEN_D = 10d;
  /** The converter. */
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
    CONVERTER.toJavaObject(null, XLBoolean.FALSE);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
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
   * Tests that the expected type is ignored during conversions to Java.
   */
  @Test
  public void testWrongExpectedClassToJavaConversion() {
    assertEquals(CONVERTER.toJavaObject(Integer.TYPE, XLBoolean.FALSE), Boolean.FALSE);
  }

  /**
   * Tests for the exception when {@link XLValue} to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(Integer.valueOf(TEN_I));
  }

  /**
   * Tests the conversion from a {@link Boolean}.
   */
  @Test
  public void testConversionFromBoolean() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(false);
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
