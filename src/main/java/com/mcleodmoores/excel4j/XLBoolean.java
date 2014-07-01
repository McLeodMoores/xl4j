package com.mcleodmoores.excel4j;

/**
 * Java representation of the xloper type xltypeBool
 */
public class XLBoolean implements XLValue {

  public <E> E accept(XLValueVisitor<E> visitor) {
    return visitor.visitXLBoolean(this);
  }

}
