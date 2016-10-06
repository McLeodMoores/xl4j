/**
 * Copyright (C) 2EMPTY14-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;

/**
 * Unit tests for XLString.
 */
public class XLStringTests {
  private static final String HELLO_WORLD = "Hello World";
  private static final String HELLO_WORLD_1 = "Hello World";
  private static final String EMPTY = "";
  private static final String EMPTY_1 = "";
  private static final String EXPECTED_HELLO_WORLD = "XLString[value=Hello World]";
  private static final String EXPECTED_EMPTY = "XLString[value=]";

  /**
   * Tests the exception thrown when the input is null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNull() {
    XLString.of(null);
  }

  /**
   * Tests the hashCode and equals methods.
   */
  @Test
  public void testXLStringEqualsAndHashCode() {
    final XLString helloWorld = XLString.of(HELLO_WORLD);
    final XLString helloWorld_1 = XLString.of(HELLO_WORLD_1);
    Assert.assertEquals(helloWorld.getValue(), HELLO_WORLD);
    final XLString empty = XLString.of(EMPTY);
    Assert.assertEquals(empty.getValue(), EMPTY);
    final XLString empty_1 = XLString.of(EMPTY_1);
    Assert.assertEquals(helloWorld, helloWorld);
    Assert.assertEquals(helloWorld.hashCode(), helloWorld.hashCode());
    Assert.assertEquals(helloWorld, helloWorld_1);
    Assert.assertEquals(helloWorld.hashCode(), helloWorld_1.hashCode());

    Assert.assertEquals(empty, empty);
    Assert.assertEquals(empty.hashCode(), empty.hashCode());
    Assert.assertEquals(empty, empty_1);
    Assert.assertEquals(empty.hashCode(), empty_1.hashCode());

    Assert.assertNotEquals(helloWorld, empty);
    Assert.assertNotEquals(null, empty);
    Assert.assertNotEquals("Hello World", empty);
    Assert.assertNotEquals(helloWorld.hashCode(), empty.hashCode());
  }

  /**
   * Tests the toString method.
   */
  @Test
  public void testXLStringToString() {
    final XLString helloWorld = XLString.of(HELLO_WORLD);
    final XLString empty = XLString.of(EMPTY);
    Assert.assertEquals(helloWorld.toString(), EXPECTED_HELLO_WORLD);
    Assert.assertEquals(empty.toString(), EXPECTED_EMPTY);
  }

  /**
   * Tests that a string can be identified correctly.
   */
  @Test
  public void testIsXLObject() {
    final long handle = 123L;
    final XLObject xlObject = XLObject.of(String.class, handle);
    assertTrue(xlObject.toXLString().isXLObject());
    assertFalse(XLString.of(HELLO_WORLD).isXLObject());
  }
}
