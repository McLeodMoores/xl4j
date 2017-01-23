/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.awt.Dimension;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.Excel;
import com.mcleodmoores.xl4j.ExcelFactory;
import com.mcleodmoores.xl4j.XLFunction;
import com.mcleodmoores.xl4j.XLParameter;
import com.mcleodmoores.xl4j.simulator.MockFunctionProcessor;
import com.mcleodmoores.xl4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.xl4j.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.util.XlDateUtils;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLError;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLValue;
import com.mcleodmoores.xl4j.values.XLValueVisitor;

/**
 * Unit tests for {@link Map2XLArrayTypeConverter}.
 */
public class Map2XLArrayTypeConverterTest {
  private static final int N = 50;
  private static final XLValue[][] ROW1 = new XLValue[2][N];
  private static final XLValue[][] ROW2 = new XLValue[2][N];
  private static final XLValue[][] ROW3 = new XLValue[2][N];
  private static final XLValue[][] ROW4 = new XLValue[2][N];
  private static final XLValue[][] COLUMN1 = new XLValue[N][2];
  private static final XLValue[][] COLUMN2 = new XLValue[N][2];
  private static final XLValue[][] COLUMN3 = new XLValue[N][2];
  private static final XLValue[][] COLUMN4 = new XLValue[N][2];
  private static final Map<LocalDate, Double> M1 = new LinkedHashMap<>();
  private static final Map M2 = new LinkedHashMap();
  private static final Map<LocalDate, Double[]> M3 = new LinkedHashMap<>();
  private static final Map<LocalDate, List<? extends Double>> M4 = new LinkedHashMap<>();
  private static final Excel EXCEL = ExcelFactory.getInstance();
  private static final TypeConverter CONVERTER = new Map2XLArrayTypeConverter(EXCEL);
  private static final MockFunctionProcessor PROCESSOR = MockFunctionProcessor.getInstance();

  static {
    for (int i = 0; i < N; i++) {
      final LocalDate date = LocalDate.now().plusDays(i);
      final double primitiveDouble = Math.random();
      //TODO change when we allow nulls
      //      final Number objectNumber = i % 2 == 0 ? primitiveDouble + 0.5 : null;
      final Number objectNumber = i % 2 == 0 ? primitiveDouble + 0.5 : primitiveDouble * 3;
      ROW1[0][i] = XLNumber.of(XlDateUtils.getDaysFromXlEpoch(date));
      ROW1[1][i] = XLNumber.of(primitiveDouble);
      ROW2[0][i] = ROW1[0][i];
      //      ROW2[1][i] = objectNumber != null ? XLNumber.of(objectNumber.doubleValue()) : null;
      ROW2[1][i] = XLNumber.of(objectNumber.doubleValue());
      ROW3[0][i] = ROW1[0][i];
      ROW3[1][i] = XLArray.of(new XLValue[][] {new XLValue[] {ROW1[1][i], XLNumber.of(primitiveDouble * 15)}});
      ROW4[0][i] = ROW1[0][i];
      ROW4[1][i] = XLArray.of(new XLValue[][] {new XLValue[] {ROW1[1][i], XLNumber.of(primitiveDouble * 15)}});
      COLUMN1[i][0] = ROW1[0][i];
      COLUMN1[i][1] = ROW1[1][i];
      COLUMN2[i][0] = ROW2[0][i];
      COLUMN2[i][1] = ROW2[1][i];
      COLUMN3[i][0] = ROW3[0][i];
      COLUMN3[i][1] = ROW3[1][i];
      COLUMN4[i][0] = ROW3[0][i];
      COLUMN4[i][1] = ROW3[1][i];
      M1.put(date, primitiveDouble);
      M2.put(date, objectNumber);
      M3.put(date, new Double[] {primitiveDouble, primitiveDouble * 15});
      M4.put(date, Arrays.asList(primitiveDouble, primitiveDouble * 15));
    }
  }

  /**
   * Tests the type mapping.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLArray.class, Map.class));
  }

  /**
   * Tests the type mapping.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(Map.class, XLArray.class));
  }

  /**
   * Tests the priority.
   */
  @Test
  public void testPriority() {
    assertEquals(CONVERTER.getPriority(), 6);
  }

  /**
   * Tests that the object to convert cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullXlValue() {
    CONVERTER.toJavaObject(Map.class, null);
  }

  /**
   * Tests that the object to convert cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullObject() {
    CONVERTER.toXLValue(XLArray.class, null);
  }

  /**
   * Test that the expected type cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullExpectedType() {
    CONVERTER.toXLValue(null, M1);
  }

  /**
   * Tests that the expected type must be a class.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testExpectedTypeNotAClass() {
    CONVERTER.toXLValue(new GenericArrayType() {
      @Override
      public Type getGenericComponentType() {
        return null;
      }
    }, M1);
  }

  /**
   * Tests that the type to convert must be a Map.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNotAMap() {
    CONVERTER.toXLValue(XLArray.class, new Object());
  }

  /**
   * Tests the behaviour when a key converter could not be found.
   */
  @Test
  public void testNoKeyConverter() {
    assertEquals(PROCESSOR.invoke("NoKeyConverter", XLArray.of(new XLValue[][] {new XLValue[] {TestXlValue.of(120, 130), XLNumber.of(43000.)}})), XLError.Null);
  }

  /**
   * Tests the behaviour when a value converter could not be found.
   */
  @Test
  public void testNoValueConverter() {
    assertEquals(PROCESSOR.invoke("NoValueConverter", XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(43000.), TestXlValue.of(120, 130)}})),
        XLError.Null);
  }

  /**
   * Tests an empty map.
   */
  @Test
  public void testEmptyMap() {
    final Object result = CONVERTER.toXLValue(XLArray.class, new HashMap<>());
    assertTrue(result instanceof XLArray);
    final XLArray xlArray = (XLArray) result;
    assertEquals(xlArray.getArray().length, 1);
    assertEquals(xlArray.getArray()[0].length, 1);
    assertEquals(xlArray.getArray()[0][0], null);
  }

  /**
   * Tests the conversion of Map<LocalDate, Double> to an XLArray.
   */
  @Test
  public void testConvertMap1() {
    final Object result = CONVERTER.toXLValue(XLArray.class, M1);
    assertTrue(result instanceof XLArray);
    final XLValue[][] xlArray = ((XLArray) result).getArray();
    final Iterator<Map.Entry<LocalDate, Double>> iter = M1.entrySet().iterator();
    for (int i = 0; i < N; i++) {
      final Map.Entry<LocalDate, Double> entry = iter.next();
      assertTrue(xlArray[i][0] instanceof XLNumber);
      assertTrue(xlArray[i][1] instanceof XLNumber);
      assertEquals(((XLNumber) xlArray[i][0]).getAsDouble(), XlDateUtils.getDaysFromXlEpoch(entry.getKey()), 1e-15);
      assertEquals(((XLNumber) xlArray[i][1]).getAsDouble(), entry.getValue().doubleValue(), 1e-15);
    }
  }

  /**
   * Tests conversion to Map<? extends LocalDate, ? super Double>.
   */
  @Test
  public void testConvertArray1() {
    assertEquals(((XLNumber) PROCESSOR.invoke("MapTest1", XLArray.of(ROW1))).getAsInt(), -1);
    assertEquals(((XLNumber) PROCESSOR.invoke("MapTest1", XLArray.of(COLUMN1))).getAsInt(), -1);
    assertEquals(((XLNumber) PROCESSOR.invoke("MapTest1_varargs", XLArray.of(ROW1))).getAsInt(), -11);
    assertEquals(((XLNumber) PROCESSOR.invoke("MapTest1_varargs", XLArray.of(COLUMN1))).getAsInt(), -11);
  }

  /**
   * Tests the conversion of Map (i.e. a raw type) to an XLArray.
   */
  @Test
  public void testConvertMap2() {
    final Object result = CONVERTER.toXLValue(XLArray.class, M2);
    assertTrue(result instanceof XLArray);
    final XLValue[][] xlArray = ((XLArray) result).getArray();
    final Iterator<Map.Entry> iter = M2.entrySet().iterator();
    for (int i = 0; i < N; i++) {
      final Map.Entry entry = iter.next();
      assertTrue(xlArray[i][0] instanceof XLNumber);
      assertTrue(xlArray[i][1] instanceof XLNumber);
      assertEquals(((XLNumber) xlArray[i][0]).getAsDouble(), XlDateUtils.getDaysFromXlEpoch((LocalDate) entry.getKey()), 1e-15);
      assertEquals(((XLNumber) xlArray[i][1]).getAsDouble(), (double) entry.getValue(), 1e-15);
    }
  }

  /**
   * Tests conversion to Map (i.e. a raw type).
   */
  @Test
  public void testConvertArray2() {
    assertEquals(((XLNumber) PROCESSOR.invoke("MapTest2", XLArray.of(ROW2))).getAsInt(), -2);
    assertEquals(((XLNumber) PROCESSOR.invoke("MapTest2", XLArray.of(COLUMN2))).getAsInt(), -2);
    assertEquals(((XLNumber) PROCESSOR.invoke("MapTest2_varargs", XLArray.of(ROW2))).getAsInt(), -22);
    assertEquals(((XLNumber) PROCESSOR.invoke("MapTest2_varargs", XLArray.of(COLUMN2))).getAsInt(), -22);
  }

  /**
   * Tests the conversion of Map<LocalDate, Double[]> to an XLArray.
   */
  @Test
  public void testConvertMap3() {
    final Object result = CONVERTER.toXLValue(XLArray.class, M3);
    assertTrue(result instanceof XLArray);
    final XLValue[][] xlArray = ((XLArray) result).getArray();
    final Iterator<Map.Entry<LocalDate, Double[]>> iter = M3.entrySet().iterator();
    for (int i = 0; i < N; i++) {
      final Map.Entry<LocalDate, Double[]> entry = iter.next();
      assertTrue(xlArray[i][0] instanceof XLNumber);
      assertTrue(xlArray[i][1] instanceof XLArray);
      assertEquals(((XLArray) xlArray[i][1]).getArray().length, 1);
      assertEquals(((XLArray) xlArray[i][1]).getArray()[0].length, 2);
      assertEquals(((XLNumber) xlArray[i][0]).getAsDouble(), XlDateUtils.getDaysFromXlEpoch(entry.getKey()), 1e-15);
      assertEquals(((XLNumber) ((XLArray) xlArray[i][1]).getArray()[0][0]).getAsDouble(), entry.getValue()[0], 1e-15);
      assertEquals(((XLNumber) ((XLArray) xlArray[i][1]).getArray()[0][1]).getAsDouble(), entry.getValue()[1], 1e-15);
    }
  }

  /**
   * Tests conversion to Map<LocalDate, Double[]>.
   */
  @Test
  public void testConvertArray3() {
    assertEquals(((XLNumber) PROCESSOR.invoke("MapTest3", XLArray.of(ROW3))).getAsInt(), -3);
    assertEquals(((XLNumber) PROCESSOR.invoke("MapTest3", XLArray.of(COLUMN3))).getAsInt(), -3);
    assertEquals(((XLNumber) PROCESSOR.invoke("MapTest3_varargs", XLArray.of(ROW3))).getAsInt(), -33);
    assertEquals(((XLNumber) PROCESSOR.invoke("MapTest3_varargs", XLArray.of(COLUMN3))).getAsInt(), -33);
  }

  /**
   * Tests the conversion of Map<LocalDate, List<? extends Double>> to an XLArray.
   */
  @Test
  public void testConvertMap4() {
    final Object result = CONVERTER.toXLValue(XLArray.class, M4);
    assertTrue(result instanceof XLArray);
    final XLValue[][] xlArray = ((XLArray) result).getArray();
    final Iterator<Map.Entry<LocalDate, List<? extends Double>>> iter = M4.entrySet().iterator();
    for (int i = 0; i < N; i++) {
      final Map.Entry<LocalDate, List<? extends Double>> entry = iter.next();
      assertTrue(xlArray[i][0] instanceof XLNumber);
      assertTrue(xlArray[i][1] instanceof XLArray);
      assertEquals(((XLArray) xlArray[i][1]).getArray().length, 1);
      assertEquals(((XLArray) xlArray[i][1]).getArray()[0].length, 2);
      assertEquals(((XLNumber) xlArray[i][0]).getAsDouble(), XlDateUtils.getDaysFromXlEpoch(entry.getKey()), 1e-15);
      assertEquals(((XLNumber) ((XLArray) xlArray[i][1]).getArray()[0][0]).getAsDouble(), entry.getValue().get(0), 1e-15);
      assertEquals(((XLNumber) ((XLArray) xlArray[i][1]).getArray()[0][1]).getAsDouble(), entry.getValue().get(1), 1e-15);
    }
  }

  /**
   * Tests conversion to Map<LocalDate, List<? extends Double>>.
   */
  @Test
  public void testConvertArray4() {
    assertEquals(((XLNumber) PROCESSOR.invoke("MapTest4", XLArray.of(ROW4))).getAsInt(), -4);
    assertEquals(((XLNumber) PROCESSOR.invoke("MapTest4", XLArray.of(COLUMN4))).getAsInt(), -4);
    assertEquals(((XLNumber) PROCESSOR.invoke("MapTest4_varargs", XLArray.of(ROW4))).getAsInt(), -44);
    assertEquals(((XLNumber) PROCESSOR.invoke("MapTest4_varargs", XLArray.of(COLUMN4))).getAsInt(), -44);
  }


  /**
   * Test function.
   * @param m  the map
   * @return -1
   */
  @XLFunction(name = "MapTest1")
  public static int testMethod1(@XLParameter final Map<? extends LocalDate, ? super Double> m) {
    assertEquals(m.size(), M1.size());
    final Iterator<Map.Entry<LocalDate, Double>> iter = M1.entrySet().iterator();
    for (final Map.Entry<? extends LocalDate, ? super Double> entry1 : m.entrySet()) {
      final Entry<LocalDate, Double> entry2 = iter.next();
      assertEquals(entry1.getKey(), entry2.getKey());
      assertEquals(entry1.getValue(), entry2.getValue());
    }
    return -1;
  }

  /**
   * Test function.
   * @param maps  array of maps
   * @return -11
   */
  @XLFunction(name = "MapTest1_varargs")
  public static int testMethod1Varargs(@XLParameter final Map<? extends LocalDate, ? super Double>... maps) {
    assertEquals(maps.length, 1);
    assertEquals(maps[0].size(), M1.size());
    final Iterator<Map.Entry<LocalDate, Double>> iter = M1.entrySet().iterator();
    for (final Map.Entry<? extends LocalDate, ? super Double> entry1 : maps[0].entrySet()) {
      final Entry<LocalDate, Double> entry2 = iter.next();
      assertEquals(entry1.getKey(), entry2.getKey());
      assertEquals((double) entry1.getValue(), entry2.getValue(), 1e-15);
    }
    return -11;
  }

  /**
   * Test function.
   * @param m  the map
   * @return -2
   */
  @SuppressWarnings("rawtypes")
  @XLFunction(name = "MapTest2")
  public static int testMethod2(@XLParameter final Map m) {
    assertEquals(m.size(), M2.size());
    final Iterator<Entry<LocalDate, Number>> iter = M2.entrySet().iterator();
    for (final Object entry1 : m.entrySet()) {
      final Entry<LocalDate, Number> entry2 = iter.next();
      // as there is no type information, the date from Excel will be treated as a number
      assertEquals((double) ((Map.Entry) entry1).getKey(), XlDateUtils.getDaysFromXlEpoch(entry2.getKey()), 1e-15);
      assertEquals((double) ((Map.Entry) entry1).getValue(), (double) entry2.getValue(), 1e-15);
    }
    return -2;
  }

  /**
   * Test function.
   * @param maps  the maps
   * @return -22
   */
  @SuppressWarnings("rawtypes")
  @XLFunction(name = "MapTest2_varargs")
  public static int testMethod2Varargs(@XLParameter final Map... maps) {
    assertEquals(maps.length, 1);
    assertEquals(maps[0].size(), M2.size());
    final Iterator<Entry<LocalDate, Number>> iter = M2.entrySet().iterator();
    for (final Object entry1 : maps[0].entrySet()) {
      final Entry<LocalDate, Number> entry2 = iter.next();
      // as there is no type information, the date from Excel will be treated as a number
      assertEquals((double) ((Map.Entry) entry1).getKey(), XlDateUtils.getDaysFromXlEpoch(entry2.getKey()), 1e-15);
      assertEquals((double) ((Map.Entry) entry1).getValue(), (double) entry2.getValue(), 1e-15);
    }
    return -22;
  }

  /**
   * Test function.
   * @param m  the map
   * @return -3
   */
  @XLFunction(name = "MapTest3")
  public static int testMethod3(@XLParameter final Map<LocalDate, Double[]> m) {
    assertEquals(m.size(), M3.size());
    final Iterator<Entry<LocalDate, Double[]>> iter = M3.entrySet().iterator();
    for (final Map.Entry<LocalDate, Double[]> entry1 : m.entrySet()) {
      final Entry<LocalDate, Double[]> entry2 = iter.next();
      assertEquals(entry1.getKey(), entry2.getKey());
      for (int i = 0; i < entry2.getValue().length; i++) {
        assertEquals(entry1.getValue()[i], entry2.getValue()[i], 1e-15);
      }
    }
    return -3;
  }

  /**
   * Test function.
   * @param maps  the maps
   * @return -33
   */
  @XLFunction(name = "MapTest3_varargs")
  public static int testMethod3Varargs(@XLParameter final Map<LocalDate, Double[]>... maps) {
    assertEquals(maps.length, 1);
    assertEquals(maps[0].size(), M3.size());
    final Iterator<Entry<LocalDate, Double[]>> iter = M3.entrySet().iterator();
    for (final Map.Entry<LocalDate, Double[]> entry1 : maps[0].entrySet()) {
      final Entry<LocalDate, Double[]> entry2 = iter.next();
      assertEquals(entry1.getKey(), entry2.getKey());
      for (int i = 0; i < entry2.getValue().length; i++) {
        assertEquals(entry1.getValue()[i], entry2.getValue()[i], 1e-15);
      }
    }
    return -33;
  }

  /**
   * Test function.
   * @param m  the map
   * @return -4
   */
  @XLFunction(name = "MapTest4")
  public static int testMethod4(@XLParameter final Map<LocalDate, List<? extends Double>> m) {
    assertEquals(m.size(), M4.size());
    final Iterator<Entry<LocalDate, List<? extends Double>>> iter = M4.entrySet().iterator();
    for (final Map.Entry<LocalDate, List<? extends Double>> entry1 : m.entrySet()) {
      final Entry<LocalDate, List<? extends Double>> entry2 = iter.next();
      assertEquals(entry1.getKey(), entry2.getKey());
      for (int i = 0; i < entry2.getValue().size(); i++) {
        assertEquals(entry1.getValue().get(i), entry2.getValue().get(i), 1e-15);
      }
    }
    return -4;
  }

  /**
   * Test function.
   * @param maps  the maps
   * @return -44
   */
  @XLFunction(name = "MapTest4_varargs")
  public static int testMethod4Varargs(@XLParameter final Map<LocalDate, List<? extends Double>>... maps) {
    assertEquals(maps.length, 1);
    assertEquals(maps[0].size(), M4.size());
    final Iterator<Entry<LocalDate, List<? extends Double>>> iter = M4.entrySet().iterator();
    for (final Map.Entry<LocalDate, List<? extends Double>> entry1 : maps[0].entrySet()) {
      final Entry<LocalDate, List<? extends Double>> entry2 = iter.next();
      assertEquals(entry1.getKey(), entry2.getKey());
      for (int i = 0; i < entry2.getValue().size(); i++) {
        assertEquals(entry1.getValue().get(i), entry2.getValue().get(i), 1e-15);
      }
    }
    return -33;
  }

  /**
   * Test function.
   * @param map  the map
   * @return  0
   */
  @XLFunction(name = "NoKeyConverter")
  public static int testNoKeyConverter(@XLParameter final Map<LocalDate, Dimension> map) {
    return 0;
  }

  /**
   * Test function.
   * @param map  the map
   * @return  0
   */
  @XLFunction(name = "NoValueConverter")
  public static int testNoValueConverter(@XLParameter final Map<Dimension, LocalDate> map) {
    return 0;
  }

  private static class TestXlValue implements XLValue {

    public static TestXlValue of(final double height, final double width) {
      return new TestXlValue(height, width);
    }

    private final double _height;
    private final double _width;

    private TestXlValue(final double height, final double width) {
      _height = height;
      _width = width;
    }

    @Override
    public <E> E accept(final XLValueVisitor<E> visitor) {
      throw new UnsupportedOperationException();
    }

  }

}
