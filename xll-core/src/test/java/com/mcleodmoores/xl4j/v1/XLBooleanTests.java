/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.values.XLBoolean;
/**
 * Unit tests for XLBoolean.
 */
public final class XLBooleanTests {
  private static final String EXPECTED_TRUE = "XLBoolean[TRUE]";
  private static final String EXPECTED_FALSE = "XLBoolean[FALSE]";

  @Test
  public void testXLBooleanEqualsAndHashCode() {
    final XLBoolean boolT = XLBoolean.from(true);
    assertTrue(boolT.getValue());
    final XLBoolean boolT1 = XLBoolean.from(true);
    final XLBoolean boolF = XLBoolean.from(false);
    assertFalse(boolF.getValue());
    final XLBoolean boolF1 = XLBoolean.from(false);
    assertEquals(boolT, boolT);
    assertEquals(boolT.hashCode(), boolT.hashCode());
    assertEquals(boolT, boolT1);
    assertEquals(boolT.hashCode(), boolT1.hashCode());

    assertEquals(boolF, boolF);
    assertEquals(boolF.hashCode(), boolF.hashCode());
    assertEquals(boolF, boolF1);
    assertEquals(boolF.hashCode(), boolF1.hashCode());

    assertNotEquals(boolT, boolF);
    assertNotEquals(null, boolF);
    assertNotEquals("Hello", boolF);
    assertNotEquals(boolT.hashCode(), boolF.hashCode());
  }

  @Test
  public void testXLBooleanToString() {
    final XLBoolean boolT = XLBoolean.from(true);
    final XLBoolean boolF = XLBoolean.from(false);
    assertEquals(boolT.toString(), EXPECTED_TRUE);
    assertEquals(boolF.toString(), EXPECTED_FALSE);
  }

  /**
   * Tests the order of the fields - if this is changed, then it will break the C++ side.
   */
  @Test
  public void testFieldOrder() {
    final XLBoolean[] values = XLBoolean.values();
    assertEquals(values[0], XLBoolean.TRUE);
    assertEquals(values[1], XLBoolean.FALSE);
  }
}
