/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.ExcelFactory;
import com.mcleodmoores.xl4j.javacode.testutils.TestObject;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 *
 */
public class JMethodTest {
  private static final XLString CLASS = XLString.of("com.mcleodmoores.xl4j.javacode.testutils.TestObject");
  private TestObject _testObject;
  private XLObject _testObjectReference;

  @BeforeTest
  public void init() {
    final XLValue xlValue = JConstruct.jconstruct(CLASS, XLNumber.of(10.));
    assertTrue(xlValue instanceof XLObject);
    _testObjectReference = (XLObject) xlValue;
    _testObject = (TestObject) ExcelFactory.getInstance().getHeap().getObject(_testObjectReference.getHandle());
  }

  @Test
  public void testReturnType() {
    final XLValue stringReference = JConstruct.jconstruct(XLString.of("java.lang.StringBuilder"), XLString.of("builder"));
    assertTrue(stringReference instanceof XLObject);
    final Object methodResult1 = JMethod.jMethod((XLObject) stringReference, XLString.of("toString"), new XLValue[0]);
    final Object methodResult2 = JMethod.jMethod((XLObject) stringReference, XLString.of("length"), new XLValue[0]);
    // expect reduction to xl types
    assertEquals(methodResult1, XLString.of("builder"));
    assertEquals(methodResult2, XLNumber.of(7));
  }

  // This shouldn't really work anyway.
  @Test(enabled=false)
  public void testNonOverloadedGetters() {
    Object methodResult = JMethod.jMethod(_testObjectReference, XLString.of("getNumber"), new XLValue[0]);
    assertTrue(methodResult instanceof XLNumber);
    // XLNumber internally stores all numbers as doubles
    assertEquals(Double.valueOf(((XLNumber) methodResult).getValue()).intValue(), _testObject.getNumber());
    // TODO fails because arrays can't be returned
    methodResult = JMethod.jMethod(_testObjectReference, XLString.of("getDoubles"), new XLValue[0]);
    assertEquals(methodResult, new Double[] {10., 20., 30., 40., 50.});
  }

  @Test
  public void testNoArgsGetMethod() {
    final Object methodResult = JMethod.jMethod(_testObjectReference, XLString.of("getDouble"), new XLValue[0]);
    assertTrue(methodResult instanceof XLNumber);
    assertEquals(((XLNumber) methodResult).getValue(), _testObject.getDouble());
  }

  @Test
  public void testIntGetMethod() {
    final Object methodResult = JMethod.jMethod(_testObjectReference, XLString.of("getDouble"), XLNumber.of(1));
    assertTrue(methodResult instanceof XLNumber);
    assertEquals(((XLNumber) methodResult).getValue(), _testObject.getDouble(1));
  }

  @Test
  public void testStringGetMethod() {
    final Object methodResult = JMethod.jMethod(_testObjectReference, XLString.of("getDouble"), XLString.of("2"));
    assertTrue(methodResult instanceof XLNumber);
    assertEquals(((XLNumber) methodResult).getValue(), _testObject.getDouble("2"));
  }

  @Test
  public void testObjectGetMethod() {
    final XLValue number = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(3));
    final Object methodResult = JMethod.jMethod(_testObjectReference, XLString.of("getDouble"), number);
    assertTrue(methodResult instanceof XLNumber);
    assertEquals(((XLNumber) methodResult).getValue(), _testObject.getDouble(Short.valueOf((short) 3)));
  }

  @Test
  public void testIntsSumMethod() {
    final Object methodResult = JMethod.jMethod(_testObjectReference, XLString.of("getDoublesSum"), XLNumber.of(1), XLNumber.of(2));
    assertTrue(methodResult instanceof XLNumber);
    assertEquals(((XLNumber) methodResult).getValue(), _testObject.getDoublesSum(new int[] {1, 2}));
  }

  @Test
  public void testStringsSumMethod() {
    final Object methodResult = JMethod.jMethod(_testObjectReference, XLString.of("getDoublesSum"), XLString.of("0"), XLString.of("3"));
    assertTrue(methodResult instanceof XLNumber);
    assertEquals(((XLNumber) methodResult).getValue(), _testObject.getDoublesSum("0", "3"));
  }

  @Test
  public void testObjectsSumMethod() {
    final XLValue index1 = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(2));
    final XLValue index2 = JConstruct.jconstruct(XLString.of("java.lang.Long"), XLNumber.of(3));
    final Object methodResult = JMethod.jMethod(_testObjectReference, XLString.of("getDoublesSum"), index1, index2, XLString.of("1"));
    assertTrue(methodResult instanceof XLNumber);
    assertEquals(((XLNumber) methodResult).getValue(), _testObject.getDoublesSum(Short.valueOf("2"), Long.valueOf(3L), Float.valueOf(1F)));
  }

  @Test
  public void testIntStringIntsSumMethod() {
    final XLValue offset = JConstruct.jconstruct(XLString.of("java.lang.Integer"), XLNumber.of(1000));
    final Object methodResult =
        JMethod.jMethod(_testObjectReference, XLString.of("getDoublesSum"), offset, XLString.of("Test1"), XLNumber.of(0), XLNumber.of(3));
    assertTrue(methodResult instanceof XLNumber);
    assertEquals(((XLNumber) methodResult).getValue(), _testObject.getDoublesSum(1000, "Test1", new int[] {0, 3}));
  }

  @Test
  public void testIntStringStringsSumMethod() {
    final XLValue offset = JConstruct.jconstruct(XLString.of("java.lang.Integer"), XLNumber.of(2000));
    final Object methodResult =
        JMethod.jMethod(_testObjectReference, XLString.of("getDoublesSum"), offset, XLString.of("Test2"), XLNumber.of(1), XLNumber.of(3));
    assertTrue(methodResult instanceof XLNumber);
    assertEquals(((XLNumber) methodResult).getValue(), _testObject.getDoublesSum(2000, "Test2", new String[] {"1", "3"}));
  }

  @Test
  public void testIntStringObjectsSumMethod() {
    final XLValue offset = JConstruct.jconstruct(XLString.of("java.lang.Integer"), XLNumber.of(3000));
    final XLValue index1 = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(2));
    final XLValue index2 = JConstruct.jconstruct(XLString.of("java.lang.Long"), XLNumber.of(3));
    final Object methodResult =
        JMethod.jMethod(_testObjectReference, XLString.of("getDoublesSum"), offset, XLString.of("Test3"), index1, index2, XLString.of("1"));
    assertTrue(methodResult instanceof XLNumber);
    assertEquals(((XLNumber) methodResult).getValue(), _testObject.getDoublesSum(3000, "Test3", new Object[] {Short.valueOf("2"), Long.valueOf(3L), "1"}));
  }
}
