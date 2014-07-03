package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeBool
 */
public final class XLBoolean implements XLValue {
  private final boolean _value;
  private XLBoolean(final boolean value) {
    _value = value;
  }
  
  public static XLBoolean of(final boolean value) {
    return new XLBoolean(value);
  }
  
  public boolean getValue() {
    return _value;
  }
  
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLBoolean(this);
  }

}
