/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Unit tests for {@link XLValueArrayXLValueArrayTypeConverter}.
 */
public class XLValueArrayXLValueArrayTypeConverterTest {
  /** The converter */
  private static final AbstractTypeConverter CONVERTER = new XLValueArrayXLValueArrayTypeConverter();

  /**
   * Tests that the java type is {@link XLValue}.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLValue[].class, XLValue[].class));
  }

  /**
   * Tests that the excel type is {@link XLValue}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(XLValue[].class, XLValue[].class));
  }

  /**
   * Tests the expected priority.
   */
  @Test
  public void testPriority() {
    assertEquals(CONVERTER.getPriority(), 7);
  }

  /**
   * Tests the behaviour when the object to convert is null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void textNullJavaObjectToConvert() {
    CONVERTER.toXLValue(XLValue[].class, null);
  }

  /**
   * Tests the behaviour when the object to convert is null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullXlObjectToConvert() {
    CONVERTER.toJavaObject(XLValue[].class, null);
  }

  /**
   * Tests that the expected type is ignored.
   */
  @Test
  public void testWrongExpectedTypeIgnored() {
    final XLValue[] xlValues = new XLValue[] {XLString.of("10")};
    assertEquals(CONVERTER.toJavaObject(XLValue.class, xlValues), xlValues);
    assertEquals(CONVERTER.toXLValue(XLValue.class, xlValues), xlValues);
  }

  /**
   * Tests that the object is passed through the converters.
   */
  @Test
  public void testConverters() {
    final XLValue[] xlValues = new XLValue[] {XLNumber.of(100), XLString.of("100")};
    assertEquals(CONVERTER.toJavaObject(XLValue[].class, xlValues), xlValues);
    assertEquals(CONVERTER.toXLValue(XLValue[].class, xlValues), xlValues);
  }
}
