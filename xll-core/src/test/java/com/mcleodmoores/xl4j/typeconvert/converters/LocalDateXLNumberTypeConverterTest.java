/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLBoolean;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Unit tests for {@link LocalDateXLNumberTypeConverterTest}.
 */
@Test
public class LocalDateXLNumberTypeConverterTest {
  /** The Excel epoch year */
  private static final int EXCEL_EPOCH_YEAR = 1900;
  /** The number of days from the Excel epoch */
  private static final long DAYS_FROM_EXCEL_EPOCH = ChronoUnit.DAYS.between(LocalDate.of(EXCEL_EPOCH_YEAR, 1, 1),
      LocalDate.ofEpochDay(0)) + 1;
  /** The number of days from Excel epoch to 2000-01-01 */
  private static final long DAYS = LocalDate.of(2000, 1, 1).toEpochDay() + DAYS_FROM_EXCEL_EPOCH;
  /** XLNumber holding a double representing 2000-01-01. */
  private static final XLNumber XL_DATE = XLNumber.of(DAYS);
  /** Local date. */
  private static final LocalDate LOCAL_DATE = LocalDate.of(2000, 1, 1);
  /** The converter. */
  private static final AbstractTypeConverter CONVERTER = new LocalDateXLNumberTypeConverter();

  /**
   * Tests that the java type is {@link LocalDate}.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLNumber.class, LocalDate.class));
  }

  /**
   * Tests that the Excel type is {@link XLNumber}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(LocalDate.class, XLNumber.class));
  }

  /**
   * Tests the expected priority.
   */
  @Test
  public void testPriority() {
    assertEquals(CONVERTER.getPriority(), 10);
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
    CONVERTER.toJavaObject(null, XL_DATE);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullXLValue() {
    CONVERTER.toJavaObject(LocalDate.class, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(LocalDate.class, XLBoolean.FALSE);
  }

  /**
   * Tests that the expected type is ignored during conversions to Java.
   */
  @Test
  public void testWrongExpectedClassToJavaConversion() {
    assertEquals(CONVERTER.toJavaObject(Integer.class, XLNumber.of(DAYS)), LOCAL_DATE);
  }

  /**
   * Tests for the exception when {@link XLValue} to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(Integer.valueOf(10));
  }

  /**
   * Tests the conversion from a {@link LocalDate}.
   */
  @Test
  public void testConversionFromLocalDate() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(LOCAL_DATE);
    assertTrue(converted instanceof XLNumber);
    final XLNumber xlNumber = (XLNumber) converted;
    assertEquals(xlNumber.getValue(), DAYS, 0);
  }

  /**
   * Tests the conversion from a {@link XLNumber}.
   */
  @Test
  public void testConversionFromXLNumber() {
    final Object converted = CONVERTER.toJavaObject(LocalDate.class, XL_DATE);
    assertTrue(converted instanceof LocalDate);
    final LocalDate localDate = (LocalDate) converted;
    assertEquals(localDate, LOCAL_DATE);
  }

}
