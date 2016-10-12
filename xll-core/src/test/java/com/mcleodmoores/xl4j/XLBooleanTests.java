/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.values.XLBoolean;

/**
 * Unit tests for XLBoolean.
 */
public final class XLBooleanTests {
  private static final String EXPECTED_TRUE = "XLBoolean[TRUE]";
  private static final String EXPECTED_FALSE = "XLBoolean[FALSE]";

  @Test
  public void testXLBooleanEqualsAndHashCode() {
    final XLBoolean boolT = XLBoolean.from(true);
    Assert.assertTrue(boolT.getValue());
    final XLBoolean boolT1 = XLBoolean.from(true);
    final XLBoolean boolF = XLBoolean.from(false);
    Assert.assertFalse(boolF.getValue());
    final XLBoolean boolF1 = XLBoolean.from(false);
    Assert.assertEquals(boolT, boolT);
    Assert.assertEquals(boolT.hashCode(), boolT.hashCode());
    Assert.assertEquals(boolT, boolT1);
    Assert.assertEquals(boolT.hashCode(), boolT1.hashCode());

    Assert.assertEquals(boolF, boolF);
    Assert.assertEquals(boolF.hashCode(), boolF.hashCode());
    Assert.assertEquals(boolF, boolF1);
    Assert.assertEquals(boolF.hashCode(), boolF1.hashCode());

    Assert.assertNotEquals(boolT, boolF);
    Assert.assertNotEquals(null, boolF);
    Assert.assertNotEquals("Hello", boolF);
    Assert.assertNotEquals(boolT.hashCode(), boolF.hashCode());
  }

  @Test
  public void testXLBooleanToString() {
    final XLBoolean boolT = XLBoolean.from(true);
    final XLBoolean boolF = XLBoolean.from(false);
    Assert.assertEquals(boolT.toString(), EXPECTED_TRUE);
    Assert.assertEquals(boolF.toString(), EXPECTED_FALSE);
  }
}
