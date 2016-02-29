/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLArray;
import com.mcleodmoores.excel4j.values.XLInteger;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Unit tests for {@link PrimitiveLongArrayXLArrayTypeConverter}.
 */
@Test
public class PrimitiveLongArrayXLArrayTypeConverterTest {
  /** The expected priority */
  private static final long EXPECTED_PRIORITY = 10;
  /** Excel */
  private static final Excel EXCEL = ExcelFactory.getInstance();
  /** The converter. */
  private static final AbstractTypeConverter CONVERTER = new PrimitiveLongArrayXLArrayTypeConverter(EXCEL);

  /**
   * Tests that the java type is long[].
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLArray.class, long[].class));
  }

  /**
   * Tests that the excel type is {@link XLArray}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(long[].class, XLArray.class));
  }

  /**
   * Tests the expected priority.
   */
  @Test
  public void testPriority() {
    assertEquals(CONVERTER.getPriority(), EXPECTED_PRIORITY);
  }

  /**
   * Tests that passing in a null expected type fails because it is not a class type.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullExpectedXLValueClass() {
    CONVERTER.toXLValue(null, new long[] {10});
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullObject() {
    CONVERTER.toXLValue(XLArray.class, null);
  }

  /**
   * Tests that passing in a null expected Java class fails because it is not an array or generic array.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullExpectedClass() {
    CONVERTER.toJavaObject(null, XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(10)}}));
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullXLValue() {
    CONVERTER.toJavaObject(long[].class, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(long[].class, new Long[] {10L});
  }

  /**
   * Tests that the component type (taken from the expected type) is used when trying to find the converter.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testWrongComponentTypeToJavaConversion() {
    CONVERTER.toJavaObject(long[].class, XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(10), XLNumber.of(20)}}));
  }

  /**
   * Tests that passing in an object to convert that is not an array fails.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(XLArray.class, true);
  }

  /**
   * Tests that the expected type must be an XLArray.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testExpectedTypeNotAnArray() {
    CONVERTER.toXLValue(XLInteger.class, new long[] {10, 20});
  }

  /**
   * Tests the conversion from XLArray containing a single row where the input contains only XLNumber.
   */
  @Test
  public void testToJavaConversionFromRow1() {
    final XLArray xlArray = XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(10), XLNumber.of(20), XLNumber.of(30)}});
    final Object converted = CONVERTER.toJavaObject(long[].class, xlArray);
    final long[] array = (long[]) converted;
    assertEquals(array, new long[] {10, 20, 30});
  }

  /**
   * Tests the conversion from XLArray containing a single row where the input contains a mixture of types.
   */
  @Test
  public void testToJavaConversionFromRow2() {
    final XLArray xlArray = XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(10), XLString.of("20"), XLNumber.of(30)}});
    final Object converted = CONVERTER.toJavaObject(long[].class, xlArray);
    final long[] array = (long[]) converted;
    assertEquals(array, new long[] {10, 20, 30});
  }

  /**
   * Tests the conversion from XLArray containing a single column where the input contains only XLNumber.
   */
  @Test
  public void testToJavaConversionFromColumn1() {
    final XLArray xlArray = XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(10)}, new XLValue[] {XLNumber.of(20)}, new XLValue[] {XLNumber.of(30)}});
    final Object converted = CONVERTER.toJavaObject(long[].class, xlArray);
    final long[] array = (long[]) converted;
    assertEquals(array, new long[] {10, 20, 30});
  }

  /**
   * Tests the conversion from XLArray containing a single column where the input contains a mixture of types.
   */
  @Test
  public void testToJavaConversionFromColumn2() {
    final XLArray xlArray = XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(10)}, new XLValue[] {XLString.of("20")}, new XLValue[] {XLNumber.of(30)}});
    final Object converted = CONVERTER.toJavaObject(long[].class, xlArray);
    final long[] array = (long[]) converted;
    assertEquals(array, new long[] {10, 20, 30});
  }

  /**
   * Tests the conversion from long[].
   */
  @Test
  public void testToXLConversionFrom1dPrimitiveLongArray() {
    final long[] array = new long[] {10, 20, 30};
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XLNumber.class, array);
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    assertEquals(xlArray, XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(10), XLNumber.of(20), XLNumber.of(30)}}));
  }

}
