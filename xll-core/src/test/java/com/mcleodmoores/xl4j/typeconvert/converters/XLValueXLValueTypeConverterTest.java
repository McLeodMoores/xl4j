/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.xl4j.typeconvert.converters.XLValueXLValueTypeConverter;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Unit tests for {@link XLValueXLValueTypeConverter}.
 */
public class XLValueXLValueTypeConverterTest {
  /** The converter */
  private static final AbstractTypeConverter CONVERTER = new XLValueXLValueTypeConverter();

  /**
   * Tests that the java type is {@link XLValue}.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLValue.class, XLValue.class));
  }

  /**
   * Tests that the excel type is {@link XLValue}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(XLValue.class, XLValue.class));
  }

  /**
   * Tests the expected priority.
   */
  @Test
  public void testPriority() {
    assertEquals(CONVERTER.getPriority(), 6);
  }

  /**
   * Tests the behaviour when the object to convert is null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void textNullJavaObjectToConvert() {
    CONVERTER.toXLValue(XLValue.class, null);
  }

  /**
   * Tests the behaviour when the object to convert is null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullXlObjectToConvert() {
    CONVERTER.toJavaObject(XLValue.class, null);
  }

  /**
   * Tests that the expected type is ignored.
   */
  @Test
  public void testWrongExpectedTypeIgnored() {
    assertEquals(CONVERTER.toJavaObject(Integer.class, XLString.of("10")), XLString.of("10"));
    assertEquals(CONVERTER.toXLValue(XLNumber.class, XLString.of("10")), XLString.of("10"));
  }

  /**
   * Tests that the object is passed through the converters.
   */
  @Test
  public void testConverters() {
    final XLValue xlValue = XLNumber.of(100);
    assertEquals(CONVERTER.toJavaObject(XLValue.class, xlValue), xlValue);
    assertEquals(CONVERTER.toXLValue(XLValue.class, xlValue), xlValue);
  }
}
