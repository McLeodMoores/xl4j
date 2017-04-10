/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

/**
 * Utility class to output byte arrays as hex strings.
 */
public final class HexUtils {
  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

  private HexUtils() {
  }

  /**
   * Convert an array of bytes to the equivalent string of Hex digits. Based on code from Stack Overflow.
   * 
   * @param bytes
   *          the byte array
   * @return a String containing a sequence of hexadecimal digits with no padding
   */
  public static String bytesToHex(final byte[] bytes) {
    ArgumentChecker.notNull(bytes, "bytes");
    final char[] hexChars = new char[bytes.length * 2];
    for (int i = 0; i < bytes.length; i++) {
      final int v = bytes[i] & 0xFF;
      hexChars[i * 2] = HEX_ARRAY[v >>> 4];
      hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }

  /**
   * Convert an array of bytes to the equivalent string of Hex digits, with each byte padded by a space: e.g. 6A7434FF would be returned as
   * "6A 74 34 FF". Based on code from Stack Overflow.
   * 
   * @param bytes
   *          the byte array, not null
   * @return a String containing a sequence of hexadecimal digits with no padding
   */
  public static String bytesToPaddedHex(final byte[] bytes) {
    ArgumentChecker.notNull(bytes, "bytes");
    final char[] hexChars = new char[bytes.length * 2 + Math.max(0, bytes.length - 1)];
    for (int i = 0; i < bytes.length; i++) {
      final int v = bytes[i] & 0xFF;
      hexChars[i * 3] = HEX_ARRAY[v >>> 4];
      hexChars[i * 3 + 1] = HEX_ARRAY[v & 0x0F];
      if (i < bytes.length - 1) { // don't pad last pair
        hexChars[i * 3 + 2] = ' ';
      }
    }
    return new String(hexChars);
  }

  /**
   * Convert an array of bytes to the equivalent string of Hex digits, with each byte padded by a space: e.g. 6A7434FF would be returned as
   * "6A 74 34 FF". This variant can limit the number of bytes rendered to make e.g. toString() calls easier to read. Based on code from
   * Stack Overflow.
   * 
   * @param bytes
   *          the byte array, not null
   * @param maxBytes
   *          the maximum number of bytes (positive) to render, okay if larger than bytes.length
   * @return a String containing a sequence of hexadecimal digits with no padding
   */
  public static String bytesToTruncatedPaddedHex(final byte[] bytes, final int maxBytes) {
    ArgumentChecker.notNull(bytes, "bytes");
    ArgumentChecker.notNegative(maxBytes, "maxBytes");
    final int size = Math.min(maxBytes, bytes.length);
    final char[] hexChars = new char[size * 2 + Math.max(0, size - 1)];
    for (int i = 0; i < size; i++) {
      final int v = bytes[i] & 0xFF;
      hexChars[i * 3] = HEX_ARRAY[v >>> 4];
      hexChars[i * 3 + 1] = HEX_ARRAY[v & 0x0F];
      if (i < size - 1) { // don't pad last pair
        hexChars[i * 3 + 2] = ' ';
      }
    }
    return new String(hexChars);
  }

  /**
   * Multi-line hex/character dump - similar to a hex editor layout.
   * 
   * @param bytes
   *          the buffer to dump
   * @param bytesPerLine
   *          the number of bytes to display on each line
   * @return multi-line String containing hex-editor style dump
   */
  public static String bytesToMultiLineDump(final byte[] bytes, final int bytesPerLine) {
    ArgumentChecker.notNull(bytes, "bytes");
    ArgumentChecker.notNegative(bytesPerLine, "bytesPerLine");
    final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
    final byte[] line = new byte[bytesPerLine];
    final StringBuffer sb = new StringBuffer();
    while (is.available() > 0) {
      final int bytesRead = is.read(line, 0, bytesPerLine);
      if (bytesRead < bytesPerLine) {
        final byte[] shortLine = new byte[bytesRead];
        System.arraycopy(line, 0, shortLine, 0, bytesRead);
        final String lineStr = bytesToPaddedHex(shortLine);
        sb.append(lineStr);
        final int remainingChars = (bytesPerLine - bytesRead) * 3;
        sb.append(new String(new char[remainingChars]).replace("\0", " ")); // repeated string of spaces
        sb.append("  ");
        // the bytes as ASCII characters, changed to ASCII because of testing platform differences
        sb.append(new String(shortLine, Charset.forName("US-ASCII")).replaceAll("\\p{C}", "."));
      } else {
        final String lineStr = bytesToPaddedHex(line);
        sb.append(lineStr);
        sb.append("  ");
        // the bytes as ASCII characters, changed to ASCII because of testing platform differences
        sb.append(new String(line, Charset.forName("US-ASCII")).replaceAll("\\p{C}", "."));
      }
      sb.append("\n");
    }
    return sb.toString();
  }
}
