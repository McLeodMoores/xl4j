/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import com.mcleodmoores.xl4j.values.XLValue;

/**
 *
 */
public class InvokerTestHelper {
  public static final int FIELD = -100;

  public InvokerTestHelper() {
  }

  public InvokerTestHelper(final int i) {
  }

  public InvokerTestHelper(final int i, final int j) {
  }

  public InvokerTestHelper(final int... ints) {
  }

  public InvokerTestHelper(final int i, final int... ints) {
  }

  public static void voidMethod() {
    return;
  }

  public static boolean noArgsMethod() {
    return false;
  }

  public static boolean singleArgMethod(final int i) {
    return i > 0;
  }

  public static boolean multiArgsMethod(final int i, final int j) {
    return i * j > 0;
  }

  public static boolean arrayArgsMethod(final int[] is, final int[] js) {
    if (is == null || js == null) {
      return false;
    }
    for (final int i : is) {
      for (final int j : js) {
        if (i * j < 0) {
          return false;
        }
      }
    }
    return true;
  }

  public static boolean varArgsMethod1(final int... ints) {
    for (final int i : ints) {
      if (i < 0) {
        return false;
      }
    }
    return true;
  }

  public static boolean varArgsMethod2(final int i, final int... ints) {
    for (final int j : ints) {
      if (i * j < 0) {
        return false;
      }
    }
    return true;
  }

  public static boolean passthroughMethod1(final XLValue xlValue) {
    if (xlValue == null) {
      return false;
    }
    return true;
  }

}
