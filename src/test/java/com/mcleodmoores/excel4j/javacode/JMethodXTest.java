/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.testutil.TestObject;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 *
 */
public class JMethodXTest {
  private static final XLString CLASS = XLString.of("com.mcleodmoores.excel4j.testutil.TestObject");
  private TestObject _testObject;
  private XLObject _testObjectReference;

  @BeforeTest
  public void init() {
    final XLValue xlValue = JConstruct.jconstruct(CLASS, XLNumber.of(10.), XLNumber.of(20.), XLNumber.of(30.), XLNumber.of(40.), XLNumber.of(50.));
    assertTrue(xlValue instanceof XLObject);
    _testObjectReference = (XLObject) xlValue;
    _testObject = (TestObject) ExcelFactory.getInstance().getHeap().getObject(_testObjectReference.getHandle());
  }

  @Test
  public void testNonOverloadedGetters() {
    Object methodResult = JMethod.jMethodX(_testObjectReference, XLString.of("getNumber"), new XLValue[0]);
    assertTrue(methodResult instanceof XLObject);
    // XLNumber internally stores all numbers as doubles
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()), _testObject.getNumber());
    // TODO fails because arrays can't be returned
    methodResult = JMethod.jMethodX(_testObjectReference, XLString.of("getDoubles"), new XLValue[0]);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()), new Double[] {10., 20., 30., 40., 50.});
  }

  @Test
  public void testNoArgsGetMethod() {
    final Object methodResult = JMethod.jMethodX(_testObjectReference, XLString.of("getDouble"), new XLValue[0]);
    assertTrue(methodResult instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()), _testObject.getDouble());
  }

  @Test
  public void testIntGetMethod() {
    final Object methodResult = JMethod.jMethodX(_testObjectReference, XLString.of("getDouble"), XLNumber.of(1));
    assertTrue(methodResult instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()), _testObject.getDouble(1));
  }

  @Test
  public void testStringGetMethod() {
    final Object methodResult = JMethod.jMethodX(_testObjectReference, XLString.of("getDouble"), XLString.of("2"));
    assertTrue(methodResult instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()), _testObject.getDouble("2"));
  }

  @Test
  public void testObjectGetMethod() {
    final XLValue number = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(3));
    final Object methodResult = JMethod.jMethodX(_testObjectReference, XLString.of("getDouble"), number);
    assertTrue(methodResult instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()), _testObject.getDouble(Short.valueOf((short) 3)));
  }

  @Test
  public void testIntsSumMethod() {
    final Object methodResult = JMethod.jMethodX(_testObjectReference, XLString.of("getDoublesSum"), XLNumber.of(1), XLNumber.of(2));
    assertTrue(methodResult instanceof XLObject);
    // note that the array must be explicitly formed or there is a clash with getDoublesSum(Object...)
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()), _testObject.getDoublesSum(new int[] {1, 2}));
  }

  @Test
  public void testStringsSumMethod() {
    final Object methodResult = JMethod.jMethodX(_testObjectReference, XLString.of("getDoublesSum"), XLString.of("0"), XLString.of("4"));
    assertTrue(methodResult instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()), _testObject.getDoublesSum("0", "4"));
  }

  @Test
  public void testObjectsSumMethod() {
    final XLValue index1 = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(2));
    final XLValue index2 = JConstruct.jconstruct(XLString.of("java.lang.Long"), XLNumber.of(3));
    final XLValue index3 = JConstruct.jconstruct(XLString.of("java.lang.Float"), XLNumber.of(1));
    final Object methodResult = JMethod.jMethodX(_testObjectReference, XLString.of("getDoublesSum"), index1, index2, index3);
    assertTrue(methodResult instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()),
        _testObject.getDoublesSum(Short.valueOf("2"), Long.valueOf(3L), Float.valueOf(1F)));
  }

  @Test
  public void testLongStringIntsSumMethod() {
    final XLValue offset = JConstruct.jconstruct(XLString.of("java.lang.Long"), XLNumber.of(1000));
    final Object methodResult =
        JMethod.jMethodX(_testObjectReference, XLString.of("getDoublesSum"), offset, XLString.of("Test1"), XLNumber.of(0), XLNumber.of(3));
    assertTrue(methodResult instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()), _testObject.getDoublesSum(1000L, "Test1", 0, 3));
  }

  @Test
  public void testLongStringStringsSumMethod() {
    final XLValue offset = JConstruct.jconstruct(XLString.of("java.lang.Long"), XLNumber.of(2000));
    final Object methodResult =
        JMethod.jMethodX(_testObjectReference, XLString.of("getDoublesSum"), offset, XLString.of("Test2"), XLNumber.of(1), XLNumber.of(4));
    assertTrue(methodResult instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()), _testObject.getDoublesSum(2000L, "Test2", 1, 4));
  }

  @Test
  public void testLongStringObjectsSumMethod() {
    final XLValue offset = JConstruct.jconstruct(XLString.of("java.lang.Long"), XLNumber.of(3000));
    final XLValue index1 = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(2));
    final XLValue index2 = JConstruct.jconstruct(XLString.of("java.lang.Long"), XLNumber.of(3));
    final XLValue index3 = JConstruct.jconstruct(XLString.of("java.lang.Float"), XLNumber.of(0));
    final Object methodResult =
        JMethod.jMethodX(_testObjectReference, XLString.of("getDoublesSum"), offset, XLString.of("Test3"), index1, index2, index3);
    assertTrue(methodResult instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()),
        _testObject.getDoublesSum(3000L, "Test3", Short.valueOf("2"), Long.valueOf(3L), Float.valueOf(1F)));
  }
}
