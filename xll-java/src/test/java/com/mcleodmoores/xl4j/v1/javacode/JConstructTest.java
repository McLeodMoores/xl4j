/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLInteger;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.javacode.testutils.TestObject;
import com.mcleodmoores.xl4j.v1.javacode.testutils.TestObject2;

/**
 * Unit tests for {@link JConstruct}.
 */
public class JConstructTest {
  private static final XLString CLASS = XLString.of("com.mcleodmoores.xl4j.v1.javacode.testutils.TestObject");

  /**
   * Tests the case where the class name is wrong.
   */
  @Test
  public void testWrongClassName() {
    final XLValue constructed = JConstruct.jconstruct(XLString.of("java.util.StringBuilder"), XLString.of("A"));
    assertSame(constructed, XLError.Null);
  }

  /**
   * Tests the case where the number of arguments is wrong.
   */
  @Test
  public void testWrongNumberOfArguments() {
    final XLValue constructed = JConstruct.jconstruct(XLString.of("java.lang.StringBuilder"), XLNumber.of(1), XLNumber.of(2));
    assertSame(constructed, XLError.Null);
  }

  /**
   * Tests the case where the arguments are of the wrong type.
   */
  @Test
  public void testWrongTypeOfArguments() {
    final XLValue constructed = JConstruct.jconstruct(XLString.of("java.lang.StringBuilder"), XLArray.of(new XLValue[][] {new XLValue[] {XLNumber.of(1)}}));
    assertSame(constructed, XLError.Null);
  }

  /**
   *
   */
  @Test
  public void testNoArgsConstructor() {
    final XLValue constructed = JConstruct.jconstruct(CLASS, new XLValue[0]);
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = new TestObject();
    assertEquals(constructedObject, expectedObject);
  }

  /**
   * No converters for XLInteger.
   */
  @Test
  public void testExpectedFailureIntConstructor() {
    final XLValue constructed = JConstruct.jconstruct(CLASS, XLInteger.of(1));
    assertTrue(constructed instanceof XLError);
  }

  /**
   *
   */
  @Test
  public void testIntConstructor() {
    final XLValue constructed = JConstruct.jconstruct(CLASS, XLNumber.of(1));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = new TestObject(1);
    assertEquals(constructedObject, expectedObject);
  }

  /**
   *
   */
  @Test
  public void testObjectConstructor() {
    final XLValue number = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(3));
    final XLValue constructed = JConstruct.jconstruct(CLASS, number);
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = new TestObject(Short.valueOf("3"));
    assertEquals(constructedObject, expectedObject);
  }

  /**
   * Tests a varargs constructor.
   */
  @Test
  public void testVarargsConstructor() {
    final XLValue constructed =
        JConstruct.jconstruct(XLString.of("com.mcleodmoores.xl4j.v1.javacode.testutils.TestObject2"), XLNumber.of(2.5), XLString.of("$"));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject2);
    final TestObject2 expectedObject = new TestObject2(2.5, "$");
    assertEquals(constructedObject, expectedObject);
  }
}
