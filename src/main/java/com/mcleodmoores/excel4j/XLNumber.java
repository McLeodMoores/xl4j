package com.mcleodmoores.excel4j;

/**
 * Java representation of the xloper type xltypeNum
 */
public class XLNumber implements XLValue {

  public <E> E accept(XLValueVisitor<E> visitor) {
    return visitor.visitXLNumber(this);
  }

}
