/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import java.io.Serializable;
import java.security.SecureRandom;

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
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testXLBigDataNull() {
    XLBigData.of(null);
  }
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testXLBigDataSerializableNull() {
    XLBigData.of((Serializable) null);
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
    XLBigData bigData = XLBigData.of(1, 0);
    Assert.assertEquals(bigData.getBuffer(), new byte[0]); // this will need changing when Excel interface actually does something!
  }
  
  @Test
  public void testEqualsAndHashCode() {
    XLBigData bigData = XLBigData.of(1, 0);
    byte[] testData = new byte[0];
    XLBigData bigData2 = XLBigData.of(testData);
    // The following is NotEquals because the handle of (1) is tested.  Should this be changed?
    Assert.assertNotEquals(bigData2, bigData); // this will need changing when Excel interface actually does something! 
    Assert.assertNotEquals(bigData, null);
    Assert.assertNotEquals(bigData, testData);
    // The following is NotEquals because the handle of (1) is tested.  Should this be changed?
    Assert.assertNotEquals(bigData2.hashCode(), bigData.hashCode()); // this will need changing when Excel interface actually does something!
    
    final String testString = "TEST STRING";
    XLBigData bigData3 = XLBigData.of(testString);
    Assert.assertNotEquals(bigData3, null);
    Assert.assertNotEquals(bigData3, testString);
    Assert.assertNotEquals(bigData3, bigData2);
    XLBigData bigData4 = XLBigData.of("TEST STRING");
    Assert.assertEquals(bigData4, bigData3);
    Assert.assertEquals(bigData4.hashCode(), bigData4.hashCode());
  }
}
