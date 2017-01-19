/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.Excel;
import com.mcleodmoores.xl4j.ExcelFactory;
import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLBoolean;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Unit tests for {@link ObjectArrayXLArrayTypeConverter}.
 */
@Test
public class ObjectArrayXLArrayTypeConverterTest {
  /** Array of Integer */
  private static final Integer[] ARRAY_OF_INTEGER = new Integer[] {Integer.valueOf(10), Integer.valueOf(20)};
  /** Array of Double */
  private static final Double[] ARRAY_OF_DOUBLE = new Double[] {Double.valueOf(10.), Double.valueOf(20.)};
  /** XLObject. */
  private static final XLArray XL_ARRAY_OF_DOUBLE = XLArray.of(new XLValue[][] {{XLNumber.of(10), XLNumber.of(20)}});
  /** An excel object */
  private static final Excel EXCEL = ExcelFactory.getInstance();
  /** The converter */
  private static final AbstractTypeConverter CONVERTER = new ObjectArrayXLArrayTypeConverter(EXCEL);

  /**
   * Tests that the java type is Object[].
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLArray.class, Object[].class));
  }

  /**
   * Tests that the excel type is {@link XLArray}.
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
    assertEquals(CONVERTER.getPriority(), 10);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullObject() {
    CONVERTER.toXLValue(XLArray.class, null);
  }

  /**
   * Tests that passing in a null expected Java class gives the expected exception.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullExpectedClass() {
    CONVERTER.toJavaObject(null, XL_ARRAY_OF_DOUBLE);
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
    CONVERTER.toJavaObject(Object[].class, ARRAY_OF_INTEGER);
  }

  /**
   * Tests for the exception when the expected class is wrong.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testWrongExpectedClassToJavaConversion() {
    CONVERTER.toJavaObject(Double.class, XL_ARRAY_OF_DOUBLE);
  }

  /**
   * Test demonstrates how pointless expectedType is here.
   */
  @Test
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(XLObject.class, ARRAY_OF_INTEGER);
  }

  /**
   * Test demonstrates how pointless expectedType is here.
   */
  @Test
  public void testWrongExpectedClassToXLConversion() {
    CONVERTER.toXLValue(XLObject.class, ARRAY_OF_DOUBLE);
  }

  /**
   * Tests the behaviour when the object is not an array.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testObjectToConvertNotAnArray() {
    CONVERTER.toXLValue(XLArray.class, new Object());
  }

  /**
   * Tests the conversion of an empty array.
   */
  @Test
  public void testConversionOfEmptyArray() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XL_ARRAY_OF_DOUBLE.getClass(), new Double[0]);
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    assertEquals(xlArray.getArray(), new XLValue[1][1]);
  }

  /**
   * Tests the conversion from a Double[].
   */
  @Test
  public void testConversionFromObject() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XL_ARRAY_OF_DOUBLE.getClass(), ARRAY_OF_DOUBLE);
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    assertEquals(xlArray, XL_ARRAY_OF_DOUBLE);
  }

  /**
   * Tests the conversion from a {@link XLArray}.
   */
  @Test
  public void testConversionFromXLArray() {
    final Object converted = CONVERTER.toJavaObject(Double[].class, XL_ARRAY_OF_DOUBLE);
    assertEquals(converted, ARRAY_OF_DOUBLE);
  }

  /**
   * Tests the conversion from a {@link XLArray}.
   */
  @Test
  public void testConversionFromXLArrayToObjectArray() {
    final Object converted = CONVERTER.toJavaObject(Object[].class, XL_ARRAY_OF_DOUBLE);
    assertEquals(converted, ARRAY_OF_DOUBLE);
  }

  /**
   * Tests the conversion from a Integer[].
   */
  @Test
  public void testConversionFromObjectIntegers() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XL_ARRAY_OF_DOUBLE.getClass(), ARRAY_OF_INTEGER);
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    assertEquals(xlArray, XL_ARRAY_OF_DOUBLE);
  }

  /**
   * Tests the conversion from a Boolean[].
   */
  @Test
  public void testConversionFromObjectBooleans() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XLArray.class, new Boolean[] { true });
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    final XLArray booleanArray = XLArray.of(new XLValue[][] { { XLBoolean.TRUE } });
    assertEquals(xlArray, booleanArray);
  }

  /**
   * Tests the conversion from a Boolean[].
   */
  @Test
  public void testConversionFromObjectBooleansMultiple() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XLArray.class, new Boolean[] { true, false, true });
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    final XLArray booleanArray = XLArray.of(new XLValue[][] { { XLBoolean.TRUE, XLBoolean.FALSE, XLBoolean.TRUE } });
    assertEquals(xlArray, booleanArray);
  }

  /**
   * Tests the conversion from a Object[].
   */
  @Test
  public void testConversionFromObjectBooleansObjs() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XLArray.class, new Object[] { Boolean.TRUE });
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    final XLArray booleanArray = XLArray.of(new XLValue[][] { { XLBoolean.TRUE } });
    assertEquals(xlArray, booleanArray);
  }

  /**
   * Tests the conversion from a Object[].
   */
  @Test
  public void testConversionFromObjectBooleansMultipleObjs() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XLArray.class, new Object[] { true, false, true });
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    final XLArray booleanArray = XLArray.of(new XLValue[][] { { XLBoolean.TRUE, XLBoolean.FALSE, XLBoolean.TRUE } });
    assertEquals(xlArray, booleanArray);
  }

  /**
   * Tests the conversion from a {@link XLArray}.
   */
  @Test
  public void testConversionFromXLArrayToObjectArrayBooleans() {
    final XLArray booleanArray = XLArray.of(new XLValue[][] { { XLBoolean.TRUE, XLBoolean.FALSE, XLBoolean.TRUE } });
    final Object converted = CONVERTER.toJavaObject(Object[].class, booleanArray);
    final Object[] results = new Object[] { true, false, true };
    assertEquals(converted, results);
  }

  /**
   * Tests the conversion from a {@link XLArray}.
   */
  @Test
  public void testConversionFromXLArrayToObjectArrayBooleansVertical() {
    final XLArray booleanArray = XLArray.of(new XLValue[][] { { XLBoolean.TRUE }, { XLBoolean.FALSE }, {XLBoolean.TRUE } });
    final Object converted = CONVERTER.toJavaObject(Object[].class, booleanArray);
    final Object[] results = new Object[] { true, false, true };
    assertEquals(converted, results);
  }

  /**
   * Tests the conversion from a Object[].
   */
  @Test
  public void testConversionFromObjectMixedObjs() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XLArray.class, new Object[] { Boolean.TRUE, 10., 1, "test1" });
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    final XLArray expectedArray = XLArray.of(new XLValue[][] { { XLBoolean.TRUE, XLNumber.of(10), XLNumber.of(1), XLString.of("test1") } });
    assertEquals(xlArray, expectedArray);
  }

  /**
   * Tests the conversion from a {@link XLArray}.
   */
  @Test
  public void testConversionFromXLArrayToObjectArrayMixedObjs() {
    final XLArray array = XLArray.of(new XLValue[][] { { XLBoolean.TRUE, XLNumber.of(2), XLString.of("test2") } });
    final Object converted = CONVERTER.toJavaObject(Object[].class, array);
    final Object[] results = new Object[] { true, 2., "test2" };
    assertEquals(converted, results);
  }

  /**
   * Tests the conversion from a {@link XLArray}.
   */
  @Test
  public void testConversionFromXLArrayToObjectArrayMixedObjsVertical() {
    final XLArray array = XLArray.of(new XLValue[][] { { XLBoolean.TRUE }, { XLNumber.of(3) }, {XLString.of("test3") } });
    final Object converted = CONVERTER.toJavaObject(Object[].class, array);
    final Object[] results = new Object[] { true, 3., "test3" };
    assertEquals(converted, results);
  }

}
