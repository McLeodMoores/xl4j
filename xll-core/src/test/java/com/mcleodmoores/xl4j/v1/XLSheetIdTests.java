/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.values.XLSheetId;

/**
 * Unit tests for XLSheetId.
 */
public final class XLSheetIdTests {
  private static final String EXPECTED_65536 = "XLSheetId[sheetId=65536]";
  private static final String EXPECTED_0 = "XLSheetId[sheetId=0]";

  @Test
  public void testXLSheetIdEqualsAndHashCode() {
    final XLSheetId sheetId65536 = XLSheetId.of(65536);
    final XLSheetId sheetId65536_1 = XLSheetId.of(65536);
    Assert.assertEquals(sheetId65536.getSheetId(), 65536);
    final XLSheetId sheetId0 = XLSheetId.of(0);
    Assert.assertEquals(sheetId0.getSheetId(), 0);
    final XLSheetId sheetId0_1 = XLSheetId.of(0);
    Assert.assertEquals(sheetId65536, sheetId65536);
    Assert.assertEquals(sheetId65536.hashCode(), sheetId65536.hashCode());
    Assert.assertEquals(sheetId65536, sheetId65536_1);
    Assert.assertEquals(sheetId65536.hashCode(), sheetId65536_1.hashCode());

    Assert.assertEquals(sheetId0, sheetId0);
    Assert.assertEquals(sheetId0.hashCode(), sheetId0.hashCode());
    Assert.assertEquals(sheetId0, sheetId0_1);
    Assert.assertEquals(sheetId0.hashCode(), sheetId0_1.hashCode());

    Assert.assertNotEquals(sheetId65536, sheetId0);
    Assert.assertNotEquals(sheetId65536.hashCode(), sheetId0.hashCode());


    Assert.assertNotEquals(null, sheetId0);
    Assert.assertNotEquals("Hello", sheetId0);
  }

  @Test
  public void testXLSheetIdToString() {
    final XLSheetId sheetId65536 = XLSheetId.of(65536);
    final XLSheetId sheetId0 = XLSheetId.of(0);
    Assert.assertEquals(sheetId65536.toString(), EXPECTED_65536);
    Assert.assertEquals(sheetId0.toString(), EXPECTED_0);
  }
}
