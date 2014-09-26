/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLArray;
import com.mcleodmoores.excel4j.values.XLBoolean;
import com.mcleodmoores.excel4j.values.XLInteger;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Unit tests for {@link ObjectXLObjectTypeConverter}.
 */
@Test
public class ObjectArrayXLArrayTypeConverterTest {
  private static final int EXPECTED_PRIORITY = 10;
  private static final Integer[] TEN_I = new Integer[] { new Integer(10) };
  private static final Double[] TEN_D = new Double[] { new Double(10d) };
  /** XLObject. */
  private static final XLArray XL_ARRAY = XLArray.of(new XLValue[][] { { XLNumber.of(10d) } });
  /** Empty Object. */
  private static final Object OBJECT = new Object();
  /** The converter */
  private static final AbstractTypeConverter CONVERTER = new ObjectArrayXLArrayTypeConverter(ExcelFactory.getInstance());

  // TODO need to set system property test.mode - how to do this?
  /**
   * Tests that the java type is {@link Object}.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLArray.class, Object[].class));
  }

  /**
   * Tests that the excel type is {@link XLObject}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(Object[].class, XLArray.class));
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
  @Test(expectedExceptions = Excel4JRuntimeException.class)
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
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullExpectedClass() {
    CONVERTER.toJavaObject(null, XL_ARRAY);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullXLValue() {
    CONVERTER.toJavaObject(Object[].class, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(Object[].class, TEN_I);
  }

  /**
   * Tests for the exception when the expected class is wrong.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testWrongExpectedClassToJavaConversion() {
    CONVERTER.toJavaObject(Double.class, XL_ARRAY);
  }

  /**
   * Test demonstrates how pointless expectedType is here.
   */
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(XLObject.class, TEN_I);
  }

  /**
   * Test demonstrates how pointless expectedType is here.
   */
  public void testWrongExpectedClassToXLConversion() {
    CONVERTER.toXLValue(XLObject.class, TEN_D);
  }

  /**
   * Tests the conversion from a {@link Object[]}.
   */
  @Test
  public void testConversionFromObject() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XL_ARRAY.getClass(), TEN_D);
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    assertEquals(xlArray, XL_ARRAY);
  }

  /**
   * Tests the conversion from a {@link XLArray}.
   */
  @Test
  public void testConversionFromXLArray() {
    final Object converted = CONVERTER.toJavaObject(Double[].class, XL_ARRAY);
    assertEquals(converted, TEN_D);
  }
  
  /**
   * Tests the conversion from a {@link XLArray}.
   */
  @Test
  public void testConversionFromXLArrayToObjectArray() {
    final Object converted = CONVERTER.toJavaObject(Object[].class, XL_ARRAY);
    assertEquals(converted, TEN_D);
  }
  
  /**
   * Tests the conversion from a {@link Object[]}.
   */
  @Test
  public void testConversionFromObjectIntegers() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XL_ARRAY.getClass(), TEN_I);
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    assertEquals(xlArray, XL_ARRAY);
  }
  
  /**
   * Tests the conversion from a {@link Object[]}.
   */
  @Test
  public void testConversionFromObjectBooleans() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XLArray.class, new Boolean[] { true });
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    XLArray booleanArray = XLArray.of(new XLValue[][] { { XLBoolean.TRUE } });
    assertEquals(xlArray, booleanArray);
  }
  
  /**
   * Tests the conversion from a {@link Object[]}.
   */
  @Test
  public void testConversionFromObjectBooleansMultiple() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XLArray.class, new Boolean[] { true, false, true });
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    XLArray booleanArray = XLArray.of(new XLValue[][] { { XLBoolean.TRUE, XLBoolean.FALSE, XLBoolean.TRUE } });
    assertEquals(xlArray, booleanArray);
  }
  
  /**
   * Tests the conversion from a {@link Object[]}.
   */
  @Test
  public void testConversionFromObjectBooleansObjs() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XLArray.class, new Object[] { true });
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    XLArray booleanArray = XLArray.of(new XLValue[][] { { XLBoolean.TRUE } });
    assertEquals(xlArray, booleanArray);
  }
  
  /**
   * Tests the conversion from a {@link Object[]}.
   */
  @Test
  public void testConversionFromObjectBooleansMultipleObjs() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XLArray.class, new Object[] { true, false, true });
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    XLArray booleanArray = XLArray.of(new XLValue[][] { { XLBoolean.TRUE, XLBoolean.FALSE, XLBoolean.TRUE } });
    assertEquals(xlArray, booleanArray);
  }

  /**
   * Tests the conversion from a {@link XLArray}.
   */
  @Test
  public void testConversionFromXLArrayToObjectArrayBooleans() {
    XLArray booleanArray = XLArray.of(new XLValue[][] { { XLBoolean.TRUE, XLBoolean.FALSE, XLBoolean.TRUE } });
    final Object converted = CONVERTER.toJavaObject(Object[].class, booleanArray);
    Object[] results = new Object[] { true, false, true };
    assertEquals(converted, results);
  }
  
  /**
   * Tests the conversion from a {@link XLArray}.
   */
  @Test
  public void testConversionFromXLArrayToObjectArrayBooleansVertical() {
    XLArray booleanArray = XLArray.of(new XLValue[][] { { XLBoolean.TRUE }, { XLBoolean.FALSE }, {XLBoolean.TRUE } });
    final Object converted = CONVERTER.toJavaObject(Object[].class, booleanArray);
    Object[] results = new Object[] { true, false, true };
    assertEquals(converted, results);
  }
}
