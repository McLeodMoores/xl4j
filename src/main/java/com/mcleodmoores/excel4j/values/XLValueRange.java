package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeMulti
 * It can take the form of a two dimensional array of mixed types of xlopers.
 */
public final class XLValueRange implements XLValue {

  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLValueRange(this);
  }

}
