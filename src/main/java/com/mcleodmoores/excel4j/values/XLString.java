package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeStr.
 * Holds an Excel String.
 */
public final class XLString implements XLValue {

  private String _value;
  
  private XLString(final String value) {
    _value = value;
  }
  
  /**
   * Static factory method to create an instance of an XLString.
   * @param value the string
   * @return an instance
   */
  public static XLString of(final String value) {
    return new XLString(value);
  }
  
  /**
   * @return the value
   */
  public String getValue() {
    return _value;
  }
  
  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLString(this);
  }

}
