/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.util;

/**
 * Checked exception used when an array that is required to be of a certain size in one dimension,
 * does not meet that requirement.  Thrown by {@link ArrayUtils#transposeIfNeeded}.
 */
public class ArrayWrongSizeException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * Constructor when exception is not caused by an underlying exception.
   *
   * @param message
   *          a message describing the exception, not null
   */
  public ArrayWrongSizeException(final String message) {
    super(message);
  }
}

