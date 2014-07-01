package com.mcleodmoores.excel4j;

/**
 * Java representation of the xloper type xltypeMulti
 * It can take the form of a two dimensional array of mixed types of xlopers.
 */
public class XLValueRange implements XLValue {

  public <E> E accept(XLValueVisitor<E> visitor) {
    return visitor.visitXLValueRange(this);
  }

}
