/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.Excel;
import com.mcleodmoores.xl4j.ExcelFactory;
import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLInteger;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Unit tests for {@link ObjectXLObjectTypeConverter}.
 */
@Test
public class ObjectXLObjectTypeConverterTest {
  private static final int EXPECTED_PRIORITY = 5;
  private static final int TEN_I = 10;
  /** An Excel object */
  private static final Excel EXCEL = ExcelFactory.getInstance();
  /** The converter */
  private static final AbstractTypeConverter CONVERTER = new ObjectXLObjectTypeConverter(EXCEL);

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
    final List<?> object = new ArrayList<>();
    final long objectHandle = EXCEL.getHeap().getHandle(object);
    final XLObject xlObject = XLObject.of(object.getClass().getSimpleName(), objectHandle);
    CONVERTER.toJavaObject(null, xlObject);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
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
  @Test
  public void testWrongExpectedClassToJavaConversion() {
    final List<?> object = new ArrayList<>();
    final long objectHandle = EXCEL.getHeap().getHandle(object);
    final XLObject xlObject = XLObject.of(object.getClass().getSimpleName(), objectHandle);
    assertEquals(CONVERTER.toJavaObject(Double.class, xlObject), object);
  }

  /**
   * Tests that the expected type is ignored during conversion to a XL class.
   */
  @Test
  public void testWrongExpectedClassToXLConversion() {
    final List<?> object = new ArrayList<>();
    final long objectHandle = EXCEL.getHeap().getHandle(object);
    final XLObject xlObject = XLObject.of(object.getClass().getSimpleName(), objectHandle);
    assertEquals(CONVERTER.toXLValue(object), xlObject);
  }

  /**
   * Tests the conversion from a {@link Object}.
   */
  @Test
  public void testConversionFromObject() {
    final List<?> object = new ArrayList<>();
    final long objectHandle = EXCEL.getHeap().getHandle(object);
    final XLObject xlObject = XLObject.of(object.getClass().getSimpleName(), objectHandle);
    final XLValue converted = (XLValue) CONVERTER.toXLValue(object);
    assertTrue(converted instanceof XLObject);
    final XLObject convertedXlObject = (XLObject) converted;
    assertEquals(convertedXlObject, xlObject);
  }

  /**
   * Tests the conversion from a {@link XLObject}.
   */
  @Test
  public void testConversionFromXLObject() {
    final List<?> object = new ArrayList<>();
    final long objectHandle = EXCEL.getHeap().getHandle(object);
    final XLObject xlObject = XLObject.of(object.getClass().getSimpleName(), objectHandle);
    final Object converted = CONVERTER.toJavaObject(ArrayList.class, xlObject);
    assertEquals(converted, object);
  }

}
