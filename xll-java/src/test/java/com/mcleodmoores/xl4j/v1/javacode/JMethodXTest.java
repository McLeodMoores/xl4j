/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.javacode.testutils.TestObject;

/**
 * Unit tests for {@link JMethod#jMethodX(XLObject, XLString, XLValue...)}.
 */
public class JMethodXTest {
  private static final XLString CLASS = XLString.of("com.mcleodmoores.xl4j.v1.javacode.testutils.TestObject");
  private TestObject _testObject;
  private XLObject _testObjectReference;
  private double[] _arrayValues;

  /**
   *
   */
  @BeforeTest
  public void init() {
    final XLValue[][] xlArrayValues = new XLValue[100][1];
    _arrayValues = new double[100];
    for (int i = 0; i < 100; i++) {
      xlArrayValues[i][0] = XLNumber.of(-1234 * i);
      _arrayValues[i] = -1234 * i;
    }
    final XLValue xlValue = JConstruct.jconstruct(CLASS, XLNumber.of(10.), XLArray.of(xlArrayValues), XLString.of("name"));
    assertTrue(xlValue instanceof XLObject);
    _testObjectReference = (XLObject) xlValue;
    _testObject = (TestObject) ExcelFactory.getInstance().getHeap().getObject(_testObjectReference.getHandle());
  }

  /**
   * Tests the result when the method cannot be found.
   */
  @Test
  public void testNoMethod() {
    final XLValue stringReference = JConstruct.jconstruct(XLString.of("java.lang.StringBuilder"), XLString.of("builder"));
    assertTrue(stringReference instanceof XLObject);
    final Object methodResult = JMethod.jMethodX((XLObject) stringReference, XLString.of("noMethodCalledThis"), new XLValue[0]);
    assertSame(methodResult, XLError.Null);
  }

  /**
   * Tests wrong number of arguments.
   */
  @Test
  public void testWrongNumberOfArguments() {
    final XLValue stringReference = JConstruct.jconstruct(XLString.of("java.lang.StringBuilder"), XLString.of("builder"));
    assertTrue(stringReference instanceof XLObject);
    final Object methodResult = JMethod.jMethodX((XLObject) stringReference, XLString.of("toString"), XLNumber.of(10), XLNumber.of(20));
    assertSame(methodResult, XLError.Null);
  }

  /**
   * Tests wrong type of arguments.
   */
  @Test
  public void testWrongTypeOfArguments() {
    final XLValue stringReference = JConstruct.jconstruct(XLString.of("java.lang.StringBuilder"), XLString.of("builder"));
    assertTrue(stringReference instanceof XLObject);
    final Object methodResult = JMethod.jMethodX((XLObject) stringReference, XLString.of("substring"), XLString.of("A"));
    assertSame(methodResult, XLError.Null);
  }

  /**
   *
   */
  @Test
  public void testReturnType() {
    final XLValue stringReference = JConstruct.jconstruct(XLString.of("java.lang.StringBuilder"), XLString.of("builder"));
    assertTrue(stringReference instanceof XLObject);
    final Object methodResult1 = JMethod.jMethodX((XLObject) stringReference, XLString.of("toString"), new XLValue[0]);
    final Object methodResult2 = JMethod.jMethodX((XLObject) stringReference, XLString.of("length"), new XLValue[0]);
    // expect references
    assertTrue(methodResult1 instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult1).getHandle()), "builder");
    assertTrue(methodResult2 instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult2).getHandle()), 7);
  }

  /**
   *
   */
  @Test
  public void testNonOverloadedGetters() {
    Object methodResult = JMethod.jMethodX(_testObjectReference, XLString.of("getNumber"), new XLValue[0]);
    assertTrue(methodResult instanceof XLObject);
    // XLNumber internally stores all numbers as doubles
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()), _testObject.getNumber());
    methodResult = JMethod.jMethodX(_testObjectReference, XLString.of("getDoubles"), new XLValue[0]);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()), _arrayValues);
  }

  /**
   *
   */
  @Test
  public void testNoArgsGetMethod() {
    final Object methodResult = JMethod.jMethodX(_testObjectReference, XLString.of("getDouble"), new XLValue[0]);
    assertTrue(methodResult instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()), _testObject.getDouble());
  }

  /**
   *
   */
  @Test
  public void testIntGetMethod() {
    final Object methodResult = JMethod.jMethodX(_testObjectReference, XLString.of("getDouble"), XLNumber.of(1));
    assertTrue(methodResult instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()), _testObject.getDouble(1));
  }

  /**
   *
   */
  @Test
  public void testStringGetMethod() {
    final Object methodResult = JMethod.jMethodX(_testObjectReference, XLString.of("getDouble"), XLString.of("2"));
    assertTrue(methodResult instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()), _testObject.getDouble("2"));
  }

  /**
   *
   */
  @Test
  public void testObjectGetMethod() {
    final XLValue number = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(3));
    final Object methodResult = JMethod.jMethodX(_testObjectReference, XLString.of("getDouble"), number);
    assertTrue(methodResult instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()), _testObject.getDouble(Short.valueOf((short) 3)));
  }

  /**
   *
   */
  @Test
  public void testIntsSumMethod() {
    final Object methodResult = JMethod.jMethodX(_testObjectReference, XLString.of("getDoublesSum"), XLNumber.of(1), XLNumber.of(2));
    assertTrue(methodResult instanceof XLObject);
    // note that the array must be explicitly formed or there is a clash with getDoublesSum(Object...)
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()), _testObject.getDoublesSum(new int[] {1, 2}));
  }

  /**
   *
   */
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

  /**
   *
   */
  @Test
  public void testIntStringIntsSumMethod() {
    final XLValue offset = JConstruct.jconstruct(XLString.of("java.lang.Integer"), XLNumber.of(1000));
    final Object methodResult =
        JMethod.jMethodX(_testObjectReference, XLString.of("getDoublesSum"), offset, XLString.of("Test1"), XLNumber.of(0), XLNumber.of(3));
    assertTrue(methodResult instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()),
        _testObject.getDoublesSum(1000, "Test1", new int[] {0, 3}));
  }

  /**
   *
   */
  @Test
  public void testIntStringStringsSumMethod() {
    final XLValue offset = JConstruct.jconstruct(XLString.of("java.lang.Integer"), XLNumber.of(2000));
    final Object methodResult =
        JMethod.jMethodX(_testObjectReference, XLString.of("getDoublesSum"), offset, XLString.of("Test2"), XLNumber.of(1), XLNumber.of(3));
    assertTrue(methodResult instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()),
        _testObject.getDoublesSum(2000, "Test2", new String[] {"1", "3"}));
  }

  /**
   *
   */
  @Test
  public void testIntStringObjectsSumMethod() {
    final XLValue offset = JConstruct.jconstruct(XLString.of("java.lang.Integer"), XLNumber.of(3000));
    final XLValue index1 = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(2));
    final XLValue index2 = JConstruct.jconstruct(XLString.of("java.lang.Long"), XLNumber.of(3));
    final Object methodResult =
        JMethod.jMethodX(_testObjectReference, XLString.of("getDoublesSum"), offset, XLString.of("Test3"), index1, index2, XLString.of("1"));
    assertTrue(methodResult instanceof XLObject);
    assertEquals(ExcelFactory.getInstance().getHeap().getObject(((XLObject) methodResult).getHandle()),
        _testObject.getDoublesSum(3000, "Test3", new Object[] {Short.valueOf("2"), Long.valueOf(3L), "1"}));
  }
}
