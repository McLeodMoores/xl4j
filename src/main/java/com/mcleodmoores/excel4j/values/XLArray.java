/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.values;

import java.util.Arrays;

import com.mcleodmoores.excel4j.util.ArgumentChecker;

/**
 * Java representation of the xloper type xltypeMulti
 * It can take the form of a two dimensional array of mixed types of xlopers.
 * REVIEW: should this be generic?
 */
public final class XLArray implements XLValue, XLReference {

  private final XLValue[][] _array;

  private XLArray(final XLValue[][] valueRange) {
    _array = valueRange;
  }

  /**
   * Static factory method to create an instance of XLValueRange.
   * @param array a two dimensional array containing XLValues
   * @return an instance
   */
  public static XLArray of(final XLValue[][] array) {
    ArgumentChecker.notNullOrEmpty(array, "array"); // not checking for squareness.
    return new XLArray(array);
  }

  /**
   * @return a two dimensional array of values, not null
   */
  public XLValue[][] getArray() {
    return _array;
  }

  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLArray(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.deepHashCode(_array); // Arrays.hashCode() had issues.
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
    if (!(obj instanceof XLArray)) {
      return false;
    }
    final XLArray other = (XLArray) obj;
    if (!Arrays.deepEquals(_array, other._array)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "XLArray[" + Arrays.deepToString(_array) + "]";
  }

}
