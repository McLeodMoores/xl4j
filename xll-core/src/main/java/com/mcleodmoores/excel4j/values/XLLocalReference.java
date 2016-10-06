/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.values;

import com.mcleodmoores.excel4j.util.ArgumentChecker;

/**
 * Java representation of the xloper type xltypeSRef
 * It represents a reference to a single block of cells on the current sheet.
 */
public final class XLLocalReference implements XLValue {

  private final XLRange _range;

  private XLLocalReference(final XLRange range) {
    _range = range;
  }

  /**
   * Static factory method to create an instance of XLLocalReference.
   * @param range a single contiguous 2D range of cells
   * @return an instance of XLLocalReference
   */
  public static XLLocalReference of(final XLRange range) {
    ArgumentChecker.notNull(range, "range");
    return new XLLocalReference(range);
  }

  /**
   * @return the range, not null
   */
  public XLRange getRange() {
    return _range;
  }

  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLLocalReference(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _range.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof XLLocalReference)) {
      return false;
    }
    final XLLocalReference other = (XLLocalReference) obj;
    if (!_range.equals(other._range)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "XLLocalReference[range=" + _range + "]";
  }


}
