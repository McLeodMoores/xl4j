/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLInteger;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Unit tests for {@link ObjectXLObjectTypeConverter}.
 */
@Test
public class ObjectXLObjectTypeConverterTest {
  private static final int EXPECTED_PRIORITY = 5;
  private static final int TEN_I = 10;
  private static final double TEN_D = 10d;
  /** XLObject. */
  private static final XLObject XL_OBJECT = XLObject.of(List.class, 1L);
  /** Empty Object. */
  private static final Object OBJECT = new Object();
  /** The converter */
  private static final AbstractTypeConverter CONVERTER = new ObjectXLObjectTypeConverter();

  // TODO need to set system property test.mode - how to do this?
  /**
   * Tests that the java type is {@link Object}.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLObject.class, Object.class));
  }

  /**
   * Tests that the excel type is {@link XLObject}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(Object.class, XLObject.class));
  }

  /**
   * Tests the expected priority.
   */
  @Test
  public void testPriority() {
    assertEquals(CONVERTER.getPriority(), EXPECTED_PRIORITY);
  }

  /**
   * Tests that passing in a null expected {@link XLValue} class gives the expected exception.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullExpectedXLValueClass() {
    CONVERTER.toXLValue(null, OBJECT);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullObject() {
    CONVERTER.toXLValue(XLObject.class, null);
  }

  /**
   * Tests that passing in a null expected Java class gives the expected exception.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullExpectedClass() {
    CONVERTER.toJavaObject(null, XL_OBJECT);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testNullXLValue() {
    CONVERTER.toJavaObject(Object.class, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(Object.class, XLInteger.of(TEN_I));
  }

  /**
   * Tests for the exception when the expected class is wrong.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongExpectedClassToJavaConversion() {
    CONVERTER.toJavaObject(Double.class, XL_OBJECT);
  }

  /**
   * Tests for the exception when {@link XLValue} to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(XLObject.class, TEN_I);
  }

  /**
   * Tests for the exception when the expected class is wrong.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongExpectedClassToXLConversion() {
    CONVERTER.toXLValue(XLObject.class, TEN_D);
  }

  /**
   * Tests the conversion from a {@link Object}.
   */
  @Test
  public void testConversionFromObject() {
    final XLValue converted = CONVERTER.toXLValue(XL_OBJECT.getClass(), OBJECT);
    assertTrue(converted instanceof XLObject);
    final XLObject xlObject = (XLObject) converted;
    assertEquals(xlObject, XL_OBJECT);
  }

  /**
   * Tests the conversion from a {@link XLObject}.
   */
  @Test
  public void testConversionFromXLObject() {
    final Object converted = CONVERTER.toJavaObject(String.class, XL_OBJECT);
    assertEquals(converted, OBJECT);
  }

}
