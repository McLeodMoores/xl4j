package com.mcleodmoores.excel4j;

/**
 * Java representation of the xloper type xltypeMissing
 */
public class XLMissing implements XLValue {

  public <E> E accept(XLValueVisitor<E> visitor) {
    return visitor.visitXLMissing(this);
  }

}
