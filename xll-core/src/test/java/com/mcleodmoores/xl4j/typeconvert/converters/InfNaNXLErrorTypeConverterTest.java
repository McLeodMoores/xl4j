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
import com.mcleodmoores.xl4j.values.XLError;
import com.mcleodmoores.xl4j.values.XLNumber;

/**
 * Unit tests for {@link InfNaNXLErrorTypeConverter}.
 */
public class InfNaNXLErrorTypeConverterTest {
  /** The expected priority */
  private static final int EXPECTED_PRIORITY = 5;
  /** The converter. */
  private static final AbstractTypeConverter CONVERTER = new InfNaNXLErrorTypeConverter();

  /**
   * Tests that the java type is {@link Float}.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLError.class, Double.class));
  }

  /**
   * Tests that the excel type is {@link XLNumber}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(Double.class, XLError.class));
  }

  /**
   * Tests the expected priority.
   */
  @Test
  public void testPriority() {
    assertEquals(CONVERTER.getPriority(), EXPECTED_PRIORITY);
  }

  /**
   * Tests that this class does not allow conversion from Java objects. The Double and Float converters handle this case.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testCannotConvertFromJava() {
    CONVERTER.toXLValue(new Object());
  }

  /**
   * Tests that the error object cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testErrorObjectNotNull() {
    CONVERTER.toJavaObject(Double.class, null);
  }

  /**
   * Tests that DIV0 and NA are converted correctly, with other error types being passed through.
   */
  @Test
  public void testXlErrors() {
    // passthroughs
    Object error = CONVERTER.toJavaObject(Double.class, XLError.Name);
    assertEquals(error, XLError.Name);
    error = CONVERTER.toJavaObject(Double.class, XLError.Null);
    assertEquals(error, XLError.Null);
    error = CONVERTER.toJavaObject(Double.class, XLError.Value);
    assertEquals(error, XLError.Value);
    error = CONVERTER.toJavaObject(Double.class, XLError.Ref);
    assertEquals(error, XLError.Ref);
    error = CONVERTER.toJavaObject(Double.class, XLError.Num);
    assertEquals(error, XLError.Num);
    // infinity and NaN
    error = CONVERTER.toJavaObject(Double.class, XLError.Div0);
    assertEquals(error, Double.POSITIVE_INFINITY);
    error = CONVERTER.toJavaObject(Double.class, XLError.NA);
    assertTrue(error instanceof Double);
    assertTrue(((Double) error).isNaN());
  }
}
