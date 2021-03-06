/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.annotations;

/**
 * Enum representing how function results should be converted before returning.
 */
public enum TypeConversionMode {
  /**
   * Results should be reduced to the simplest possible type, e.g. XLString, XLNumber, XLBoolean etc.
   */
  SIMPLEST_RESULT,
  /**
   * Results should be returned as object handles. This means you can return e.g. doubles with NaN/Inf, denormals, etc. with no loss of
   * precision.
   */
  OBJECT_RESULT,
  /**
   * Parameters are not converted.
   */
  PASSTHROUGH,

}
