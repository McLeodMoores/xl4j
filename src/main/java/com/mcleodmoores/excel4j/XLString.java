package com.mcleodmoores.excel4j;

/**
 * Java representation of the xloper type xltypeStr
 */
public class XLString implements XLValue {

  public <E> E accept(XLValueVisitor<E> visitor) {
    return visitor.visitXLString(this);
  }

}
