package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeInt
 */
public final class XLInteger implements XLValue {
  private final int _value;
  
  private XLInteger(final int value) {
    _value = value;
  }
  
  public static XLInteger of(final int value) {
    return new XLInteger(value);
  }
  
  public int getValue() {
    return _value;
  }
  
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLInteger(this);
  }

}
