/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.values;

/**
 * Java representation of the xloper type xltypeNil. This is used to represent completely blank cells in XLArrays.
 */
public enum XLNil implements XLValue {
  /**
   * Singleton instance.
   */
  INSTANCE;

  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLNil(this);
  }

  @Override
  public String toString() {
    return "XLNil";
  }
}
