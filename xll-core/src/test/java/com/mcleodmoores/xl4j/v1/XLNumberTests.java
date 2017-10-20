/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1;

import static org.testng.Assert.assertEquals;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.values.XLNumber;

/**
 * Unit tests for XLNumber.
 */
public class XLNumberTests {
  private static final String EXPECTED_987_654321 = "XLNumber[value=987.654321]";
  private static final String EXPECTED_0_0 = "XLNumber[value=0.0]";

  // CHECKSTYLE:OFF
  /**
   *
   */
  @Test
  public void testXLNumberEqualsAndHashCode() {
    final XLNumber longNum = XLNumber.of(48039284324380L);
    Assert.assertEquals(longNum.getValue(), 48039284324380d);
    final XLNumber number987_654321 = XLNumber.of(987.654321);
    final XLNumber number987_654321_1 = XLNumber.of(987.654321);
    Assert.assertEquals(number987_654321.getValue(), 987.654321);
    final XLNumber number0 = XLNumber.of(0d);
    Assert.assertEquals(number0.getValue(), 0d);
    final XLNumber number0_1 = XLNumber.of(0d);
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

  /**
   *
   */
  @Test
  public void testXLNumberToString() {
    final XLNumber number987_654321 = XLNumber.of(987.654321);
    final XLNumber number0 = XLNumber.of(0);
    Assert.assertEquals(number987_654321.toString(), EXPECTED_987_654321);
    Assert.assertEquals(number0.toString(), EXPECTED_0_0);
  }

  /**
   *
   */
  @Test
  public void testXLNumberAsType() {
    XLNumber number = XLNumber.of(100);
    assertEquals(number.getAsDouble(), 100d);
    assertEquals(number.getAsFloat(), 100f);
    assertEquals(number.getAsInt(), 100);
    assertEquals(number.getAsLong(), 100L);
    assertEquals(number.getAsShort(), (short) 100);
    number = XLNumber.of(123.45);
    assertEquals(number.getAsDouble(), 123.45d);
    assertEquals(number.getAsFloat(), 123.45f);
    assertEquals(number.getAsInt(), 123);
    assertEquals(number.getAsLong(), 123L);
    assertEquals(number.getAsShort(), (short) 123);
  }
}
