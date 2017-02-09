/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Unit tests for {@link PrimitiveBooleanXLStringTypeConverter}.
 */
@Test
public class PrimitiveBooleanXLStringTypeConverterTest {
  /** The converter. */
  private static final AbstractTypeConverter CONVERTER = new PrimitiveBooleanXLStringTypeConverter();

  /**
   * Tests that the java type is boolean.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLString.class, Boolean.TYPE));
  }

  /**
   * Tests that the excel type is {@link XLString}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(Boolean.TYPE, XLString.class));
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
    CONVERTER.toJavaObject(null, XLString.of("true"));
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
    CONVERTER.toJavaObject(Boolean.TYPE, XLNumber.of(10));
  }

  /**
   * Tests that the expected type is ignored during conversions to Java.
   */
  @Test
  public void testWrongExpectedClassToJavaConversion() {
    assertEquals(CONVERTER.toJavaObject(Integer.class, XLString.of("falSe")), false);
  }

  /**
   * Tests for the exception when the {@link XLValue} to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(Integer.valueOf(10));
  }

  /**
   * Tests the conversion from a boolean.
   */
  @Test
  public void testConversionFromBoolean() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(false);
    assertTrue(converted instanceof XLString);
    final XLString xlString = (XLString) converted;
    assertEquals(xlString.getValue(), Boolean.FALSE.toString());
  }

  /**
   * Tests the conversion from a {@link XLString}.
   */
  @Test
  public void testConversionFromXLString() {
    Object converted = CONVERTER.toJavaObject(Boolean.TYPE, XLString.of(Boolean.FALSE.toString()));
    assertTrue(converted instanceof Boolean);
    Boolean bool = (Boolean) converted;
    assertFalse(bool);
    converted = CONVERTER.toJavaObject(Boolean.TYPE, XLString.of(Boolean.TRUE.toString()));
    assertTrue(converted instanceof Boolean);
    bool = (Boolean) converted;
    assertTrue(bool);
  }
}
