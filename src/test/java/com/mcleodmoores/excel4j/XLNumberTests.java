/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.values.XLNumber;

/**
 * Unit tests for XLNumber.
 */
public final class XLNumberTests {
  // CHECKSTYLE:OFF
  private static final String EXPECTED_987_654321 = "XLNumber[value=987.654321]";
  private static final String EXPECTED_0_0 = "XLNumber[value=0.0]";

  @Test
  public void testXLNumberEqualsAndHashCode() {
    XLNumber longNum = XLNumber.of(48039284324380L);
    Assert.assertEquals(longNum.getValue(), 48039284324380d);
    XLNumber number987_654321 = XLNumber.of(987.654321);
    XLNumber number987_654321_1 = XLNumber.of(987.654321);
    Assert.assertEquals(number987_654321.getValue(), 987.654321);
    XLNumber number0 = XLNumber.of(0d);
    Assert.assertEquals(number0.getValue(), 0d);
    XLNumber number0_1 = XLNumber.of(0d);
    Assert.assertEquals(number987_654321, number987_654321);
    Assert.assertEquals(number987_654321.hashCode(), number987_654321.hashCode());
    Assert.assertEquals(number987_654321, number987_654321_1);
    Assert.assertEquals(number987_654321.hashCode(), number987_654321_1.hashCode());

    Assert.assertEquals(number0, number0);
    Assert.assertEquals(number0.hashCode(), number0.hashCode());
    Assert.assertEquals(number0, number0_1);
    Assert.assertEquals(number0.hashCode(), number0_1.hashCode());

    Assert.assertNotEquals(number987_654321, number0);
    Assert.assertNotEquals(null, number0);
    Assert.assertNotEquals("Hello World", number0);
    Assert.assertNotEquals(number987_654321.hashCode(), number0.hashCode());
  }

  @Test
  public void testXLNumberToString() {
    XLNumber number987_654321 = XLNumber.of(987.654321);
    XLNumber number0 = XLNumber.of(0);
    Assert.assertEquals(number987_654321.toString(), EXPECTED_987_654321);
    Assert.assertEquals(number0.toString(), EXPECTED_0_0);
  }
  
}
