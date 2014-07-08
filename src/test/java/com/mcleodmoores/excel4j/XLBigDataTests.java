/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import java.io.Serializable;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.util.SerializationUtils;
import com.mcleodmoores.excel4j.values.XLBigData;

/**
 * Unit tests for XLBigData.
 */
public final class XLBigDataTests {
  // CHECKSTYLE:OFF
  private static final Logger s_logger = LoggerFactory.getLogger(XLBigDataTests.class);
  private static final Excel EXCEL = ExcelFactory.getMockInstance();
  
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testXLBigDataNull() {
    XLBigData.of(null);
  }
  
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testXLBigDataSerializableNull() {
    XLBigData.of((Serializable) null);
  }
  
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testXLBigDataExcelCallbackNull() {
    XLBigData.of((Excel) null, 0, 1);
  }
  
  @Test
  public void testXLBigDataEmpty() {
    byte[] testData = new byte[0];
    XLBigData bigData = XLBigData.of(testData);
    Assert.assertEquals(bigData.getBuffer(), testData);
  }
  
  @Test
  public void testXLBigDataNonEmpty() {
    byte[] testData = new byte[256];
    SecureRandom random = new SecureRandom();
    random.nextBytes(testData);
    XLBigData bigData = XLBigData.of(testData);
    Assert.assertEquals(bigData.getBuffer(), testData);
  }
  
  @Test
  public void testXLBigDataManualSerialization() {
    final String testString = "TEST STRING";
    byte[] testData = SerializationUtils.serialize(testString);
    XLBigData bigData = XLBigData.of(testData);
    Assert.assertEquals(bigData.getBuffer(), testData);
    Assert.assertEquals(bigData.getValue(), testString);
  }
  
  @Test
  public void testXLBigDataAutomaticSerialization() {
    final String testString = "TEST STRING";
    XLBigData bigData = XLBigData.of(testString);
    Assert.assertEquals(bigData.getValue(), testString);
  }
  
  @Test
  public void testXLBigDataHandleSize() {
    XLBigData bigData = XLBigData.of(EXCEL, 1, 0);
    Assert.assertEquals(bigData.getBuffer(), new byte[0]); // this will need changing when Excel interface actually does something!
  }
  
  @Test
  public void testEqualsAndHashCode() {
    XLBigData bigData = XLBigData.of(EXCEL, 1, 0);
    byte[] testData = new byte[0];
    XLBigData bigData2 = XLBigData.of(testData);
    Assert.assertEquals(bigData, bigData);
    Assert.assertEquals(bigData2, bigData); // this will need changing when Excel interface actually does something! 
    Assert.assertNotEquals(bigData, null);
    Assert.assertNotEquals(bigData, testData);
    Assert.assertEquals(bigData2.hashCode(), bigData.hashCode()); // this will need changing when Excel interface actually does something!
    
    final String testString = "TEST STRING";
    XLBigData bigData3 = XLBigData.of(testString);
    Assert.assertNotEquals(null, bigData3);
    Assert.assertNotEquals(testString, bigData3);
    Assert.assertNotEquals(bigData3, bigData2);
    XLBigData bigData4 = XLBigData.of("TEST STRING");
    Assert.assertEquals(bigData4, bigData3);
    Assert.assertEquals(bigData4.hashCode(), bigData4.hashCode());

    Assert.assertEquals(bigData4.hashCode(), bigData3.hashCode());
  }
  
  @Test
  public void testXLBigDataToString() {
    byte[] testData = new byte[256];
    SecureRandom random = new SecureRandom();
    random.nextBytes(testData);
    XLBigData bigData = XLBigData.of(testData);
    String toStr = bigData.toString();
    Assert.assertEquals(toStr.length(), 127);
  }
  
  @Test
  public void testXLBigDataToStringEmpty() {
    byte[] testData = new byte[0];
    XLBigData bigData = XLBigData.of(testData);
    String toStr = bigData.toString();
    s_logger.info("toString was " + toStr);
    Assert.assertEquals(toStr.length(), 27);
  }
}
