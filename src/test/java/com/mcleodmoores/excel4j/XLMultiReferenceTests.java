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
  // CHECKSTYLE:OFF

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
  	XLRange xlRange = XLRange.of(1, 100, 2, 3);
  	XLSheetId sheetId = XLSheetId.of(1234);
    XLMultiReference multiRef = XLMultiReference.of(sheetId, xlRange);
    Assert.assertEquals(multiRef.getSingleRange(), xlRange);
    Assert.assertEquals(multiRef.getSheetId(), sheetId);
    Assert.assertEquals(multiRef.getRanges().size(), 1);
  }
  
  @Test
  public void testSingleListConstructionAndGetters() {
  	XLRange xlRange = XLRange.of(1, 100, 2, 3);
  	XLSheetId sheetId = XLSheetId.of(1234);
    XLMultiReference multiRef = XLMultiReference.of(sheetId, Collections.singletonList(xlRange));
    Assert.assertEquals(multiRef.getSingleRange(), xlRange);
    Assert.assertEquals(multiRef.getSheetId(), sheetId);
    Assert.assertEquals(multiRef.getRanges().size(), 1);
  }
  
  @Test
  public void testMultiListConstructionAndGetters() {
  	XLRange xlRange1 = XLRange.of(1, 100, 2, 3);
  	XLRange xlRange2 = XLRange.of(2, 200, 6, 9);
  	XLSheetId sheetId = XLSheetId.of(1234);
  	List<XLRange> ranges = new ArrayList<>();
  	ranges.add(xlRange1);
  	ranges.add(xlRange2);
    XLMultiReference multiRef = XLMultiReference.of(sheetId, ranges);
    Assert.assertEquals(multiRef.getSingleRange(), xlRange1);
    Assert.assertEquals(multiRef.getRanges().size(), 2);
    Assert.assertEquals(multiRef.getRanges(), ranges);
    Assert.assertEquals(multiRef.getSheetId(), sheetId);
  }
  
  @Test
  public void testEqualsAndHashCode() {
    XLRange range = XLRange.of(0, 15, 0, 15);
    XLRange range_1 = XLRange.of(1, 15, 0, 15);
    
    XLSheetId sheetId = XLSheetId.of(5678);
    
    List<XLRange> ranges1 = new ArrayList<XLRange>();
    ranges1.add(range);
    ranges1.add(range_1);
    XLMultiReference multiRef = XLMultiReference.of(sheetId, range);
    XLMultiReference multiRefs1 = XLMultiReference.of(sheetId, ranges1);
    
    XLRange[] ranges2 = new XLRange[] { range, range_1 };
    XLMultiReference multiRefs2 = XLMultiReference.of(sheetId, ranges2);
    
    XLMultiReference multiRefs3 = XLMultiReference.of(sheetId, range, range_1);
    
    Assert.assertEquals(multiRefs1, multiRefs1);
    Assert.assertEquals(multiRefs2, multiRefs2);
    Assert.assertEquals(multiRefs3, multiRefs3);

    Assert.assertEquals(multiRefs1, multiRefs2);
    Assert.assertEquals(multiRefs2, multiRefs3);
    Assert.assertEquals(multiRefs3, multiRefs1);
    
    Assert.assertNotEquals(multiRefs1, null);
    Assert.assertNotEquals(multiRefs2, null);
    Assert.assertNotEquals(multiRefs3, null);
    
    Assert.assertEquals(multiRefs1.hashCode(), multiRefs2.hashCode());
    Assert.assertEquals(multiRefs2.hashCode(), multiRefs3.hashCode());
    Assert.assertEquals(multiRefs3.hashCode(), multiRefs1.hashCode());
    
    XLMultiReference multiRefs4 = XLMultiReference.of(sheetId, range_1, range);
    Assert.assertNotEquals(multiRefs3, multiRefs4);
    Assert.assertNotEquals(multiRefs4, multiRef);
  }

  private static final String SINGLE_ROW = "XLMultiReference[sheetId=9999, range=XLRange[Single Row row=100, columns=3 to 5]]";
  private static final String SINGLE_COL = "XLMultiReference[sheetId=9999, range=XLRange[Single Column rows=100 to 104, column=3]]";
  private static final String SINGLE_CELL = "XLMultiReference[sheetId=9999, range=XLRange[Single Cell row=100, column=3]]";
  private static final String FULL_RANGE = "XLMultiReference[sheetId=9999, range=XLRange[Range rows=10 to 45, columns=0 to 30]]";
  
  @Test
  public void testToString() {
  	XLSheetId sheetId = XLSheetId.of(9999);
    XLRange singleRow = XLRange.of(100, 100, 3, 5);
    XLMultiReference singleRowRef = XLMultiReference.of(sheetId, singleRow);
    Assert.assertEquals(singleRowRef.toString(), SINGLE_ROW);
    XLRange singleCol = XLRange.of(100, 104, 3, 3);
    XLMultiReference singleColRef = XLMultiReference.of(sheetId, singleCol);
    Assert.assertEquals(singleColRef.toString(), SINGLE_COL);
    XLRange singleCell = XLRange.of(100, 100, 3, 3);
    XLMultiReference singleCellRef = XLMultiReference.of(sheetId, singleCell);
    Assert.assertEquals(singleCellRef.toString(), SINGLE_CELL);
    XLRange singleCell2 = XLRange.ofCell(100, 3);
    XLMultiReference singleCell2Ref = XLMultiReference.of(sheetId, singleCell2);
    Assert.assertEquals(singleCell2Ref.toString(), SINGLE_CELL);
    XLRange fullRange = XLRange.of(10, 45, 0, 30);
    XLMultiReference fullRangeRef = XLMultiReference.of(sheetId, fullRange);
    Assert.assertEquals(fullRangeRef.toString(), FULL_RANGE);
  }
}
