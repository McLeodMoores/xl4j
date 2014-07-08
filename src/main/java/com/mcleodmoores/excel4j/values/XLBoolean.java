package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeBool.
 */
public final class XLBoolean implements XLValue {
  private static final int PRIME2 = 1237;
  private static final int PRIME1 = 1231;
  private final boolean _value;
  /**
   * True value constant.
   */
  public static final XLBoolean TRUE = new XLBoolean(true);
  /**
   * False value constant.
   */
  public static final XLBoolean FALSE = new XLBoolean(false);
  
  private XLBoolean(final boolean value) {
    _value = value;
  }
  
  /**
   * Create an instance of an XLBoolean.
   * @param value the value to embed
   * @return an instance
   */
  public static XLBoolean of(final boolean value) {
    if (value) {
      return TRUE;
    } else {
      return FALSE;
    }
  }
  
  /**
   * Get the value of the embedded boolean.
   * @return the value of the embedded boolean
   */
  public boolean getValue() {
    return _value;
  }
  
  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLBoolean(this);
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_value ? PRIME1 : PRIME2);
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
    if (!(obj instanceof XLBoolean)) {
      return false;
    }
    XLBoolean other = (XLBoolean) obj;
    if (_value != other._value) {
      return false;
    }
    return true; 
    // unreachable because we only give out TRUE or FALSE, so equality caught by first if above.
    // but I'm leaving this here to be defensive.
  }

  @Override
  public String toString() {
    return "XLBoolean[value=" + _value + "]";
  }
}
