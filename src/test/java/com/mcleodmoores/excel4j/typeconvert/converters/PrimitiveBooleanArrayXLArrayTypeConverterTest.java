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
import com.mcleodmoores.excel4j.values.XLBoolean;
import com.mcleodmoores.excel4j.values.XLInteger;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Unit tests for {@link PrimitiveBooleanArrayXLArrayTypeConverter}.
 */
@Test
public class PrimitiveBooleanArrayXLArrayTypeConverterTest {
  /** The expected priority */
  private static final int EXPECTED_PRIORITY = 10;
  /** Excel */
  private static final Excel EXCEL = ExcelFactory.getInstance();
  /** The converter. */
  private static final AbstractTypeConverter CONVERTER = new PrimitiveBooleanArrayXLArrayTypeConverter(EXCEL);

  /**
   * Tests that the java type is boolean[].
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLArray.class, boolean[].class));
  }

  /**
   * Tests that the excel type is {@link XLArray}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(boolean[].class, XLArray.class));
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
    CONVERTER.toXLValue(null, new boolean[] {false});
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
    CONVERTER.toJavaObject(null, XLArray.of(new XLValue[][] {new XLValue[] {XLBoolean.FALSE}}));
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullXLValue() {
    CONVERTER.toJavaObject(boolean[].class, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(boolean[].class, new Boolean[] {Boolean.TRUE});
  }

  /**
   * Tests that the component type (taken from the expected type) is used when trying to find the converter.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testWrongComponentTypeToJavaConversion() {
    CONVERTER.toJavaObject(int[].class, XLArray.of(new XLValue[][] {new XLValue[] {XLBoolean.FALSE, XLBoolean.TRUE}}));
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
    CONVERTER.toXLValue(XLInteger.class, new boolean[] {true, false});
  }

  /**
   * Tests the behaviour when there is no available converter to a boolean.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNoConverterToBooleanRow() {
    final XLArray xlArray = XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(0), XLNumber.of(1)}});
    CONVERTER.toJavaObject(boolean[].class, xlArray);
  }

  /**
   * Tests the behaviour when there is no available converter to a boolean.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNoConverterToBooleanColumn() {
    final XLArray xlArray = XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(0)}, new XLValue[]{XLNumber.of(1)}});
    CONVERTER.toJavaObject(boolean[].class, xlArray);
  }

  /**
   * Tests the conversion from XLArray containing a single row where the input contains only XLBoolean.
   */
  @Test
  public void testToJavaConversionFromRow1() {
    final XLArray xlArray = XLArray.of(new XLValue[][] {new XLValue[] {XLBoolean.TRUE, XLBoolean.FALSE, XLBoolean.TRUE}});
    final Object converted = CONVERTER.toJavaObject(boolean[].class, xlArray);
    final boolean[] array = (boolean[]) converted;
    assertEquals(array, new boolean[] {true, false, true});
  }

  /**
   * Tests the conversion from XLArray containing a single row where the input contains a mixture of types.
   */
  @Test
  public void testToJavaConversionFromRow2() {
    final XLArray xlArray = XLArray.of(new XLValue[][] {new XLValue[] {XLBoolean.TRUE, XLString.of("False"), XLBoolean.TRUE}});
    final Object converted = CONVERTER.toJavaObject(boolean[].class, xlArray);
    final boolean[] array = (boolean[]) converted;
    assertEquals(array, new boolean[] {true, false, true});
  }

  /**
   * Tests the conversion from XLArray containing a single column where the input contains only XLBoolean.
   */
  @Test
  public void testToJavaConversionFromColumn1() {
    final XLArray xlArray = XLArray.of(new XLValue[][] {new XLValue[] {XLBoolean.TRUE}, new XLValue[] {XLBoolean.FALSE}, new XLValue[] {XLBoolean.TRUE}});
    final Object converted = CONVERTER.toJavaObject(boolean[].class, xlArray);
    final boolean[] array = (boolean[]) converted;
    assertEquals(array, new boolean[] {true, false, true});
  }

  /**
   * Tests the conversion from XLArray containing a single column where the input contains a mixture of types.
   */
  @Test
  public void testToJavaConversionFromColumn2() {
    final XLArray xlArray = XLArray.of(new XLValue[][] {new XLValue[] {XLBoolean.TRUE}, new XLValue[] {XLString.of("False")}, new XLValue[] {XLBoolean.TRUE}});
    final Object converted = CONVERTER.toJavaObject(boolean[].class, xlArray);
    final boolean[] array = (boolean[]) converted;
    assertEquals(array, new boolean[] {true, false, true});
  }

  /**
   * Tests the conversion from boolean[].
   */
  @Test
  public void testToXLConversionFrom1dPrimitiveBooleanArray() {
    final boolean[] array = new boolean[] {true, false, false};
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XLBoolean.class, array);
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    assertEquals(xlArray, XLArray.of(new XLValue[][] {new XLValue[] {XLBoolean.TRUE, XLBoolean.FALSE, XLBoolean.FALSE}}));
  }

}
