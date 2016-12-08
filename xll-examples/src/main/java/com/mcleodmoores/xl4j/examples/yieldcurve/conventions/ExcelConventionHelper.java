/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.yieldcurve.conventions;

import com.mcleodmoores.xl4j.XLFunction;

/**
 *
 */
public final class ExcelConventionHelper {

  /**
   * Gets an overnight index convention builder.
   *
   * @return  a convention builder
   */
  @XLFunction(
      name = "OvernightIndexConventionBuilder",
      category = "Convention",
      description = "A builder for overnight indices")
  public static OvernightIndexConventionBuilder overnightIndexConventionBuilder() {
    return OvernightIndexConventionBuilder.builder();
  }

  /**
   * Gets an ibor-type index convention builder.
   *
   * @return  a convention builder
   */
  @XLFunction(
      name = "IborTypeIndexConventionBuilder",
      category = "Convention",
      description = "A builder for ibor-type indices")
  public static IborTypeIndexConventionBuilder builder() {
    return new IborTypeIndexConventionBuilder();
  }

  /**
   * Private constructor.
   */
  private ExcelConventionHelper() {
  }
}
