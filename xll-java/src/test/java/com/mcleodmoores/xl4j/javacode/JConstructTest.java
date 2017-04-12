/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.ExcelFactory;
import com.mcleodmoores.xl4j.javacode.testutils.TestObject;
import com.mcleodmoores.xl4j.values.XLError;
import com.mcleodmoores.xl4j.values.XLInteger;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 *
 */
public class JConstructTest {
  private static final XLString CLASS = XLString.of("com.mcleodmoores.xl4j.javacode.testutils.TestObject");

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
