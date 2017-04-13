package com.mcleodmoores.xl4j.util;

/**
 * Checked exception used when an array that is required to be of a certain size in one dimension,
 * does not meet that requirement.  Thrown by ArrayUtils.transposeIfNeeded().
 */
public class ArrayWrongSizeException extends Exception {
  public ArrayWrongSizeException(String arg0) {
    super(arg0);
  }
}

