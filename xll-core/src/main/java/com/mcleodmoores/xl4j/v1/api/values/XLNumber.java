/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.values;

/**
 * Java representation of the xloper type xltypeNum. This holds an Excel Number, which is an integer or floating point number, with slightly
 * lower precision than IEEE-754 floating point (no Inf/NaN or denormal).
 */
public final class XLNumber implements XLValue {
  /** The value as a double */
  private final double _value;

  /**
   * @param value
   *          the value
   */
  private XLNumber(final double value) {
    _value = value;
  }

  /**
   * Static factory method to create an instance of an XLNumber. NOTE: currently accepts NaN/Inf even though Excel doesn't support them.
   * 
   * @param value
   *          the value
   * @return XLNumber
   */
  public static XLNumber of(final double value) {
    return new XLNumber(value);
  }

  /**
   * Static factory method to create an instance of an XLNumber.
   * 
   * @param value
   *          the value
   * @return XLNumber
   */
  public static XLNumber of(final int value) {
    return new XLNumber(value);
  }

  /**
   * Static factory method to create an instance of an XLNumber.
   * 
   * @param value
   *          the value
   * @return XLNumber
   */
  public static XLNumber of(final long value) {
    return new XLNumber(value);
  }

  /**
   * Gets the value as stored.
   * 
   * @return the value
   */
  public double getValue() {
    return _value;
  }

  /**
   * Casts the value to a short.
   * 
   * @return the value as a short
   */
  public short getAsShort() {
    return (short) _value;
  }

  /**
   * Casts the value to an int.
   * 
   * @return the value as an int
   */
  public int getAsInt() {
    return (int) _value;
  }

  /**
   * Casts the value to a long.
   * 
   * @return the value as a long
   */
  public long getAsLong() {
    return (long) _value;
  }

  /**
   * Casts the value to a float.
   * 
   * @return the value as a float
   */
  public float getAsFloat() {
    return (float) _value;
  }

  /**
   * Gets the value as a double.
   * 
   * @return the value as a double
   */
  public double getAsDouble() {
    return _value;
  }

  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLNumber(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_value);
    result = prime * result + (int) (temp ^ temp >>> 32);
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
    if (!(obj instanceof XLNumber)) {
      return false;
    }
    final XLNumber other = (XLNumber) obj;
    if (Double.doubleToLongBits(_value) != Double.doubleToLongBits(other._value)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "XLNumber[value=" + _value + "]";
  }

}
