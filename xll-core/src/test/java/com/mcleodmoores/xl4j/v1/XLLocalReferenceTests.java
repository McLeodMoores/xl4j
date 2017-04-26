/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.values.XLLocalReference;
import com.mcleodmoores.xl4j.v1.api.values.XLRange;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for XLLocalReference.
 */
public class XLLocalReferenceTests {

  /**
   *
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testFirstArgNegative() {
    XLLocalReference.of(null);
  }

  /**
   *
   */
  @Test
  public void testConstructionAndGetter() {
    final XLRange xlRange = XLRange.of(1, 100, 2, 3);
    final XLLocalReference localRef = XLLocalReference.of(xlRange);
    Assert.assertEquals(localRef.getRange(), xlRange);
  }

  /**
   *
   */
  @Test
  public void testEqualsAndHashCode() {
    final XLRange range = XLRange.of(0, 15, 0, 15);
    final XLRange range_1 = XLRange.of(1, 15, 0, 15);
    final XLRange range_2 = XLRange.of(0, 15, 0, 15);
    final XLLocalReference localRef = XLLocalReference.of(range);
    final XLLocalReference localRef_1 = XLLocalReference.of(range_1);
    final XLLocalReference localRef_2 = XLLocalReference.of(range_2);
    Assert.assertEquals(localRef, localRef);
    Assert.assertNotEquals(null, localRef);
    Assert.assertNotEquals("Hello", localRef);
    Assert.assertEquals(localRef, localRef_2);
    Assert.assertEquals(localRef.hashCode(), localRef.hashCode());
    Assert.assertEquals(localRef.hashCode(), localRef_2.hashCode());

    Assert.assertNotEquals(localRef, localRef_1);
    Assert.assertNotEquals(localRef.hashCode(), localRef_1.hashCode());
  }

  private static final String SINGLE_ROW = "XLLocalReference[range=XLRange[Single Row row=100, columns=3 to 5]]";
  private static final String SINGLE_COL = "XLLocalReference[range=XLRange[Single Column rows=100 to 104, column=3]]";
  private static final String SINGLE_CELL = "XLLocalReference[range=XLRange[Single Cell row=100, column=3]]";
  private static final String FULL_RANGE = "XLLocalReference[range=XLRange[Range rows=10 to 45, columns=0 to 30]]";

  /**
   *
   */
  @Test
  public void testToString() {
    final XLRange singleRow = XLRange.of(100, 100, 3, 5);
    final XLLocalReference singleRowRef = XLLocalReference.of(singleRow);
    Assert.assertEquals(singleRowRef.toString(), SINGLE_ROW);
    final XLRange singleCol = XLRange.of(100, 104, 3, 3);
    final XLLocalReference singleColRef = XLLocalReference.of(singleCol);
    Assert.assertEquals(singleColRef.toString(), SINGLE_COL);
    final XLRange singleCell = XLRange.of(100, 100, 3, 3);
    final XLLocalReference singleCellRef = XLLocalReference.of(singleCell);
    Assert.assertEquals(singleCellRef.toString(), SINGLE_CELL);
    final XLRange singleCell2 = XLRange.ofCell(100, 3);
    final XLLocalReference singleCell2Ref = XLLocalReference.of(singleCell2);
    Assert.assertEquals(singleCell2Ref.toString(), SINGLE_CELL);
    final XLRange fullRange = XLRange.of(10, 45, 0, 30);
    final XLLocalReference fullRangeRef = XLLocalReference.of(fullRange);
    Assert.assertEquals(fullRangeRef.toString(), FULL_RANGE);
  }
}
