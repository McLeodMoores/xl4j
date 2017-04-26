/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.v1.api.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLBoolean;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.simulator.MockFunctionProcessor;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

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
  /** The function processor */
  private static final MockFunctionProcessor PROCESSOR = MockFunctionProcessor.getInstance();
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
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullObject() {
    CONVERTER.toXLValue(null);
  }

  /**
   * Tests that passing in a null expected Java class gives the expected exception.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullExpectedClass() {
    CONVERTER.toJavaObject(null, XL_ARRAY_OF_DOUBLE);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
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
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testWrongExpectedClassToJavaConversion() {
    CONVERTER.toJavaObject(Double.class, XL_ARRAY_OF_DOUBLE);
  }

  /**
   * Tests the behaviour when the object is not an array.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testObjectToConvertNotAnArray() {
    CONVERTER.toXLValue(new Object());
  }

  /**
   * Tests the conversion of an empty array.
   */
  @Test
  public void testConversionOfEmptyArray() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(new Double[0]);
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    assertEquals(xlArray.getArray(), new XLValue[1][1]);
  }

  /**
   * Tests the conversion from a Double[].
   */
  @Test
  public void testConversionFromObject() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(ARRAY_OF_DOUBLE);
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
    final XLValue converted = (XLValue) CONVERTER.toXLValue(ARRAY_OF_INTEGER);
    assertTrue(converted instanceof XLArray);
    final XLArray xlArray = (XLArray) converted;
    assertEquals(xlArray, XL_ARRAY_OF_DOUBLE);
  }

  /**
   * Tests the conversion from a Boolean[].
   */
  @Test
  public void testConversionFromObjectBooleans() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(new Boolean[] { true });
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
    final XLValue converted = (XLValue) CONVERTER.toXLValue(new Boolean[] { true, false, true });
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
    final XLValue converted = (XLValue) CONVERTER.toXLValue(new Object[] { Boolean.TRUE });
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
    final XLValue converted = (XLValue) CONVERTER.toXLValue(new Object[] { true, false, true });
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
    final XLValue converted = (XLValue) CONVERTER.toXLValue(new Object[] { Boolean.TRUE, 10., 1, "test1" });
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

  /**
   * Tests a parameterized function that takes a generic array of list.
   */
  @Test
  public void testArrayOfGenericList() {
    final XLArray numberArray = XLArray.of(new XLValue[][] {new XLValue[] {
        XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(1), XLNumber.of(-2), XLString.of("3")}}),
        XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(4), XLNumber.of(-5)}}),
        XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(6)}})
    }});
    final Object result = PROCESSOR.invoke("GenericsTestHelper.f0", numberArray);
    assertTrue(result instanceof XLNumber);
    final double expectedResult = 1 - 2 + 3 + 4 - 5 + 6;
    assertEquals(((XLNumber) result).getAsDouble(), expectedResult, 1e-15);
  }

  /**
   * Tests DoubleConstOperation -> Operation<Double, Double> and QuadrupleConstOperation -> Operation<Double, U extends Number>.
   * The generic bound is matched by finding a class or superclass that can be assigned from the parameterized type.
   */
  @Test
  public void testArrayOfGenericListAndOperations() {
    final XLArray numberArray = XLArray.of(new XLValue[][] {new XLValue[] {
        XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(1), XLNumber.of(-2)}}),
        XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(3), XLNumber.of(-6)}})}});
    final XLArray operationArguments = XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(8), XLString.of("5")}});
    final Object result = PROCESSOR.invoke("GenericsTestHelper.f1", numberArray, operationArguments);
    assertTrue(result instanceof XLNumber);
    final double expectedResult = (1 - 2 + 3 - 6) * (8 * 2 + 2 * 5 * 2);
    assertEquals(((XLNumber) result).getAsDouble(), expectedResult, 1e-15);
  }

  /**
   * Tests SumAndScaleAsFloatOperation -> ToString. There is no available converter for the other interface that is
   * implemented (Operation&lt;U[], Float&gt;).
   */
  @Test
  public void testBothInterfacesChecked() {
    final XLArray numberInputs = XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(-1), XLNumber.of(-2)}});
    final XLArray operationArguments = XLArray.of(new XLValue[][] {new XLValue[] {XLString.of("4"), XLString.of("6"), XLString.of("8")}});
    Object result = PROCESSOR.invoke("GenericsTestHelper.f2", operationArguments);
    assertTrue(result instanceof XLString);
    final String expectedString = "Sum and scale as Float, Sum and scale as Float, Sum and scale as Float";
    assertEquals(((XLString) result).getValue(), expectedString);
    result = PROCESSOR.invoke("GenericsTestHelper.f3", numberInputs, operationArguments);
    assertTrue(result instanceof XLNumber);
    final float expectedResult = (-1 + -2) * (4 + 6 + 8);
    assertEquals(((XLNumber) result).getAsFloat(), expectedResult, 1e-8);
  }

  /**
   * Test function.
   * @param array
   *        an array
   * @return
   *        the result of the function
   */
  @XLFunction(name = "ObjectArrayTest1")
  public static BigDecimal[] objectArrayTest1(@XLParameter final Double[] array) {
    final BigDecimal[] result = new BigDecimal[array.length];
    int i = 0;
    for (final Double t : array) {
      result[i++] = BigDecimal.valueOf(t.doubleValue());
    }
    return result;
  }

  /**
   * Test function that returns a parameterized array.
   * @param <U>
   *          the type in the return array
   * @param array
   *          an array
   * @return
   *          the result of the function
   */
  @SuppressWarnings("unchecked")
  @XLFunction(name = "ObjectArrayTest2")
  public static <U extends Number> U[] objectArrayTest2(@XLParameter final Double[] array) {
    final BigDecimal[] result = new BigDecimal[array.length];
    int i = 0;
    for (final Double t : array) {
      result[i++] = BigDecimal.valueOf(t.doubleValue());
    }
    return (U[]) result;
  }

  /**
   * Test function that takes and returns a parameterized array.
   * @param <T>
   *          the type in the input array
   * @param <U>
   *          the type in the return array
   * @param array
   *          an array
   * @return
   *          the result of the function
   */
  @SuppressWarnings("unchecked")
  @XLFunction(name = "ObjectArrayTest3")
  public static <T extends Number, U extends Number> U[] objectArrayTest3(@XLParameter final T[] array) {
    final Number[] result = new Number[array.length];
    int i = 0;
    for (final T t : array) {
      result[i++] = BigDecimal.valueOf(t.doubleValue());
    }
    return (U[]) result;
  }

}
