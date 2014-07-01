package com.mcleodmoores.excel4j;

/**
 * Java representation of the xloper type xltypeError
 */
public class XLError implements XLValue {

  public <E> E accept(XLValueVisitor<E> visitor) {
    return visitor.visitXLError(this);
  }

}
