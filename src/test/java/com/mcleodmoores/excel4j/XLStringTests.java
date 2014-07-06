/**
 * Copyright (C) 2EMPTY14-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLString;

/**
 * Unit tests for XLString.
 */
public final class XLStringTests {
  // CHECKSTYLE:OFF
	private static final String HELLO_WORLD = "Hello World";
	private static final String HELLO_WORLD_1 = "Hello World";
	private static final String EMPTY = "";
	private static final String EMPTY_1 = "";
  private static final String EXPECTED_HELLO_WORLD = "XLString[value=Hello World]";
  private static final String EXPECTED_EMPTY = "XLString[value=]";

  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNull() {
  	XLString.of(null);
  }
  
  @Test
  public void testXLStringEqualsAndHashCode() {
    XLString helloWorld = XLString.of(HELLO_WORLD);
    XLString helloWorld_1 = XLString.of(HELLO_WORLD_1);
    Assert.assertEquals(helloWorld.getValue(), HELLO_WORLD);
    XLString empty = XLString.of(EMPTY);
    Assert.assertEquals(empty.getValue(), EMPTY);
    XLString empty_1 = XLString.of(EMPTY_1);
    Assert.assertEquals(helloWorld, helloWorld);
    Assert.assertEquals(helloWorld.hashCode(), helloWorld.hashCode());
    Assert.assertEquals(helloWorld, helloWorld_1);
    Assert.assertEquals(helloWorld.hashCode(), helloWorld_1.hashCode());

    Assert.assertEquals(empty, empty);
    Assert.assertEquals(empty.hashCode(), empty.hashCode());
    Assert.assertEquals(empty, empty_1);
    Assert.assertEquals(empty.hashCode(), empty_1.hashCode());

    Assert.assertNotEquals(helloWorld, empty);
    Assert.assertNotEquals(helloWorld.hashCode(), empty.hashCode());
  }

  @Test
  public void testXLStringToString() {
    XLString helloWorld = XLString.of(HELLO_WORLD);
    XLString empty = XLString.of(EMPTY);
    Assert.assertEquals(helloWorld.toString(), EXPECTED_HELLO_WORLD);
    Assert.assertEquals(empty.toString(), EXPECTED_EMPTY);
  }
  
}
