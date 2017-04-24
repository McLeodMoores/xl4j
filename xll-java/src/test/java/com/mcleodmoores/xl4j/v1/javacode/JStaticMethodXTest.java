/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLBoolean;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.javacode.testutils.TestObject;

/**
 * Unit tests for {@link JMethod#jStaticMethodX(XLString, XLString, XLValue...)}.
 */
public class JStaticMethodXTest {
  private static final XLString CLASS = XLString.of("com.mcleodmoores.xl4j.v1.javacode.testutils.TestObject");

  /**
   * Tests that this method returns references.
   */
  @Test
  public void testReturnType() {
    final Object methodResult1 = JMethod.jStaticMethodX(XLString.of("java.lang.String"), XLString.of("valueOf"), XLBoolean.FALSE);
    final Object methodResult2 = JMethod.jStaticMethodX(XLString.of("java.lang.Double"), XLString.of("valueOf"), XLString.of("3"));
    // expect references
    assertTrue(methodResult1 instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult1).getHandle()), "false");
    assertTrue(methodResult2 instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult2).getHandle()), 3.);
  }

  /**
   * Tests the no-args factory method.
   */
  @Test
  public void testNoArgsFactoryMethod() {
    final Object constructed = JMethod.jStaticMethodX(CLASS, XLString.of("of"), new XLValue[0]);
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.of();
    assertEquals(constructedObject, expectedObject);
  }

  /**
   * Tests the int factory method.
   */
  @Test
  public void testIntFactoryMethod() {
    final Object constructed = JMethod.jStaticMethodX(CLASS, XLString.of("ofPrimitiveInt"), XLNumber.of(1));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.ofPrimitiveInt(1);
    assertEquals(constructedObject, expectedObject);
  }

  /**
   * Tests the String factory method.
   */
  @Test
  public void testStringFactoryMethod() {
    final Object constructed = JMethod.jStaticMethodX(CLASS, XLString.of("ofString"), XLString.of("2"));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.ofString("2");
    assertEquals(constructedObject, expectedObject);
  }

  /**
   * Tests the object factory method.
   */
  @Test
  public void testObjectFactoryMethod() {
    final XLValue number = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(3));
    final Object constructed = JMethod.jStaticMethodX(CLASS, XLString.of("ofObject"), number);
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.ofObject(Short.valueOf("3"));
    assertEquals(constructedObject, expectedObject);
  }

  /**
   * Tests the int, String factory method.
   */
  @Test
  public void testIntStringFactoryMethod() {
    final Object constructed = JMethod.jStaticMethodX(CLASS, XLString.of("ofPrimitiveIntString"), XLNumber.of(4), XLString.of("40"));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.ofPrimitiveIntString(4, "40");
    // could go through (int, Object)
    assertEquals(constructedObject, expectedObject);
  }

  /**
   * Tests the int, Double factory method.
   */
  @Test
  public void testIntDoubleFactoryMethod() {
    final Object constructed = JMethod.jStaticMethodX(CLASS, XLString.of("ofPrimitiveIntDouble"), XLNumber.of(5), XLNumber.of(50));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.ofPrimitiveIntDouble(5, 50.);
    assertEquals(constructedObject, expectedObject);
  }

  /**
   * Tests the Double factory method.
   */
  @Test
  public void testDoubleFactoryMethod() {
    final Object constructed = JMethod.jStaticMethodX(CLASS, XLString.of("ofDouble"), XLNumber.of(6.));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.ofDouble(6.);
    assertEquals(constructedObject, expectedObject);
  }

  /**
   * Tests the int, Object factory method.
   */
  @Test
  public void testIntObjectFactoryMethod() {
    final XLValue number = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(70));
    final Object constructed = JMethod.jStaticMethodX(CLASS, XLString.of("ofPrimitiveIntObject"), XLNumber.of(7), number);
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.ofPrimitiveIntObject(7, Short.valueOf("70"));
    assertEquals(constructedObject, expectedObject);
  }

  /**
   * Tests the String... factory method.
   */
  @Test
  public void testStringsFactoryMethod() {
    final Object constructed = JMethod.jStaticMethodX(CLASS, XLString.of("ofStrings"), XLString.of("80"), XLString.of("81"), XLString.of("82"));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.ofStrings("80", "81", "82");
    assertEquals(constructedObject, expectedObject);
  }

  /**
   * Tests the int, String... factory method.
   */
  @Test
  public void testIntStringsFactoryMethod() {
    final Object constructed = JMethod.jStaticMethodX(CLASS, XLString.of("ofPrimitiveIntStrings"),
        XLNumber.of(9), XLString.of("90"), XLString.of("91"), XLString.of("92"));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.ofPrimitiveIntStrings(9, new String[] {"90", "91", "92"});
    assertEquals(constructedObject, expectedObject);
  }

  /**
   * Tests the Doubles... factory method.
   */
  @Test
  public void testDoublesFactoryMethod() {
    final Object constructed = JMethod.jStaticMethodX(CLASS, XLString.of("ofDoubles"), XLNumber.of(100.), XLNumber.of(101.), XLNumber.of(102.));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.ofDoubles(100., 101., 102.);
    assertEquals(constructedObject, expectedObject);
  }

  /**
   * Tests the int, Objects... factory method.
   */
  @Test
  public void testIntObjectsFactoryMethod() {
    final XLValue number1 = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(110));
    final XLValue number2 = JConstruct.jconstruct(XLString.of("java.lang.Long"), XLNumber.of(111));
    final XLValue number3 = JConstruct.jconstruct(XLString.of("java.lang.Float"), XLNumber.of(112));
    final Object constructed = JMethod.jStaticMethodX(CLASS, XLString.of("ofPrimitiveIntObjects"), XLNumber.of(11), number1, number2, number3);
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.ofPrimitiveIntObjects(11, new Object[] {(short) 110, 111L, 112F});
    assertEquals(constructedObject, expectedObject);
  }

  /**
   * Tests the int, Double... factory method.
   */
  @Test
  public void testIntDoublesFactoryMethod() {
    final Object constructed = JMethod.jStaticMethodX(CLASS, XLString.of("ofPrimitiveIntDoubles"),
        XLNumber.of(12), XLNumber.of(120.), XLNumber.of(121.), XLNumber.of(122.));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.ofPrimitiveIntDoubles(12, new Double[] {120., 121., 122.});
    assertEquals(constructedObject, expectedObject);
  }

  /**
   * Tests the Object... factory method.
   */
  @Test
  public void testObjectsFactoryMethod() {
    final XLValue number1 = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(130));
    final XLValue number2 = JConstruct.jconstruct(XLString.of("java.lang.Long"), XLNumber.of(131));
    final XLValue number3 = JConstruct.jconstruct(XLString.of("java.lang.Float"), XLNumber.of(132));
    final Object constructed = JMethod.jStaticMethodX(CLASS, XLString.of("ofObjects"), number1, number2, number3);
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.ofObjects(new Object[] {(short) 130, 131L, 132F});
    assertEquals(constructedObject, expectedObject);
  }

  /**
   * Tests the String[], String factory method.
   */
  @Test
  public void testStringArrayStringsFactoryMethod() {
    final Object constructed = JMethod.jStaticMethodX(CLASS, XLString.of("ofStringsStrings"),
        XLArray.of(new XLValue[][] {new XLValue[] {XLString.of("14")}}), XLString.of("140"), XLString.of("141"));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.ofStringsStrings(new String[] {"14"}, new String[] {"140", "141"});
    assertEquals(constructedObject, expectedObject);
  }
}
