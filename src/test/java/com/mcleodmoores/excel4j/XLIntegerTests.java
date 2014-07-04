/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.values.XLInteger;

/**
 * Unit tests for XLBigData.
 */
public final class XLIntegerTests {
  // CHECKSTYLE:OFF
  private static final String EXPECTED_65536 = "XLInteger[value=65536]";
  private static final String EXPECTED_0 = "XLInteger[value=0]";

  @Test
  public void testXLIntegerEqualsAndHashCode() {
    XLInteger integer65536 = XLInteger.of(65536);
    XLInteger integer65536_1 = XLInteger.of(65536);
    Assert.assertEquals(integer65536.getValue(), 65536);
    XLInteger integer0 = XLInteger.of(0);
    Assert.assertEquals(integer0.getValue(), 0);
    XLInteger integer0_1 = XLInteger.of(0);
    Assert.assertEquals(integer65536, integer65536);
    Assert.assertEquals(integer65536.hashCode(), integer65536.hashCode());
    Assert.assertEquals(integer65536, integer65536_1);
    Assert.assertEquals(integer65536.hashCode(), integer65536_1.hashCode());

    Assert.assertEquals(integer0, integer0);
    Assert.assertEquals(integer0.hashCode(), integer0.hashCode());
    Assert.assertEquals(integer0, integer0_1);
    Assert.assertEquals(integer0.hashCode(), integer0_1.hashCode());

    Assert.assertNotEquals(integer65536, integer0);
    Assert.assertNotEquals(integer65536.hashCode(), integer0.hashCode());
  }

  @Test
  public void testXLIntegerToString() {
    XLInteger integer65536 = XLInteger.of(65536);
    XLInteger integer0 = XLInteger.of(0);
    Assert.assertEquals(integer65536.toString(), EXPECTED_65536);
    Assert.assertEquals(integer0.toString(), EXPECTED_0);
  }
}
