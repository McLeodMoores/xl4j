/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;
import com.mcleodmoores.xl4j.v1.util.HexUtils;

/**
 * Unit Tests for HexUtils.
 */
public class HexUtilsTests {
  private static final byte[] BYTES = new byte[] { 0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0 };
  private static final String EXPECTED = "123456789ABCDEF0";
  private static final String EXPECTED_PADDED = "12 34 56 78 9A BC DE F0";
  private static final String EXPECTED_PADDED_FIVE = "12 34 56 78 9A";
  private static final String EXPECTED_PADDED_ONE = "12";
  private static final String EXPECTED_PADDED_ZERO = "";
  private static final byte[] BYTES_1 = new byte[] { (byte) 0xFF };
  private static final String EXPECTED_1 = "FF";
  private static final String EXPECTED_PADDED_1 = "FF";
  private static final String EXPECTED_PADDED_FIVE_1 = "FF";
  private static final String EXPECTED_PADDED_ONE_1 = "FF";
  private static final String EXPECTED_PADDED_ZERO_1 = "";

  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testBytesToHexNull() {
    HexUtils.bytesToHex(null);
  }

  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testBytesToPaddedHexNull() {
    HexUtils.bytesToPaddedHex(null);
  }

  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testBytesToTruncatedPaddedHexNull() {
    HexUtils.bytesToTruncatedPaddedHex(null, 20);
  }

  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testBytesToTruncatedPaddedHexNegativeMaxBytes() {
    HexUtils.bytesToTruncatedPaddedHex(new byte[30], -20);
  }

  @Test
  public void testBytesToHexEmpty() {
    Assert.assertEquals(HexUtils.bytesToHex(new byte[0]), "");
  }

  @Test
  public void testBytesToPaddedHexEmpty() {
    Assert.assertEquals(HexUtils.bytesToPaddedHex(new byte[0]), "");
  }

  @Test
  public void testBytesToTruncatedPaddedHexEmpty() {
    Assert.assertEquals(HexUtils.bytesToTruncatedPaddedHex(new byte[0], 16), "");
    Assert.assertEquals(HexUtils.bytesToTruncatedPaddedHex(new byte[0], 0), "");
  }

  @Test
  public void testBytesToHexExpected() {
    Assert.assertEquals(HexUtils.bytesToHex(BYTES), EXPECTED);
    Assert.assertEquals(HexUtils.bytesToPaddedHex(BYTES), EXPECTED_PADDED);
    Assert.assertEquals(HexUtils.bytesToTruncatedPaddedHex(BYTES, 5), EXPECTED_PADDED_FIVE);
    Assert.assertEquals(HexUtils.bytesToTruncatedPaddedHex(BYTES, 1), EXPECTED_PADDED_ONE);
    Assert.assertEquals(HexUtils.bytesToTruncatedPaddedHex(BYTES, 0), EXPECTED_PADDED_ZERO);
  }

  @Test
  public void testBytesToHexExpectedSingle() {
    Assert.assertEquals(HexUtils.bytesToHex(BYTES_1), EXPECTED_1);
    Assert.assertEquals(HexUtils.bytesToPaddedHex(BYTES_1), EXPECTED_PADDED_1);
    Assert.assertEquals(HexUtils.bytesToTruncatedPaddedHex(BYTES_1, 5), EXPECTED_PADDED_FIVE_1);
    Assert.assertEquals(HexUtils.bytesToTruncatedPaddedHex(BYTES_1, 1), EXPECTED_PADDED_ONE_1);
    Assert.assertEquals(HexUtils.bytesToTruncatedPaddedHex(BYTES_1, 0), EXPECTED_PADDED_ZERO_1);
  }

  private static final String EXPECTED_MULTI_LINE_SIX =
      "12 34 56 78 9A BC  .4Vx��\n"
          + "DE F0              ��\n";
  private static final String EXPECTED_MULTI_LINE_FOUR =
      "12 34 56 78  .4Vx\n"
          + "9A BC DE F0  ����\n";
  private static final String EXPECTED_MULTI_LINE_FOUR_1 =
      "FF           �\n";
  private static final String EXPECTED_MULTI_LINE_FOUR_2 = "";

  @Test
  public void testBytesToMultiLineDump() {
    Assert.assertEquals(HexUtils.bytesToMultiLineDump(BYTES, 6), EXPECTED_MULTI_LINE_SIX);
    Assert.assertEquals(HexUtils.bytesToMultiLineDump(BYTES, 4), EXPECTED_MULTI_LINE_FOUR);
    Assert.assertEquals(HexUtils.bytesToMultiLineDump(BYTES_1, 4), EXPECTED_MULTI_LINE_FOUR_1);
    Assert.assertEquals(HexUtils.bytesToMultiLineDump(new byte[0], 4), EXPECTED_MULTI_LINE_FOUR_2);

  }
}
