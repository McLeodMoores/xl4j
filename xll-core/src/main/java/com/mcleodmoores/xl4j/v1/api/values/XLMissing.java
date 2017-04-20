/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.values;

/**
 * Java representation of the xloper type xltypeMissing. Instances of this class are passed when arguments to functions taking XLValue are
 * missing.
 */
public enum XLMissing implements XLValue {
  /**
   * Singleton instance.
   */
  INSTANCE;

  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLMissing(this);
  }

  @Override
  public String toString() {
    return "XLMissing";
  }
}
