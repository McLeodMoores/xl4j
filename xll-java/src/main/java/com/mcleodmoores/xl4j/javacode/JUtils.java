/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.javacode;

import com.mcleodmoores.xl4j.XLParameter;
import com.mcleodmoores.xl4j.XLFunction;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Java function utilities.
 */
public final class JUtils {

  /**
   * Orders two calculations.
   *
   * @param before
   *          the cell to calculate before
   * @param after
   *          the cell to calculate after
   * @return the result.
   */
  @XLFunction(name = "After", category = "Java",
      description = "Order two calculations, allowing sequences of operations")
  public static XLValue after(
      @XLParameter(name = "before", description = "cell to calculate before") final XLValue before,
      @XLParameter(name = "after", description = "value to calculate after") final XLValue after) {
    return after;
  }

  private JUtils() {
  }
}
