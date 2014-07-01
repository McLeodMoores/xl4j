package com.mcleodmoores.excel4j;

/**
 * Java representation of the xloper type xltypeInt
 */
public final class XLInteger implements XLValue {
  final private int _value;
  
  private XLInteger(int value) {
    _value = value;
  }
  
  public static XLInteger of(int value) {
    return new XLInteger(value);
  }
  
  public int getValue() {
    return _value;
  }
  public <E> E accept(XLValueVisitor<E> visitor) {
    return visitor.visitXLInteger(this);
  }

}
