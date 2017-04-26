/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.values;

import java.util.Objects;

import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Special version of XLString that holds a String with unprintable characters at the start that encodes an object handle.
 */
public final class XLObject implements XLValue {
  /** The object prefix */
  private static final char OBJECT_PREFIX = '\u00BB';
  /** The simple name */
  private final String _clazz;
  /** The handle */
  private final long _handle;

  /**
   * @param clazz
   *          the simple name
   * @param handle
   *          the handle
   */
  private XLObject(final String clazz, final long handle) {
    _clazz = clazz;
    _handle = handle;
  }

  /**
   * Static factory method to create an instance of an XLString.
   *
   * @param clazz
   *          the Class that this object points to
   * @param handle
   *          the object handle
   * @return an instance
   */
  public static XLObject of(final String clazz, final long handle) {
    ArgumentChecker.notNull(clazz, "clazz");
    return new XLObject(clazz, handle);
  }

  /**
   * Static factory method to create an instance of an XLString.
   *
   * @param clazz
   *          the Class that this object points to
   * @param handle
   *          the object handle
   * @return an instance
   */
  public static XLObject of(final Class<?> clazz, final long handle) {
    ArgumentChecker.notNull(clazz, "clazz");
    final String simpleName = clazz.getSimpleName();
    return new XLObject(simpleName == null ? clazz.getName() : simpleName, handle);
  }

  /**
   * @return the object's class
   */
  public String getClazz() {
    return _clazz;
  }

  /**
   * @return the object handle
   */
  public long getHandle() {
    return _handle;
  }

  /**
   * Convert this XLObject into an XLString for passing back to Excel. It adds a ^Z control character to the front of the string so Excel
   * users cannot create arbitrary object references, but can read them. The string produced is in the form ClassName-000000000000000000000.
   *
   * @return an XLString object containing the object handle and class name.
   */
  public XLString toXLString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(OBJECT_PREFIX);
    sb.append(_clazz);
    sb.append('-');
    sb.append(Long.toUnsignedString(_handle));
    return XLString.of(sb.toString());
  }

  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLObject(this);
  }

  @Override
  public int hashCode() {
    return (int) _handle;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof XLObject)) {
      return false;
    }
    final XLObject other = (XLObject) obj;
    if (_handle != other._handle) {
      return false;
    }
    if (!Objects.equals(_clazz, other._clazz)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "XLObject[class=" + _clazz + ", " + Long.toUnsignedString(_handle) + "]";
  }

}
