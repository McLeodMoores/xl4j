/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.awt.Dimension;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * Unit tests for {@link Set2XLArrayTypeConverter}.
 */
public class Set2XLArrayTypeConverterTest {
  private static final int N = 50;
  private static final XLValue[][] ROW1 = new XLValue[1][N];
  private static final XLValue[][] ROW2 = new XLValue[1][N];
  private static final XLValue[][] ROW3 = new XLValue[1][N];
  private static final XLValue[][] ROW4 = new XLValue[1][N];
  private static final XLValue[][] COLUMN1 = new XLValue[N][1];
  private static final XLValue[][] COLUMN2 = new XLValue[N][1];
  private static final XLValue[][] COLUMN3 = new XLValue[N][1];
  private static final XLValue[][] COLUMN4 = new XLValue[N][1];
  private static final Set<? super LocalDate> S1 = new LinkedHashSet<>();
  private static final Set S2 = new LinkedHashSet();
  private static final Set<Double[]> S3 = new LinkedHashSet<>();
  private static final Set<List<? extends Double>> S4 = new LinkedHashSet<>();
  private static final Excel EXCEL = ExcelFactory.getInstance();
  private static final TypeConverter CONVERTER = new Set2XLArrayTypeConverter(EXCEL);
  private static final MockFunctionProcessor PROCESSOR = MockFunctionProcessor.getInstance();

  static {
    for (int i = 0; i < N; i++) {
      final LocalDate date = LocalDate.now().plusDays(i);
      final double primitiveDouble = Math.random();
      //TODO change when we allow nulls
      //      final Number objectNumber = i % 2 == 0 ? primitiveDouble + 0.5 : null;
      final Number objectNumber = i % 2 == 0 ? primitiveDouble + 0.5 : primitiveDouble * 3;
      ROW1[0][i] = XLNumber.of(XlDateUtils.getDaysFromXlEpoch(date));
      //      ROW2[1][i] = objectNumber != null ? XLNumber.of(objectNumber.doubleValue()) : null;
      ROW2[0][i] = ROW1[0][i];
      ROW3[0][i] = XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(objectNumber.doubleValue()), XLNumber.of(primitiveDouble * 15)}});
      ROW4[0][i] = XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(objectNumber.doubleValue()), XLNumber.of(primitiveDouble * 15)}});
      COLUMN1[i][0] = ROW1[0][i];
      COLUMN2[i][0] = ROW2[0][i];
      COLUMN3[i][0] = ROW3[0][i];
      COLUMN4[i][0] = ROW4[0][i];
      S1.add(date);
      S2.add(date);
      S3.add(new Double[] {objectNumber.doubleValue(), primitiveDouble * 15});
      S4.add(Arrays.asList(objectNumber.doubleValue(), primitiveDouble * 15));
    }
  }

  /**
   * Tests the type mapping.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLArray.class, Set.class));
  }

  /**
   * Tests the type mapping.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(Set.class, XLArray.class));
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
    CONVERTER.toJavaObject(Set.class, null);
  }

  /**
   * Tests that the object to convert cannot be null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullObject() {
    CONVERTER.toXLValue(null);
  }

  /**
   * Tests that the expected type must be a Set.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testExpectedTypeNotASet() {
    CONVERTER.toJavaObject(Object.class, XLArray.of(ROW1));
  }

  /**
   * Tests that the expected type must be a class.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testExpectedTypeNotAClass() {
    CONVERTER.toJavaObject(new GenericArrayType() {
      @Override
      public Type getGenericComponentType() {
        return null;
      }
    }, XLArray.of(COLUMN2));
  }

  /**
   * Tests that the parameterized type must be a class.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testParameterizedTypeNotAClass() {
    CONVERTER.toJavaObject(new ParameterizedType() {

      @Override
      public Type[] getActualTypeArguments() {
        return null;
      }

      @Override
      public Type getOwnerType() {
        return null;
      }

      @Override
      public Type getRawType() {
        return null;
      }

    }, XLArray.of(COLUMN1));
  }

  /**
   * Tests that the parameterized type must be a Set.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testParameterizedTypeNotASet() {
    CONVERTER.toJavaObject(new ParameterizedType() {

      @Override
      public Type[] getActualTypeArguments() {
        return null;
      }

      @Override
      public Type getOwnerType() {
        return null;
      }

      @Override
      public Type getRawType() {
        return Map.class;
      }

    }, XLArray.of(COLUMN1));
  }

  /**
   * Tests that the type to convert must be a Set.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNotASet() {
    CONVERTER.toXLValue(new Object());
  }

  /**
   * Tests the behaviour when a converter could not be found.
   */
  @Test
  public void testNoConverter() {
    assertEquals(PROCESSOR.invoke("NoConverter", XLArray.of(new XLValue[][] {new XLValue[] {TestXlValue.of(120, 130)}})), XLError.Null);
  }

  /**
   * Tests an empty set.
   */
  @Test
  public void testEmptySet() {
    final Object result = CONVERTER.toXLValue(new HashSet<>());
    assertTrue(result instanceof XLArray);
    final XLArray xlArray = (XLArray) result;
    assertEquals(xlArray.getArray().length, 1);
    assertEquals(xlArray.getArray()[0].length, 1);
    assertEquals(xlArray.getArray()[0][0], null);
  }

  /**
   * Tests the conversion of Set<? super LocalDate> to an XLArray.
   */
  @Test
  public void testConvertSet1() {
    final Object result = CONVERTER.toXLValue(S1);
    assertTrue(result instanceof XLArray);
    final XLValue[][] xlArray = ((XLArray) result).getArray();
    final Iterator<? super LocalDate> iter = S1.iterator();
    for (int i = 0; i < N; i++) {
      final LocalDate entry = (LocalDate) iter.next();
      assertTrue(xlArray[i][0] instanceof XLNumber);
      assertEquals(((XLNumber) xlArray[i][0]).getAsDouble(), XlDateUtils.getDaysFromXlEpoch(entry), 1e-15);
    }
  }

  /**
   * Tests conversion to Set<? super LocalDate>.
   */
  @Test
  public void testConvertArray1() {
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest1", XLArray.of(ROW1))).getAsInt(), -1);
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest1", XLArray.of(COLUMN1))).getAsInt(), -1);
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest1_varargs", XLArray.of(ROW1))).getAsInt(), -11);
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest1_varargs", XLArray.of(COLUMN1))).getAsInt(), -11);
  }

  /**
   * Tests the conversion of Set (i.e. a raw type) to an XLArray.
   */
  @Test
  public void testConvertSet2() {
    final Object result = CONVERTER.toXLValue(S2);
    assertTrue(result instanceof XLArray);
    final XLValue[][] xlArray = ((XLArray) result).getArray();
    final Iterator<Object> iter = S2.iterator();
    for (int i = 0; i < N; i++) {
      final Object entry = iter.next();
      assertTrue(xlArray[i][0] instanceof XLNumber);
      assertEquals(((XLNumber) xlArray[i][0]).getAsDouble(), XlDateUtils.getDaysFromXlEpoch((LocalDate) entry), 1e-15);
    }
  }

  /**
   * Tests conversion to Set (i.e. a raw type).
   */
  @Test
  public void testConvertArray2() {
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest2", XLArray.of(ROW2))).getAsInt(), -2);
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest2", XLArray.of(COLUMN2))).getAsInt(), -2);
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest2_varargs", XLArray.of(ROW2))).getAsInt(), -22);
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest2_varargs", XLArray.of(COLUMN2))).getAsInt(), -22);
  }

  /**
   * Tests the conversion of Set<Double[]> to an XLArray.
   */
  @Test
  public void testConvertSet3() {
    final Object result = CONVERTER.toXLValue(S3);
    assertTrue(result instanceof XLArray);
    final XLValue[][] xlArray = ((XLArray) result).getArray();
    final Iterator<Double[]> iter = S3.iterator();
    for (int i = 0; i < N; i++) {
      final Double[] entry = iter.next();
      assertTrue(xlArray[i][0] instanceof XLArray);
      assertEquals(((XLArray) xlArray[i][0]).getArray()[0].length, 2);
      assertEquals(((XLNumber) ((XLArray) xlArray[i][0]).getArray()[0][0]).getAsDouble(), entry[0], 1e-15);
      assertEquals(((XLNumber) ((XLArray) xlArray[i][0]).getArray()[0][1]).getAsDouble(), entry[1], 1e-15);
    }
  }

  /**
   * Tests conversion to Set<Double[]>.
   */
  @Test
  public void testConvertArray3() {
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest3", XLArray.of(ROW3))).getAsInt(), -3);
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest3", XLArray.of(COLUMN3))).getAsInt(), -3);
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest3_varargs", XLArray.of(ROW3))).getAsInt(), -33);
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest3_varargs", XLArray.of(COLUMN3))).getAsInt(), -33);
  }

  /**
   * Tests the conversion of Set<List<? extends Double>> to an XLArray.
   */
  @Test
  public void testConvertSet4() {
    final Object result = CONVERTER.toXLValue(S4);
    assertTrue(result instanceof XLArray);
    final XLValue[][] xlArray = ((XLArray) result).getArray();
    final Iterator<List<? extends Double>> iter = S4.iterator();
    for (int i = 0; i < N; i++) {
      final List<? extends Double> entry = iter.next();
      assertEquals(((XLNumber) ((XLArray) xlArray[i][0]).getArray()[0][0]).getAsDouble(), entry.get(0), 1e-15);
      assertEquals(((XLNumber) ((XLArray) xlArray[i][0]).getArray()[1][0]).getAsDouble(), entry.get(1), 1e-15);
    }
  }

  /**
   * Tests conversion to Set<List<? extends Double>>.
   */
  @Test
  public void testConvertArray4() {
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest4", XLArray.of(ROW4))).getAsInt(), -4);
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest4", XLArray.of(COLUMN4))).getAsInt(), -4);
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest4_varargs", XLArray.of(ROW4))).getAsInt(), -44);
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest4_varargs", XLArray.of(COLUMN4))).getAsInt(), -44);
  }

  /**
   * Tests conversion to Set<? extends LocalDate>.
   */
  @Test
  public void testConvertArray5() {
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest5", XLArray.of(ROW1))).getAsInt(), -5);
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest5", XLArray.of(COLUMN1))).getAsInt(), -5);
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest5_varargs", XLArray.of(ROW1))).getAsInt(), -55);
    assertEquals(((XLNumber) PROCESSOR.invoke("SetTest5_varargs", XLArray.of(COLUMN1))).getAsInt(), -55);
  }

  /**
   * Test function.
   * @param s  the set
   * @return -1
   */
  @XLFunction(name = "SetTest1")
  public static int testMethod1(@XLParameter final Set<? super LocalDate> s) {
    assertEquals(s.size(), S1.size());
    final Iterator<?> iter = S1.iterator();
    for (final Object entry1 : s) {
      final Object entry2 = iter.next();
      assertEquals(entry1, entry2);
    }
    return -1;
  }

  /**
   * Test function.
   * @param sets  array of sets
   * @return -11
   */
  @XLFunction(name = "SetTest1_varargs")
  public static int testMethod1Varargs(@XLParameter final Set<? super LocalDate>... sets) {
    assertEquals(sets.length, 1);
    assertEquals(sets[0].size(), S1.size());
    final Iterator<?> iter = S1.iterator();
    for (final Object entry1 : sets[0]) {
      final Object entry2 = iter.next();
      assertEquals(entry1, entry2);
    }
    return -11;
  }

  /**
   * Test function.
   * @param s  the set
   * @return -2
   */
  @SuppressWarnings("rawtypes")
  @XLFunction(name = "SetTest2")
  public static int testMethod2(@XLParameter final Set s) {
    assertEquals(s.size(), S2.size());
    final Iterator<LocalDate> iter = S2.iterator();
    for (final Object entry1 : s) {
      final LocalDate entry2 = iter.next();
      // as there is no type information, the date from Excel will be treated as a number
      assertEquals((double) entry1, XlDateUtils.getDaysFromXlEpoch(entry2), 1e-15);
    }
    return -2;
  }

  /**
   * Test function.
   * @param sets  the sets
   * @return -22
   */
  @SuppressWarnings("rawtypes")
  @XLFunction(name = "SetTest2_varargs")
  public static int testMethod2Varargs(@XLParameter final Set... sets) {
    assertEquals(sets.length, 1);
    assertEquals(sets[0].size(), S2.size());
    final Iterator<LocalDate> iter = S2.iterator();
    for (final Object entry1 : sets[0]) {
      final LocalDate entry2 = iter.next();
      // as there is no type information, the date from Excel will be treated as a number
      assertEquals((double) entry1, XlDateUtils.getDaysFromXlEpoch(entry2), 1e-15);
    }
    return -22;
  }

  /**
   * Test function.
   * @param s  the set
   * @return -3
   */
  @XLFunction(name = "SetTest3")
  public static int testMethod3(@XLParameter final Set<Double[]> s) {
    assertEquals(s.size(), S3.size());
    final Iterator<Double[]> iter = S3.iterator();
    for (final Double[] entry1 : s) {
      final Double[] entry2 = iter.next();
      for (int i = 0; i < entry2.length; i++) {
        assertEquals(entry1[i], entry2[i], 1e-15);
      }
    }
    return -3;
  }

  /**
   * Test function.
   * @param sets  the sets
   * @return -33
   */
  @XLFunction(name = "SetTest3_varargs")
  public static int testMethod3Varargs(@XLParameter final Set<Double[]>... sets) {
    assertEquals(sets.length, 1);
    assertEquals(sets[0].size(), S3.size());
    final Iterator<Double[]> iter = S3.iterator();
    for (final Double[] entry1 : sets[0]) {
      final Double[] entry2 = iter.next();
      for (int i = 0; i < entry2.length; i++) {
        assertEquals(entry1[i], entry2[i], 1e-15);
      }
    }
    return -33;
  }

  /**
   * Test function.
   * @param s  the set
   * @return -4
   */
  @XLFunction(name = "SetTest4")
  public static int testMethod4(@XLParameter final Set<List<? extends Double>> s) {
    assertEquals(s.size(), S4.size());
    final Iterator<List<? extends Double>> iter = S4.iterator();
    for (final List<? extends Double> entry1 : s) {
      final List<? extends Double> entry2 = iter.next();
      for (int i = 0; i < entry2.size(); i++) {
        assertEquals(entry1.get(i), entry2.get(i), 1e-15);
      }
    }
    return -4;
  }

  /**
   * Test function.
   * @param sets  the sets
   * @return -44
   */
  @XLFunction(name = "SetTest4_varargs")
  public static int testMethod4Varargs(@XLParameter final Set<List<? extends Double>>... sets) {
    assertEquals(sets.length, 1);
    assertEquals(sets[0].size(), S4.size());
    final Iterator<List<? extends Double>> iter = S4.iterator();
    for (final List<? extends Double> entry1 : sets[0]) {
      final List<? extends Double> entry2 = iter.next();
      for (int i = 0; i < entry2.size(); i++) {
        assertEquals(entry1.get(i), entry2.get(i), 1e-15);
      }
    }
    return -44;
  }

  /**
   * Test function.
   * @param set  the set
   * @return  0
   */
  @XLFunction(name = "NoConverter")
  public static int testNoKeyConverter(@XLParameter final Set<Dimension> set) {
    return 0;
  }

  /**
   * Test function.
   * @param s  the set
   * @return -5
   */
  @XLFunction(name = "SetTest5")
  public static int testMethod5(@XLParameter final Set<? extends LocalDate> s) {
    assertEquals(s.size(), S1.size());
    final Iterator<?> iter = S1.iterator();
    for (final LocalDate entry1 : s) {
      final Object entry2 = iter.next();
      assertEquals(entry1, entry2);
    }
    return -5;
  }

  /**
   * Test function.
   * @param sets  array of sets
   * @return -55
   */
  @XLFunction(name = "SetTest5_varargs")
  public static int testMethod5Varargs(@XLParameter final Set<? extends LocalDate>... sets) {
    assertEquals(sets.length, 1);
    assertEquals(sets[0].size(), S1.size());
    final Iterator<?> iter = S1.iterator();
    for (final LocalDate entry1 : sets[0]) {
      final Object entry2 = iter.next();
      assertEquals(entry1, entry2);
    }
    return -55;
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
