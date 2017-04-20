/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.values;

import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Java representation of the xloper type xltypeStr. Holds an Excel String.
 */
public final class XLString implements XLValue {

  private static final String OBJECT_PREFIX = "\u00BB";
  private final String _value;

  private XLString(final String value) {
    _value = value;
  }

  /**
   * Static factory method to create an instance of an XLString.
   *
   * @param value
   *          the string, not null
   * @return an instance
   */
  public static XLString of(final String value) {
    ArgumentChecker.notNull(value, "value");
    return new XLString(value);
  }

  /**
   * @return the value
   */
  public String getValue() {
    return _value;
  }

  /**
   * @return true, if this XLString represents an XLObject
   */
  public boolean isXLObject() {
    return _value.startsWith(OBJECT_PREFIX);
  }

  /**
   * @return XLObject if string contains object handle, throws XL4JRuntimeException otherwise
   * Checks with {@link #isXLObject} before calling. Note this does not check validity on heap.
   */
  public XLObject toXLObject() {
    if (isXLObject()) {
      final String[] split = _value.split("-");
      if (split.length != 2) {
        throw new XL4JRuntimeException("String has object prefix character but cannot split on hyphen");
      }
      return XLObject.of(split[0].substring(1), Long.parseUnsignedLong(split[1]));
    }
    throw new XL4JRuntimeException("XLString is not object handle");
  }

  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLString(this);
  }

  @Override
  public int hashCode() {
    return _value.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof XLString)) {
      return false;
    }
    final XLString other = (XLString) obj;
    if (!_value.equals(other._value)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "XLString[value=" + _value + "]";
  }

}
