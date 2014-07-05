/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.values.XLBoolean;

/**
 * Unit tests for XLBoolean.
 */
public final class XLBooleanTests {
  // CHECKSTYLE:OFF
  private static final String EXPECTED_TRUE = "XLBoolean[value=true]";
  private static final String EXPECTED_FALSE = "XLBoolean[value=false]";
  
  @Test
  public void testXLBooleanEqualsAndHashCode() {
    XLBoolean boolT = XLBoolean.of(true);
    Assert.assertTrue(boolT.getValue());
    XLBoolean boolT1 = XLBoolean.of(true);
    XLBoolean boolF = XLBoolean.of(false);
    Assert.assertFalse(boolF.getValue());
    XLBoolean boolF1 = XLBoolean.of(false);
    Assert.assertEquals(boolT, boolT);
    Assert.assertEquals(boolT.hashCode(), boolT.hashCode());
    Assert.assertEquals(boolT, boolT1);
    Assert.assertEquals(boolT.hashCode(), boolT1.hashCode());
    
    Assert.assertEquals(boolF, boolF);
    Assert.assertEquals(boolF.hashCode(), boolF.hashCode());
    Assert.assertEquals(boolF, boolF1);
    Assert.assertEquals(boolF.hashCode(), boolF1.hashCode());
    
    Assert.assertNotEquals(boolT, boolF);
    Assert.assertNotEquals(boolT.hashCode(), boolF.hashCode());
  }
  
  @Test
  public void testXLBooleanToString() {
    XLBoolean boolT = XLBoolean.of(true);
    XLBoolean boolF = XLBoolean.of(false);
    Assert.assertEquals(boolT.toString(), EXPECTED_TRUE);
    Assert.assertEquals(boolF.toString(), EXPECTED_FALSE);
  }
}
