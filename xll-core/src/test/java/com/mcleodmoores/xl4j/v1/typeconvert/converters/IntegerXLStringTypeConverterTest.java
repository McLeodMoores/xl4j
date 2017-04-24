/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.v1.api.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.xl4j.v1.api.values.XLBoolean;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link IntegerXLStringTypeConverter}.
 */
@Test
public class IntegerXLStringTypeConverterTest {
  /** The converter. */
  private static final AbstractTypeConverter CONVERTER = new IntegerXLStringTypeConverter();

  /**
   * Tests that the java type is int.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLString.class, Integer.class));
  }

  /**
   * Tests that the excel type is {@link XLString}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(Integer.class, XLString.class));
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
  @Test(expectedExceptions = XL4JRuntimeException.class)
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
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullXLValue() {
    CONVERTER.toJavaObject(Integer.class, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(Integer.class, XLBoolean.FALSE);
  }

  /**
   * Tests that the expected type is ignored during conversions to Java.
   */
  @Test
  public void testWrongExpectedClassToJavaConversion() {
    assertEquals(CONVERTER.toJavaObject(Integer.class, XLString.of("2")), 2);
  }

  /**
   * Tests for the exception when the {@link XLValue} to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(Long.valueOf(10));
  }

  /**
   * Tests the conversion from a int.
   */
  @Test
  public void testConversionFromInteger() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(2);
    assertTrue(converted instanceof XLString);
    final XLString xlString = (XLString) converted;
    assertEquals(xlString.getValue(), Integer.valueOf(2).toString());
  }

  /**
   * Tests the conversion from a {@link XLString}.
   */
  @Test
  public void testConversionFromXLString() {
    final Object converted = CONVERTER.toJavaObject(Integer.class, XLString.of(Integer.valueOf(3).toString()));
    assertTrue(converted instanceof Integer);
    final Integer integer = (Integer) converted;
    assertEquals(integer, Integer.valueOf(3));
  }
}
