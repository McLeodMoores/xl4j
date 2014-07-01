package com.mcleodmoores.excel4j;

/**
 * Java representation of the xloper type xltypeInt
 */
public class XLInteger implements XLValue {

  public <E> E accept(XLValueVisitor<E> visitor) {
    return visitor.visitXLInteger(this);
  }

}
