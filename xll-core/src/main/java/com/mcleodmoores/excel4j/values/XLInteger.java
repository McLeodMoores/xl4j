/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeInt
 * This is never returned by Excel, only occasionally used when calling into Excel.
 */
public final class XLInteger implements XLValue {
  private final int _value;

  private XLInteger(final int value) {
    _value = value;
  }

  /**
   * Static factory method to return an instance.
   * @param value the value
   * @return an instance
   */
  public static XLInteger of(final int value) {
    return new XLInteger(value);
  }

  /**
   * Get the value.
   * @return the value
   */
  public int getValue() {
    return _value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _value;
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
    if (!(obj instanceof XLInteger)) {
      return false;
    }
    final XLInteger other = (XLInteger) obj;
    if (_value != other._value) {
      return false;
    }
    return true;
  }

  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLInteger(this);
  }

  @Override
  public String toString() {
    return "XLInteger[value=" + _value + "]";
  }

}
