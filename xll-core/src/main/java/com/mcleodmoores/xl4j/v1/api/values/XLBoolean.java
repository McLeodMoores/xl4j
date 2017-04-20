/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.values;

/**
 * Java representation of the xloper type xltypeBool.
 */
public enum XLBoolean implements XLValue {
  // NOTE: if you re-order these fields, you will need to change code on the native side (in CCallExecutor::convert)
  /**
   * True value.
   */
  TRUE,
  /**
   * False value.
   */
  FALSE;

  /**
   * Create an XLBoolean from a boolean.
   * 
   * @param value
   *          the value to embed
   * @return an instance
   */
  public static XLBoolean from(final boolean value) {
    if (value) {
      return TRUE;
    }
    return FALSE;
  }

  /**
   * Get the value of the embedded boolean.
   * 
   * @return the value of the embedded boolean
   */
  public boolean getValue() {
    return this == TRUE;
  }

  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLBoolean(this);
  }

  @Override
  public String toString() {
    return "XLBoolean[" + super.toString() + "]";
  }
}
