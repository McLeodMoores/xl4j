/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLBigData;
import com.mcleodmoores.xl4j.values.XLBoolean;
import com.mcleodmoores.xl4j.values.XLError;
import com.mcleodmoores.xl4j.values.XLInteger;
import com.mcleodmoores.xl4j.values.XLLocalReference;
import com.mcleodmoores.xl4j.values.XLMissing;
import com.mcleodmoores.xl4j.values.XLMultiReference;
import com.mcleodmoores.xl4j.values.XLNil;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLRange;
import com.mcleodmoores.xl4j.values.XLSheetId;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Unit tests for XLArray.
 */
public class XLArrayTest {

  private static final XLValue[][] SINGLE = new XLValue[][] { { XLBigData.of("Hello World") } };
  private static final XLValue[][] SINGLE_1 = new XLValue[][] { { XLBigData.of("Hello World") } };

  private static final XLValue[][] MULTI = new XLValue[][] {
    { XLBoolean.from(true), XLError.NA, XLInteger.of(65536) },
    { XLLocalReference.of(XLRange.of(1, 1, 3, 5)), XLMissing.INSTANCE, XLNil.INSTANCE },
    { XLMultiReference.of(XLSheetId.of(1234), XLRange.of(1, 2, 3, 4), XLRange.ofCell(4, 5)), XLNumber.of(43.234) },
    { XLString.of("Hello World"), XLError.Null, XLError.Ref }
  };
  private static final XLValue[][] MULTI_1 = new XLValue[][] {
    { XLBoolean.from(true), XLError.NA, XLInteger.of(65536) },
    { XLLocalReference.of(XLRange.of(1, 1, 3, 5)), XLMissing.INSTANCE, XLNil.INSTANCE },
    { XLMultiReference.of(XLSheetId.of(1234), XLRange.of(1, 2, 3, 4), XLRange.ofCell(4, 5)), XLNumber.of(43.234) },
    { XLString.of("Hello World"), XLError.Null, XLError.Ref }
  };
  private static final XLValue[][] MULTI_2 = new XLValue[][] { // Note that the XLRange.ofCell() is changed from above.
    { XLBoolean.from(true), XLError.NA, XLInteger.of(65536) },
    { XLLocalReference.of(XLRange.of(1, 1, 3, 5)), XLMissing.INSTANCE, XLNil.INSTANCE },
    { XLMultiReference.of(XLSheetId.of(1234), XLRange.of(1, 2, 3, 4), XLRange.ofCell(5, 5)), XLNumber.of(43.234) },
    { XLString.of("Hello World"), XLError.Null, XLError.Ref }
  };

  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNull() {
    XLArray.of(null);
  }

  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testEmpty() {
    XLArray.of(new XLValue[][] {});
  }

  @Test // (expectedExceptions = Excel4JRuntimeException.class) - this perhaps should be caught, enable this if we decide to check for it.
  public void testEmptyNested() {
    XLArray.of(new XLValue[][] {{}}); // single empty array: [1][0]
  }

  @Test
  public void testConstructionAndGetterSingle() {
    final XLArray localRef = XLArray.of(SINGLE);
    Assert.assertEquals(localRef.getArray(), SINGLE);
  }

  @Test
  public void testConstructionAndGetterMulti() {
    final XLArray localRef = XLArray.of(MULTI);
    Assert.assertEquals(localRef.getArray(), MULTI);
  }

  @Test
  public void testEqualsAndHashCode() {
    final XLArray single = XLArray.of(SINGLE);
    final XLArray single_1 = XLArray.of(SINGLE_1);
    final XLArray multi = XLArray.of(MULTI);
    final XLArray multi_1 = XLArray.of(MULTI_1);
    final XLArray multi_2 = XLArray.of(MULTI_2);
    Assert.assertEquals(single, single);
    Assert.assertNotEquals(null, single);
    Assert.assertNotEquals("Hello", single);
    Assert.assertEquals(single, single_1);
    Assert.assertNotEquals(null, multi);
    Assert.assertEquals(multi, multi);
    Assert.assertEquals(multi, multi_1);
    Assert.assertNotEquals(single, multi);
    Assert.assertNotEquals(multi, single);
    Assert.assertNotEquals(multi, multi_2);

    // hashCode
    Assert.assertEquals(single.hashCode(), single.hashCode());
    Assert.assertEquals(single.hashCode(), single_1.hashCode());
    Assert.assertNotEquals(single.hashCode(), multi.hashCode());
    Assert.assertNotEquals(multi.hashCode(), multi_2.hashCode());
    Assert.assertEquals(multi.hashCode(), multi_1.hashCode());
  }

  private static final String SINGLE_TO_STRING = "XLArray[[[XLBigData[len=18, buffer=[AC ED 00 05 74 00 0B 48 65 6C 6C 6F 20 57 6F 72 6C 64]]]]]";
  private static final String MULTI_TO_STRING = "XLArray[[[XLBoolean[TRUE], NA, XLInteger[value=65536]], "
      + "[XLLocalReference[range=XLRange[Single Row row=1, columns=3 to 5]], XLMissing, XLNil], "
      + "[XLMultiReference[sheetId=1234, ranges=[XLRange[Range rows=1 to 2, columns=3 to 4], "
      + "XLRange[Single Cell row=4, column=5]]], XLNumber[value=43.234]], [XLString[value=Hello World], Null, Ref]]]";

  @Test
  public void testToString() {
    final XLArray single = XLArray.of(SINGLE);
    Assert.assertEquals(single.toString(), SINGLE_TO_STRING);
    final XLArray multi = XLArray.of(MULTI);
    Assert.assertEquals(multi.toString(), MULTI_TO_STRING);
  }

  /**
   * Tests the logic for determining if the array is a row, column or area.
   */
  @Test
  public void testRowColumnArea() {
    final XLArray row = XLArray.of(new XLValue[][] {{XLNumber.of(1), XLNumber.of(2), XLNumber.of(3), XLNumber.of(4)}});
    assertTrue(row.isRow());
    assertFalse(row.isColumn());
    assertFalse(row.isArea());
    final XLArray column = XLArray.of(new XLValue[][] {new XLValue[]{XLNumber.of(1)}, new XLValue[] {XLNumber.of(2)}, new XLValue[]{XLNumber.of(3)},
      new XLValue[]{XLNumber.of(4)}});
    assertFalse(column.isRow());
    assertTrue(column.isColumn());
    assertFalse(column.isArea());
    final XLArray area = XLArray.of(new XLValue[][] {new XLValue[]{XLNumber.of(1), XLNumber.of(2), XLNumber.of(3), XLNumber.of(4)},
      new XLValue[]{XLNumber.of(1), XLNumber.of(2), XLNumber.of(3), XLNumber.of(4)}});
    assertFalse(area.isRow());
    assertFalse(area.isColumn());
    assertTrue(area.isArea());
  }
}
