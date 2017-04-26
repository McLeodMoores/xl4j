/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1;

import java.io.Serializable;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.values.XLBigData;
import com.mcleodmoores.xl4j.v1.util.SerializationUtils;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for XLBigData.
 */
public class XLBigDataTests {
  private static final Logger LOGGER = LoggerFactory.getLogger(XLBigDataTests.class);
  private static final Excel EXCEL = ExcelFactory.getInstance();

  /**
   * Tests that the input cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testXLBigDataNull() {
    XLBigData.of(null);
  }

  /**
   * Tests that the input cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testXLBigDataSerializableNull() {
    XLBigData.of((Serializable) null);
  }

  /**
   * Tests that the Excel callback cannot be null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testXLBigDataExcelCallbackNull() {
    XLBigData.of((Excel) null, 0, 1);
  }

  /**
   *
   */
  @Test
  public void testXLBigDataEmpty() {
    final byte[] testData = new byte[0];
    final XLBigData bigData = XLBigData.of(testData);
    Assert.assertEquals(bigData.getBuffer(), testData);
  }

  /**
   *
   */
  @Test
  public void testXLBigDataNonEmpty() {
    final byte[] testData = new byte[256];
    final SecureRandom random = new SecureRandom();
    random.nextBytes(testData);
    final XLBigData bigData = XLBigData.of(testData);
    Assert.assertEquals(bigData.getBuffer(), testData);
  }

  /**
   *
   */
  @Test
  public void testXLBigDataManualSerialization() {
    final String testString = "TEST STRING";
    final byte[] testData = SerializationUtils.serialize(testString);
    final XLBigData bigData = XLBigData.of(testData);
    Assert.assertEquals(bigData.getBuffer(), testData);
    Assert.assertEquals(bigData.getValue(), testString);
  }

  /**
   *
   */
  @Test
  public void testXLBigDataAutomaticSerialization() {
    final String testString = "TEST STRING";
    final XLBigData bigData = XLBigData.of(testString);
    Assert.assertEquals(bigData.getValue(), testString);
  }

  /**
   *
   */
  @Test
  public void testXLBigDataHandleSize() {
    final XLBigData bigData = XLBigData.of(EXCEL, 1, 0);
    Assert.assertEquals(bigData.getBuffer(), new byte[0]); // this will need changing when Excel interface actually does something!
  }

  /**
   *
   */
  @Test
  public void testEqualsAndHashCode() {
    final XLBigData bigData = XLBigData.of(EXCEL, 1, 0);
    final byte[] testData = new byte[0];
    final XLBigData bigData2 = XLBigData.of(testData);
    Assert.assertEquals(bigData, bigData);
    Assert.assertEquals(bigData2, bigData); // this will need changing when Excel interface actually does something!
    Assert.assertNotEquals(bigData, null);
    Assert.assertNotEquals(bigData, testData);
    Assert.assertEquals(bigData2.hashCode(), bigData.hashCode()); // this will need changing when Excel interface actually does something!

    final String testString = "TEST STRING";
    final XLBigData bigData3 = XLBigData.of(testString);
    Assert.assertNotEquals(null, bigData3);
    Assert.assertNotEquals(testString, bigData3);
    Assert.assertNotEquals(bigData3, bigData2);
    final XLBigData bigData4 = XLBigData.of("TEST STRING");
    Assert.assertEquals(bigData4, bigData3);
    Assert.assertEquals(bigData4.hashCode(), bigData4.hashCode());

    Assert.assertEquals(bigData4.hashCode(), bigData3.hashCode());
  }

  /**
   *
   */
  @Test
  public void testXLBigDataToString() {
    final byte[] testData = new byte[256];
    final SecureRandom random = new SecureRandom();
    random.nextBytes(testData);
    final XLBigData bigData = XLBigData.of(testData);
    final String toStr = bigData.toString();
    Assert.assertEquals(toStr.length(), 127);
  }

  /**
   *
   */
  @Test
  public void testXLBigDataToStringEmpty() {
    final byte[] testData = new byte[0];
    final XLBigData bigData = XLBigData.of(testData);
    final String toStr = bigData.toString();
    LOGGER.info("toString was " + toStr);
    Assert.assertEquals(toStr.length(), 27);
  }
}
