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
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Unit tests for {@link ObjectArrayXLObjectArrayTypeConverter}.
 */
@Test
public class ObjectArray2DXLArrayTypeConverterTest {
  /** Array of array of Integer */
  private static final Integer[][] ARRAY_OF_INTEGER =
      new Integer[][]{new Integer[]{Integer.valueOf(1), Integer.valueOf(2)}, new Integer[]{Integer.valueOf(3), Integer.valueOf(4)}};
  /** Array of array of Double */
  private static final Double[][] ARRAY_OF_DOUBLE =
      new Double[][]{new Double[]{Double.valueOf(1.), Double.valueOf(2.)}, new Double[]{Double.valueOf(3.), Double.valueOf(4.)}};
  /** XLObject. */
  private static final XLArray XL_ARRAY_OF_DOUBLE =
      XLArray.of(new XLValue[][]{new XLValue[]{XLNumber.of(1), XLNumber.of(2)}, new XLValue[]{XLNumber.of(3), XLNumber.of(4)}});
  /** An excel object */
  private static final Excel EXCEL = ExcelFactory.getInstance();
  /** The converter */
  private static final AbstractTypeConverter CONVERTER;

  static {
    CONVERTER = new ObjectArray2DXLArrayTypeConverter(EXCEL);
  }

  /**
   * Tests that the java type is Object[][].
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLArray.class, Object[][].class));
  }

  /**
   * Tests that the excel type is {@link XLArray}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(Object[][].class, XLArray.class));
  }

  /**
   * Tests the expected priority.
   */
  @Test
  public void testPriority() {
    assertEquals(CONVERTER.getPriority(), 10);
  }

  /**
   * Tests that passing in a null expected {@link XLValue} class gives the expected exception.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullExpectedXLValueClass() {
    CONVERTER.toXLValue(null, ARRAY_OF_DOUBLE);
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
    CONVERTER.toJavaObject(null, XL_ARRAY_OF_DOUBLE);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullXLValue() {
    CONVERTER.toJavaObject(Object[][].class, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(Object[][].class, ARRAY_OF_INTEGER);
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
    CONVERTER.toXLValue(XLArray.class, ARRAY_OF_INTEGER);
  }

  /**
   * Test demonstrates how pointless expectedType is here.
   */
  @Test
  public void testWrongExpectedClassToXLConversion() {
    CONVERTER.toXLValue(XLArray.class, ARRAY_OF_DOUBLE);
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
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XL_ARRAY_OF_DOUBLE.getClass(), new Double[0][0]);
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    assertEquals(xlArray.getArray(), new XLValue[0][0]);
  }

  /**
   * Tests the conversion from a Double[][].
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
    final Object converted = CONVERTER.toJavaObject(Double[][].class, XL_ARRAY_OF_DOUBLE);
    assertEquals(converted, ARRAY_OF_DOUBLE);
  }

  /**
   * Tests the conversion from a {@link XLArray}.
   */
  @Test
  public void testConversionFromXLArrayToObjectArray() {
    final Object converted = CONVERTER.toJavaObject(Object[][].class, XL_ARRAY_OF_DOUBLE);
    assertEquals(converted, ARRAY_OF_DOUBLE);
  }

  /**
   * Tests the conversion from a Integer[][].
   */
  @Test
  public void testConversionFromObjectIntegers() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XL_ARRAY_OF_DOUBLE.getClass(), ARRAY_OF_INTEGER);
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    assertEquals(xlArray, XL_ARRAY_OF_DOUBLE);
  }

  /**
   * Tests the conversion from a Boolean[][].
   */
  @Test
  public void testConversionFromObjectBooleans() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XLArray.class, new Boolean[][]{new Boolean[]{true}, new Boolean[]{false}});
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    final XLArray booleanArray = XLArray.of(new XLValue[][]{new XLValue[]{XLBoolean.TRUE}, new XLValue[]{XLBoolean.FALSE}});
    assertEquals(xlArray, booleanArray);
  }

  //TODO test non-rectangular arrays

  /**
   * Tests the conversion from a Boolean[][].
   */
  @Test
  public void testConversionFromObjectBooleansMultiple() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XLArray.class, new Boolean[][]{new Boolean[] {true, false}, new Boolean[]{true, true}});
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    final XLArray booleanArray = XLArray.of(new XLValue[][]{new XLValue[]{XLBoolean.TRUE, XLBoolean.FALSE}, new XLValue[]{XLBoolean.TRUE, XLBoolean.TRUE}});
    assertEquals(xlArray, booleanArray);
  }

  /**
   * Tests the conversion from a Object[][].
   */
  @Test
  public void testConversionFromObjectBooleansObjs() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XLArray.class, new Object[][]{new Object[] {Boolean.TRUE}});
    assertTrue(converted instanceof XLArray);
    final XLValue[][] xlArray = ((XLArray) converted).getArray();
    final XLValue[][] expectedArray = new XLValue[][] {new XLValue[] {XLBoolean.TRUE}};
    assertEquals(xlArray, expectedArray);
  }

  /**
   * Tests the conversion from a Object[][].
   */
  @Test
  public void testConversionFromObjectBooleansMultipleObjs() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XLArray.class, new Object[][] {new Object[] {true, false}, new Object[]{false, false}});
    assertTrue(converted instanceof XLArray);
    final XLValue[][] xlArray = ((XLArray) converted).getArray();
    final XLValue[][] expectedArray = new XLValue[][] {new XLValue[] {XLBoolean.TRUE, XLBoolean.FALSE}, new XLValue[] {XLBoolean.FALSE, XLBoolean.FALSE}};
    assertEquals(xlArray, expectedArray);
  }

  /**
   * Tests the conversion from a {@link XLArray}.
   */
  @Test
  public void testConversionFromXLArrayToObjectArrayBooleans() {
    final XLArray booleanArray = XLArray.of(new XLValue[][] {{XLBoolean.TRUE, XLBoolean.FALSE, XLBoolean.TRUE}});
    final Object converted = CONVERTER.toJavaObject(Object[].class, booleanArray);
    final Object[][] results = new Object[][] {new Object[]{true, false, true}};
    assertEquals(converted, results);
  }

  /**
   * Tests the conversion from a {@link XLArray}.
   */
  @Test
  public void testConversionFromXLArrayToObjectArrayBooleansVertical() {
    final XLArray booleanArray = XLArray.of(new XLValue[][] {new XLValue[]{XLBoolean.TRUE}, new XLValue[]{XLBoolean.FALSE}, new XLValue[]{XLBoolean.TRUE}});
    final Object converted = CONVERTER.toJavaObject(Object[][].class, booleanArray);
    final Object[][] results = new Object[][] {new Object[]{true}, new Object[]{false}, new Object[]{true}};
    assertEquals(converted, results);
  }

  /**
   * Tests the conversion from a Object[][].
   */
  @Test
  public void testConversionFromObjectMixedObjs() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XLArray.class, new Object[][] {new Object[]{Boolean.TRUE, 10.}, new Object[]{1, "test1"}});
    assertTrue(converted instanceof XLArray);
    final XLValue[][] expectedArray = new XLValue[][] {new XLValue[] {XLBoolean.TRUE, XLNumber.of(10)}, new XLValue[] {XLNumber.of(1), XLString.of("test1")}};
    assertEquals(((XLArray) converted).getArray(), expectedArray);
  }

  /**
   * Tests the conversion from a {@link XLArray}.
   */
  @Test
  public void testConversionFromXLArrayToObjectArrayMixedObjs() {
    final XLArray array = XLArray.of(new XLValue[][] {{XLBoolean.TRUE, XLNumber.of(2), XLString.of("test2")}});
    final Object converted = CONVERTER.toJavaObject(Object[][].class, array);
    final Object[][] results = new Object[][] {new Object[] {true, 2., "test2"}};
    assertEquals(converted, results);
  }

  /**
   * Tests the conversion from a {@link XLArray}.
   */
  @Test
  public void testConversionFromXLArrayToObjectArrayMixedObjsVertical() {
    final XLArray array = XLArray.of(new XLValue[][] {{XLBoolean.TRUE}, {XLNumber.of(3)}, {XLString.of("test3")}});
    final Object converted = CONVERTER.toJavaObject(Object[][].class, array);
    final Object[][] results = new Object[][] { new Object[]{true}, new Object[]{3.}, new Object[]{"test3"}};
    assertEquals(converted, results);
  }
}