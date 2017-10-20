/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.values.XLInteger;

/**
 * Unit tests for XLInteger.
 */
public class XLIntegerTests {
  private static final String EXPECTED_65536 = "XLInteger[value=65536]";
  private static final String EXPECTED_0 = "XLInteger[value=0]";

  // CHECKSTYLE:OFF
  /**
   *
   */
  @Test
  public void testXLIntegerEqualsAndHashCode() {
    final XLInteger integer65536 = XLInteger.of(65536);
    final XLInteger integer65536_1 = XLInteger.of(65536);
    Assert.assertEquals(integer65536.getValue(), 65536);
    final XLInteger integer0 = XLInteger.of(0);
    Assert.assertEquals(integer0.getValue(), 0);
    final XLInteger integer0_1 = XLInteger.of(0);
    Assert.assertEquals(integer65536, integer65536);
    Assert.assertEquals(integer65536.hashCode(), integer65536.hashCode());
    Assert.assertEquals(integer65536, integer65536_1);
    Assert.assertEquals(integer65536.hashCode(), integer65536_1.hashCode());

    Assert.assertEquals(integer0, integer0);
    Assert.assertEquals(integer0.hashCode(), integer0.hashCode());
    Assert.assertEquals(integer0, integer0_1);
    Assert.assertEquals(integer0.hashCode(), integer0_1.hashCode());

    Assert.assertNotEquals(integer65536, integer0);
    Assert.assertNotEquals(null, integer0);
    Assert.assertNotEquals("Hello", integer0);
    Assert.assertNotEquals(integer65536.hashCode(), integer0.hashCode());
  }

  /**
   *
   */
  @Test
  public void testXLIntegerToString() {
    final XLInteger integer65536 = XLInteger.of(65536);
    final XLInteger integer0 = XLInteger.of(0);
    Assert.assertEquals(integer65536.toString(), EXPECTED_65536);
    Assert.assertEquals(integer0.toString(), EXPECTED_0);
  }
}
