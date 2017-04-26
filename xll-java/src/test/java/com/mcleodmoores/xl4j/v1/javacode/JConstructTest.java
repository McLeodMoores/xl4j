/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLInteger;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.javacode.testutils.TestObject;

/**
 *
 */
public class JConstructTest {
  private static final XLString CLASS = XLString.of("com.mcleodmoores.xl4j.v1.javacode.testutils.TestObject");

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

}
