package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeBool.
 */
public final class XLBoolean implements XLValue {
  private final boolean _value;
  private XLBoolean(final boolean value) {
    _value = value;
  }
  
  /**
   * Create an instance of an XLBoolean.
   * @param value the value to embed
   * @return an instance
   */
  public static XLBoolean of(final boolean value) {
    return new XLBoolean(value);
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

}
