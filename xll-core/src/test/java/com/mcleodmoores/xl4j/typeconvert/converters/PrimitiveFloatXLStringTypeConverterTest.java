/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLBoolean;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Unit tests for {@link PrimitiveFloatXLStringTypeConverter}.
 */
@Test
public class PrimitiveFloatXLStringTypeConverterTest {
  /** The converter. */
  private static final AbstractTypeConverter CONVERTER = new PrimitiveFloatXLStringTypeConverter();

  /**
   * Tests that the java type is float.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLString.class, Float.TYPE));
  }

  /**
   * Tests that the excel type is {@link XLString}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(Float.TYPE, XLString.class));
  }

  /**
   * Tests the expected priority.
   */
  @Test
  public void testPriority() {
    assertEquals(CONVERTER.getPriority(), -1);
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
    CONVERTER.toJavaObject(null, XLString.of("3"));
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullXLValue() {
    CONVERTER.toJavaObject(Float.TYPE, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(Float.TYPE, XLBoolean.FALSE);
  }

  /**
   * Tests that the expected type is ignored during conversions to Java.
   */
  @Test
  public void testWrongExpectedClassToJavaConversion() {
    assertEquals(CONVERTER.toJavaObject(Integer.class, XLString.of("2")), 2f);
  }

  /**
   * Tests for the exception when the {@link XLValue} to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(Integer.valueOf(10));
  }

  /**
   * Tests the conversion from a float.
   */
  @Test
  public void testConversionFromFloat() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(2f);
    assertTrue(converted instanceof XLString);
    final XLString xlString = (XLString) converted;
    assertEquals(xlString.getValue(), Float.valueOf(2f).toString());
  }

  /**
   * Tests the conversion from a {@link XLString}.
   */
  @Test
  public void testConversionFromXLString() {
    final Object converted = CONVERTER.toJavaObject(Float.TYPE, XLString.of(Float.valueOf(3f).toString()));
    assertTrue(converted instanceof Float);
    final Float floa = (Float) converted;
    assertEquals(floa, 3f);
  }
}
