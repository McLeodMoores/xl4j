/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeBool.
 */
public enum XLBoolean implements XLValue {
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
   * @param value the value to embed
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
