/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLBigData;
import com.mcleodmoores.excel4j.values.XLBoolean;
import com.mcleodmoores.excel4j.values.XLError;
import com.mcleodmoores.excel4j.values.XLInteger;
import com.mcleodmoores.excel4j.values.XLLocalReference;
import com.mcleodmoores.excel4j.values.XLMissing;
import com.mcleodmoores.excel4j.values.XLMultiReference;
import com.mcleodmoores.excel4j.values.XLNil;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLSheetId;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;
import com.mcleodmoores.excel4j.values.XLValueRange;
import com.mcleodmoores.excel4j.values.XLRange;

/**
 * Unit tests for XLValueRange.
 */
public final class XLValueRangeTests {
  // CHECKSTYLE:OFF
	
	private static final XLValue[][] SINGLE = new XLValue[][] { { XLBigData.of("Hello World") } };
	private static final XLValue[][] SINGLE_1 = new XLValue[][] { { XLBigData.of("Hello World") } };
	
	private static final XLValue[][] MULTI = new XLValue[][] { 
		{ XLBoolean.of(true), XLError.NA, XLInteger.of(65536) },
		{ XLLocalReference.of(XLRange.of(1, 1, 3, 5)), XLMissing.getInstance(), XLNil.getInstance() }, 
		{ XLMultiReference.of(XLSheetId.of(1234), XLRange.of(1, 2, 3, 4), XLRange.ofCell(4, 5)), XLNumber.of(43.234) },
		{ XLString.of("Hello World"), XLError.Null, XLError.Ref }
	};
	private static final XLValue[][] MULTI_1 = new XLValue[][] { 
		{ XLBoolean.of(true), XLError.NA, XLInteger.of(65536) },
		{ XLLocalReference.of(XLRange.of(1, 1, 3, 5)), XLMissing.getInstance(), XLNil.getInstance() }, 
		{ XLMultiReference.of(XLSheetId.of(1234), XLRange.of(1, 2, 3, 4), XLRange.ofCell(4, 5)), XLNumber.of(43.234) },
		{ XLString.of("Hello World"), XLError.Null, XLError.Ref }
	};	
	private static final XLValue[][] MULTI_2 = new XLValue[][] { // Note that the XLRange.ofCell() is changed from above.
		{ XLBoolean.of(true), XLError.NA, XLInteger.of(65536) },
		{ XLLocalReference.of(XLRange.of(1, 1, 3, 5)), XLMissing.getInstance(), XLNil.getInstance() }, 
		{ XLMultiReference.of(XLSheetId.of(1234), XLRange.of(1, 2, 3, 4), XLRange.ofCell(5, 5)), XLNumber.of(43.234) },
		{ XLString.of("Hello World"), XLError.Null, XLError.Ref }
	};

  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNull() {
    XLValueRange.of(null);
  }
  
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testEmpty() {
    XLValueRange.of(new XLValue[][] {});
  }
  
  @Test // (expectedExceptions = Excel4JRuntimeException.class) - this perhaps should be caught, enable this if we decide to check for it.
  public void testEmptyNested() {
    XLValueRange.of(new XLValue[][] {{}}); // single empty array: [1][0]
  }
  
  @Test
  public void testConstructionAndGetterSingle() {
    XLValueRange localRef = XLValueRange.of(SINGLE);
    Assert.assertEquals(localRef.getValueRange(), SINGLE);
  }  
  
  @Test
  public void testConstructionAndGetterMulti() {
    XLValueRange localRef = XLValueRange.of(MULTI);
    Assert.assertEquals(localRef.getValueRange(), MULTI);
  }  
  
  @Test
  public void testEqualsAndHashCode() {
    XLValueRange single = XLValueRange.of(SINGLE);
    XLValueRange single_1 = XLValueRange.of(SINGLE_1);
    XLValueRange multi = XLValueRange.of(MULTI);
    XLValueRange multi_1 = XLValueRange.of(MULTI_1);
    XLValueRange multi_2 = XLValueRange.of(MULTI_2);
    Assert.assertEquals(single, single);
    Assert.assertNotEquals(single, null);
    Assert.assertNotEquals(single, "Hello");
    Assert.assertEquals(single, single_1);
    Assert.assertNotEquals(multi, null);
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

  private static final String SINGLE_TO_STRING = "XLValueRange[valueRange=[[XLBigData[len=18, buffer=[AC ED 00 05 74 00 0B 48 65 6C 6C 6F 20 57 6F 72 6C 64]]]]]";
  private static final String MULTI_TO_STRING = "XLValueRange[valueRange=[[XLBoolean[value=true], NA, XLInteger[value=65536]], " + 
                                                "[XLLocalReference[range=XLRange[Single Row row=1, columns=3 to 5]], XLMissing, XLNil], " + 
  		                                          "[XLMultiReference[sheetId=1234, ranges=[XLRange[Range rows=1 to 2, columns=3 to 4], " + 
                                                "XLRange[Single Cell row=4, column=5]]], XLNumber[value=43.234]], [XLString[value=Hello World], Null, Ref]]]";
  
  @Test
  public void testToString() {
    XLValueRange single = XLValueRange.of(SINGLE);
    Assert.assertEquals(single.toString(), SINGLE_TO_STRING);
    XLValueRange multi = XLValueRange.of(MULTI);
    Assert.assertEquals(multi.toString(), MULTI_TO_STRING);
  }
}
