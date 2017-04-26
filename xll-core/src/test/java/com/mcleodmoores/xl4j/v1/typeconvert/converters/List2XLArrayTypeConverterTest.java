/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.awt.Dimension;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.v1.api.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.api.values.XLValueVisitor;
import com.mcleodmoores.xl4j.v1.simulator.MockFunctionProcessor;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;
import com.mcleodmoores.xl4j.v1.util.XlDateUtils;

/**
 * Unit tests for {@link List2XLArrayTypeConverter}.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class List2XLArrayTypeConverterTest {
  private static final int N = 50;
  private static final XLValue[][] ROW1 = new XLValue[1][N];
  private static final XLValue[][] ROW2 = new XLValue[1][N];
  private static final XLValue[][] ROW3 = new XLValue[1][N];
  private static final XLValue[][] ROW4 = new XLValue[1][N];
  private static final XLValue[][] COLUMN1 = new XLValue[N][1];
  private static final XLValue[][] COLUMN2 = new XLValue[N][1];
  private static final XLValue[][] COLUMN3 = new XLValue[N][1];
  private static final XLValue[][] COLUMN4 = new XLValue[N][1];
  private static final List<? super LocalDate> L1 = new LinkedList<>();
  private static final List L2 = new LinkedList();
  private static final List<Double[]> L3 = new LinkedList<>();
  private static final List<List<? extends Double>> L4 = new LinkedList<>();
  private static final Excel EXCEL = ExcelFactory.getInstance();
  private static final TypeConverter CONVERTER = new List2XLArrayTypeConverter(EXCEL);
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
      L1.add(date);
      L2.add(date);
      L3.add(new Double[] {objectNumber.doubleValue(), primitiveDouble * 15});
      L4.add(Arrays.asList(objectNumber.doubleValue(), primitiveDouble * 15));
    }
  }

  /**
   * Tests the type mapping.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLArray.class, List.class));
  }

  /**
   * Tests the type mapping.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(List.class, XLArray.class));
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
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullXlValue() {
    CONVERTER.toJavaObject(List.class, null);
  }

  /**
   * Tests that the object to convert cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullObject() {
    CONVERTER.toXLValue(null);
  }

  /**
   * Tests that the expected type must be a List.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testExpectedTypeNotAList() {
    CONVERTER.toJavaObject(Object.class, XLArray.of(ROW1));
  }

  /**
   * Tests that the expected type must be a class.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testExpectedTypeNotAClass() {
    CONVERTER.toJavaObject(new GenericArrayType() {
      @Override
      public Type getGenericComponentType() {
        return null;
      }
    }, XLArray.of(ROW3));
  }

  /**
   * Tests that the parameterized type must be a class.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
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
   * Tests that the parameterized type must be a List.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testParameterizedTypeNotAList() {
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
        return Set.class;
      }

    }, XLArray.of(COLUMN1));
  }

  /**
   * Tests that the type to convert must be a List.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNotAList() {
    CONVERTER.toXLValue(new Object());
  }

  /**
   * Tests the behaviour when a converter could not be found.
   */
  @Test
  public void testNoConverter() {
    assertEquals(PROCESSOR.invoke("NoConverterList", XLArray.of(new XLValue[][] {new XLValue[] {TestXlValue.of(120, 130)}})), XLError.Null);
  }

  /**
   * Tests an empty List.
   */
  @Test
  public void testEmptyList() {
    final Object result = CONVERTER.toXLValue(new LinkedList<>());
    assertTrue(result instanceof XLArray);
    final XLArray xlArray = (XLArray) result;
    assertEquals(xlArray.getArray().length, 1);
    assertEquals(xlArray.getArray()[0].length, 1);
    assertNull(xlArray.getArray()[0][0]);
  }

  /**
   * Tests the conversion of List<? super LocalDate> to an XLArray.
   */
  @Test
  public void testConvertList1() {
    final Object result = CONVERTER.toXLValue(L1);
    assertTrue(result instanceof XLArray);
    final XLValue[][] xlArray = ((XLArray) result).getArray();
    final Iterator<? super LocalDate> iter = L1.iterator();
    for (int i = 0; i < N; i++) {
      final LocalDate entry = (LocalDate) iter.next();
      assertTrue(xlArray[i][0] instanceof XLNumber);
      assertEquals(((XLNumber) xlArray[i][0]).getAsDouble(), XlDateUtils.getDaysFromXlEpoch(entry), 1e-15);
    }
  }

  /**
   * Tests conversion to List<? super LocalDate>.
   */
  @Test
  public void testConvertArray1() {
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest1", XLArray.of(ROW1))).getAsInt(), -1);
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest1", XLArray.of(COLUMN1))).getAsInt(), -1);
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest1_varargs", XLArray.of(ROW1))).getAsInt(), -11);
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest1_varargs", XLArray.of(COLUMN1))).getAsInt(), -11);
  }

  /**
   * Tests the conversion of List (i.e. a raw type) to an XLArray.
   */
  @Test
  public void testConvertList2() {
    final Object result = CONVERTER.toXLValue(L2);
    assertTrue(result instanceof XLArray);
    final XLValue[][] xlArray = ((XLArray) result).getArray();
    final Iterator<Object> iter = L2.iterator();
    for (int i = 0; i < N; i++) {
      final Object entry = iter.next();
      assertTrue(xlArray[i][0] instanceof XLNumber);
      assertEquals(((XLNumber) xlArray[i][0]).getAsDouble(), XlDateUtils.getDaysFromXlEpoch((LocalDate) entry), 1e-15);
    }
  }

  /**
   * Tests conversion to List (i.e. a raw type).
   */
  @Test
  public void testConvertArray2() {
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest2", XLArray.of(ROW2))).getAsInt(), -2);
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest2", XLArray.of(COLUMN2))).getAsInt(), -2);
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest2_varargs", XLArray.of(ROW2))).getAsInt(), -22);
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest2_varargs", XLArray.of(COLUMN2))).getAsInt(), -22);
  }

  /**
   * Tests the conversion of List<Double[]> to an XLArray.
   */
  @Test
  public void testConvertList3() {
    final Object result = CONVERTER.toXLValue(L3);
    assertTrue(result instanceof XLArray);
    final XLValue[][] xlArray = ((XLArray) result).getArray();
    final Iterator<Double[]> iter = L3.iterator();
    for (int i = 0; i < N; i++) {
      final Double[] entry = iter.next();
      assertTrue(xlArray[i][0] instanceof XLArray);
      assertEquals(((XLArray) xlArray[i][0]).getArray()[0].length, 2);
      assertEquals(((XLNumber) ((XLArray) xlArray[i][0]).getArray()[0][0]).getAsDouble(), entry[0], 1e-15);
      assertEquals(((XLNumber) ((XLArray) xlArray[i][0]).getArray()[0][1]).getAsDouble(), entry[1], 1e-15);
    }
  }

  /**
   * Tests conversion to List<Double[]>.
   */
  @Test
  public void testConvertArray3() {
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest3", XLArray.of(ROW3))).getAsInt(), -3);
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest3", XLArray.of(COLUMN3))).getAsInt(), -3);
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest3_varargs", XLArray.of(ROW3))).getAsInt(), -33);
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest3_varargs", XLArray.of(COLUMN3))).getAsInt(), -33);
  }

  /**
   * Tests the conversion of List<List<? extends Double>> to an XLArray.
   */
  @Test
  public void testConvertList4() {
    final Object result = CONVERTER.toXLValue(L4);
    assertTrue(result instanceof XLArray);
    final XLValue[][] xlArray = ((XLArray) result).getArray();
    final Iterator<List<? extends Double>> iter = L4.iterator();
    for (int i = 0; i < N; i++) {
      final List<? extends Double> entry = iter.next();
      assertTrue(xlArray[i][0] instanceof XLArray);
      assertEquals(((XLNumber) ((XLArray) xlArray[i][0]).getArray()[0][0]).getAsDouble(), entry.get(0), 1e-15);
      assertEquals(((XLNumber) ((XLArray) xlArray[i][0]).getArray()[1][0]).getAsDouble(), entry.get(1), 1e-15);
    }
  }

  /**
   * Tests conversion to List<List<? extends Double>>.
   */
  @Test
  public void testConvertArray4() {
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest4", XLArray.of(ROW4))).getAsInt(), -4);
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest4", XLArray.of(COLUMN4))).getAsInt(), -4);
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest4_varargs", XLArray.of(ROW4))).getAsInt(), -44);
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest4_varargs", XLArray.of(COLUMN4))).getAsInt(), -44);
  }

  /**
   * Tests conversion to List<? extends LocalDate>.
   */
  @Test
  public void testConvertArray5() {
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest5", XLArray.of(ROW1))).getAsInt(), -5);
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest5", XLArray.of(COLUMN1))).getAsInt(), -5);
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest5_varargs", XLArray.of(ROW1))).getAsInt(), -55);
    assertEquals(((XLNumber) PROCESSOR.invoke("ListTest5_varargs", XLArray.of(COLUMN1))).getAsInt(), -55);
  }

  /**
   * Test function.
   * @param l  the List
   * @return -1
   */
  @XLFunction(name = "ListTest1")
  public static int testMethod1(@XLParameter final List<? super LocalDate> l) {
    assertEquals(l.size(), L1.size());
    final Iterator<?> iter = L1.iterator();
    for (final Object entry1 : l) {
      final Object entry2 = iter.next();
      assertEquals(entry1, entry2);
    }
    return -1;
  }

  /**
   * Test function.
   * @param lists  array of Lists
   * @return -11
   */
  @XLFunction(name = "ListTest1_varargs")
  public static int testMethod1Varargs(@XLParameter final List<? super LocalDate>... lists) {
    assertEquals(lists.length, 1);
    assertEquals(lists[0].size(), L1.size());
    final Iterator<?> iter = L1.iterator();
    for (final Object entry1 : lists[0]) {
      final Object entry2 = iter.next();
      assertEquals(entry1, entry2);
    }
    return -11;
  }

  /**
   * Test function.
   * @param l  the List
   * @return -2
   */
  @XLFunction(name = "ListTest2")
  public static int testMethod2(@XLParameter final List l) {
    assertEquals(l.size(), L2.size());
    final Iterator<LocalDate> iter = L2.iterator();
    for (final Object entry1 : l) {
      final LocalDate entry2 = iter.next();
      // as there is no type information, the date from Excel will be treated as a number
      assertEquals((double) entry1, XlDateUtils.getDaysFromXlEpoch(entry2), 1e-15);
    }
    return -2;
  }

  /**
   * Test function.
   * @param lists  the Lists
   * @return -22
   */
  @XLFunction(name = "ListTest2_varargs")
  public static int testMethod2Varargs(@XLParameter final List... lists) {
    assertEquals(lists.length, 1);
    assertEquals(lists[0].size(), L2.size());
    final Iterator<LocalDate> iter = L2.iterator();
    for (final Object entry1 : lists[0]) {
      final LocalDate entry2 = iter.next();
      // as there is no type information, the date from Excel will be treated as a number
      assertEquals((double) entry1, XlDateUtils.getDaysFromXlEpoch(entry2), 1e-15);
    }
    return -22;
  }

  /**
   * Test function.
   * @param l  the List
   * @return -3
   */
  @XLFunction(name = "ListTest3")
  public static int testMethod3(@XLParameter final List<Double[]> l) {
    assertEquals(l.size(), L3.size());
    final Iterator<Double[]> iter = L3.iterator();
    for (final Double[] entry1 : l) {
      final Double[] entry2 = iter.next();
      for (int i = 0; i < entry2.length; i++) {
        assertEquals(entry1[i], entry2[i], 1e-15);
      }
    }
    return -3;
  }

  /**
   * Test function.
   * @param lists  the Lists
   * @return -33
   */
  @XLFunction(name = "ListTest3_varargs")
  public static int testMethod3Varargs(@XLParameter final List<Double[]>... lists) {
    assertEquals(lists.length, 1);
    assertEquals(lists[0].size(), L3.size());
    final Iterator<Double[]> iter = L3.iterator();
    for (final Double[] entry1 : lists[0]) {
      final Double[] entry2 = iter.next();
      for (int i = 0; i < entry2.length; i++) {
        assertEquals(entry1[i], entry2[i], 1e-15);
      }
    }
    return -33;
  }

  /**
   * Test function.
   * @param l  the List
   * @return -4
   */
  @XLFunction(name = "ListTest4")
  public static int testMethod4(@XLParameter final List<List<? extends Double>> l) {
    assertEquals(l.size(), L4.size());
    final Iterator<List<? extends Double>> iter = L4.iterator();
    for (final List<? extends Double> entry1 : l) {
      final List<? extends Double> entry2 = iter.next();
      for (int i = 0; i < entry2.size(); i++) {
        assertEquals(entry1.get(i), entry2.get(i), 1e-15);
      }
    }
    return -4;
  }

  /**
   * Test function.
   * @param lists  the Lists
   * @return -44
   */
  @XLFunction(name = "ListTest4_varargs")
  public static int testMethod4Varargs(@XLParameter final List<List<? extends Double>>... lists) {
    assertEquals(lists.length, 1);
    assertEquals(lists[0].size(), L4.size());
    final Iterator<List<? extends Double>> iter = L4.iterator();
    for (final List<? extends Double> entry1 : lists[0]) {
      final List<? extends Double> entry2 = iter.next();
      for (int i = 0; i < entry2.size(); i++) {
        assertEquals(entry1.get(i), entry2.get(i), 1e-15);
      }
    }
    return -44;
  }

  /**
   * Test function.
   * @param list  the List
   * @return  0
   */
  @XLFunction(name = "NoConverterList")
  public static int testNoKeyConverter(@XLParameter final List<Dimension> list) {
    return 0;
  }

  /**
   * Test function.
   * @param l  the List
   * @return -5
   */
  @XLFunction(name = "ListTest5")
  public static int testMethod5(@XLParameter final List<? extends LocalDate> l) {
    assertEquals(l.size(), L1.size());
    final Iterator<?> iter = L1.iterator();
    for (final LocalDate entry1 : l) {
      final Object entry2 = iter.next();
      assertEquals(entry1, entry2);
    }
    return -5;
  }

  /**
   * Test function.
   * @param lists  array of Lists
   * @return -55
   */
  @XLFunction(name = "ListTest5_varargs")
  public static int testMethod5Varargs(@XLParameter final List<? extends LocalDate>... lists) {
    assertEquals(lists.length, 1);
    assertEquals(lists[0].size(), L1.size());
    final Iterator<?> iter = L1.iterator();
    for (final LocalDate entry1 : lists[0]) {
      final Object entry2 = iter.next();
      assertEquals(entry1, entry2);
    }
    return -55;
  }

  /**
   *
   */
  @Test
  public void testConvertArray11() {
    final XLArray xlArray = XLArray.of(new XLValue[][] {
      new XLValue[] {XLNumber.of(1), XLNumber.of(2), XLNumber.of(3), XLNumber.of(4)}
    });
    final Object result = PROCESSOR.invoke("ObjectArrayTest1", xlArray);
  }

  /**
   * A test function.
   * @param <T>
   *          the type of the objects in the input list
   * @param <U>
   *          the type of the objects in the result list
   * @param list
   *          a list
   * @return
   *          the result of the function
   */
  @XLFunction(name = "ObjectArrayTest1")
  public static <T extends Number, U extends Number> List<U> objectArrayTest1(@XLParameter final List<? extends T> list) {
    final List<U> result = new ArrayList<>();
    for (final T t : list) {
      result.add((U) BigDecimal.valueOf(t.doubleValue() * 2));
    }
    return result;
  }

  /**
   * Test XLValue.
   */
  private static final class TestXlValue implements XLValue {

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
