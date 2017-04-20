/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.core;

/**
 * Enum representing the types that can be referenced as functions from Excel. Note that {{@link #FIELD} is
 * used to refer to fields and enum values.
 */
public enum CallTarget {

  /**
   * A field or enum.
   */
  FIELD,
  /**
   * A method.
   */
  METHOD,
  /**
   * A constructor.
   */
  CONSTRUCTOR
}
