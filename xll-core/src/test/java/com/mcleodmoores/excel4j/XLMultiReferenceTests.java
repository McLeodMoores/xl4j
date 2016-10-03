/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLMultiReference;
import com.mcleodmoores.excel4j.values.XLRange;
import com.mcleodmoores.excel4j.values.XLSheetId;

/**
 * Unit tests for XLMultiReference.
 */
public final class XLMultiReferenceTests {

  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testOneNull() {
    XLMultiReference.of(null);
  }

  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullArray() {
    XLMultiReference.of(null, (XLRange[]) null);
  }

  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testEmptyArray() {
    XLMultiReference.of(XLSheetId.of(0), new XLRange[] {});
  }

  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullList() {
    XLMultiReference.of(null, (List<XLRange>) null);
  }

  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testEmptyList() {
    XLMultiReference.of(XLSheetId.of(0), new ArrayList<XLRange>());
  }

  @Test
  public void testSingleConstructionAndGetters() {
    final XLRange xlRange = XLRange.of(1, 100, 2, 3);
    final XLSheetId sheetId = XLSheetId.of(1234);
    final XLMultiReference multiRef = XLMultiReference.of(sheetId, xlRange);
    Assert.assertEquals(multiRef.getSingleRange(), xlRange);
    Assert.assertEquals(multiRef.getSheetId(), sheetId);
    Assert.assertEquals(multiRef.getRanges().size(), 1);
  }

  @Test
  public void testSingleListConstructionAndGetters() {
    final XLRange xlRange = XLRange.of(1, 100, 2, 3);
    final XLSheetId sheetId = XLSheetId.of(1234);
    final XLMultiReference multiRef = XLMultiReference.of(sheetId, Collections.singletonList(xlRange));
    Assert.assertEquals(multiRef.getSingleRange(), xlRange);
    Assert.assertEquals(multiRef.getSheetId(), sheetId);
    Assert.assertEquals(multiRef.getRanges().size(), 1);
  }

  @Test
  public void testMultiListConstructionAndGetters() {
    final XLRange xlRange1 = XLRange.of(1, 100, 2, 3);
    final XLRange xlRange2 = XLRange.of(2, 200, 6, 9);
    final XLSheetId sheetId = XLSheetId.of(1234);
    final List<XLRange> ranges = new ArrayList<>();
    ranges.add(xlRange1);
    ranges.add(xlRange2);
    final XLMultiReference multiRef = XLMultiReference.of(sheetId, ranges);
    Assert.assertEquals(multiRef.getSingleRange(), xlRange1);
    Assert.assertEquals(multiRef.getRanges().size(), 2);
    Assert.assertEquals(multiRef.getRanges(), ranges);
    Assert.assertEquals(multiRef.getSheetId(), sheetId);
  }

  @Test
  public void testEqualsAndHashCode() {
    final XLRange range = XLRange.of(0, 15, 0, 15);
    final XLRange range_1 = XLRange.of(1, 15, 0, 15);

    final XLSheetId sheetId = XLSheetId.of(5678);
    final XLSheetId sheetId_1 = XLSheetId.of(123);
    final List<XLRange> ranges1 = new ArrayList<XLRange>();
    ranges1.add(range);
    ranges1.add(range_1);
    final XLMultiReference multiRef = XLMultiReference.of(sheetId, range);
    final XLMultiReference multiRefs1 = XLMultiReference.of(sheetId, ranges1);

    final XLRange[] ranges2 = new XLRange[] { range, range_1 };
    final XLMultiReference multiRefs2 = XLMultiReference.of(sheetId, ranges2);

    final XLMultiReference multiRefs3 = XLMultiReference.of(sheetId, range, range_1);

    Assert.assertEquals(multiRefs1, multiRefs1);
    Assert.assertEquals(multiRefs2, multiRefs2);
    Assert.assertEquals(multiRefs3, multiRefs3);

    Assert.assertEquals(multiRefs1, multiRefs2);
    Assert.assertEquals(multiRefs2, multiRefs3);
    Assert.assertEquals(multiRefs3, multiRefs1);

    Assert.assertNotEquals(null, multiRefs1);
    Assert.assertNotEquals(null, multiRefs2);
    Assert.assertNotEquals(null, multiRefs3);

    Assert.assertNotEquals("Hello", multiRefs1);
    Assert.assertNotEquals("Hello", multiRefs2);
    Assert.assertNotEquals("Hello", multiRefs3);

    Assert.assertEquals(multiRefs1.hashCode(), multiRefs2.hashCode());
    Assert.assertEquals(multiRefs2.hashCode(), multiRefs3.hashCode());
    Assert.assertEquals(multiRefs3.hashCode(), multiRefs1.hashCode());

    final XLMultiReference multiRefs4 = XLMultiReference.of(sheetId, range_1, range);
    Assert.assertNotEquals(multiRefs3, multiRefs4);
    Assert.assertNotEquals(multiRefs4, multiRef);
    final XLMultiReference multiRefs5 = XLMultiReference.of(sheetId_1, range);
    Assert.assertNotEquals(multiRefs5, multiRef);
  }

  private static final String SINGLE_ROW = "XLMultiReference[sheetId=9999, range=XLRange[Single Row row=100, columns=3 to 5]]";
  private static final String SINGLE_COL = "XLMultiReference[sheetId=9999, range=XLRange[Single Column rows=100 to 104, column=3]]";
  private static final String SINGLE_CELL = "XLMultiReference[sheetId=9999, range=XLRange[Single Cell row=100, column=3]]";
  private static final String FULL_RANGE = "XLMultiReference[sheetId=9999, range=XLRange[Range rows=10 to 45, columns=0 to 30]]";

  @Test
  public void testToString() {
    final XLSheetId sheetId = XLSheetId.of(9999);
    final XLRange singleRow = XLRange.of(100, 100, 3, 5);
    final XLMultiReference singleRowRef = XLMultiReference.of(sheetId, singleRow);
    Assert.assertEquals(singleRowRef.toString(), SINGLE_ROW);
    final XLRange singleCol = XLRange.of(100, 104, 3, 3);
    final XLMultiReference singleColRef = XLMultiReference.of(sheetId, singleCol);
    Assert.assertEquals(singleColRef.toString(), SINGLE_COL);
    final XLRange singleCell = XLRange.of(100, 100, 3, 3);
    final XLMultiReference singleCellRef = XLMultiReference.of(sheetId, singleCell);
    Assert.assertEquals(singleCellRef.toString(), SINGLE_CELL);
    final XLRange singleCell2 = XLRange.ofCell(100, 3);
    final XLMultiReference singleCell2Ref = XLMultiReference.of(sheetId, singleCell2);
    Assert.assertEquals(singleCell2Ref.toString(), SINGLE_CELL);
    final XLRange fullRange = XLRange.of(10, 45, 0, 30);
    final XLMultiReference fullRangeRef = XLMultiReference.of(sheetId, fullRange);
    Assert.assertEquals(fullRangeRef.toString(), FULL_RANGE);
  }
}
