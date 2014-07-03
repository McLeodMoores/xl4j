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
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLInteger(this);
  }

}
