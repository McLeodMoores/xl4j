package com.mcleodmoores.excel4j.values;

import com.mcleodmoores.excel4j.util.ArgumentChecker;

/**
 * Java representation of the xloper type xltypeMulti
 * It can take the form of a two dimensional array of mixed types of xlopers.
 * REVIEW: should this be generic?
 */
public final class XLValueRange implements XLValue {

  private XLValue[][] _valueRange;
  
  private XLValueRange(final XLValue[][] valueRange) {
    _valueRange = valueRange;
  }
  
  /**
   * Static factory method to create an instance of XLValueRange.
   * @param valueRange a two dimensional array containing XLValues
   * @return an instance
   */
  public static XLValueRange of(final XLValue[][] valueRange) {
    ArgumentChecker.notNullOrEmpty(valueRange, "valueRange"); // not checking for squareness.
    return new XLValueRange(valueRange);
  }
  
  /**
   * @return a two dimensional array of values, not null
   */
  public XLValue[][] getValueRange() {
    return _valueRange;
  }

  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLValueRange(this);
  }

}
