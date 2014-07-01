package com.mcleodmoores.excel4j;

/**
 * Java representation of the xloper type xltypeNil
 */
public class XLNil implements XLValue {

  public <E> E accept(XLValueVisitor<E> visitor) {
    return visitor.visitXLNil(this);
  }

}
