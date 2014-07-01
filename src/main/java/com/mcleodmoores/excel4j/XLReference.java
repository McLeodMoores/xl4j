package com.mcleodmoores.excel4j;

/**
 * Java representation of the xloper type xltypeRef
 */
public class XLReference implements XLValue {

  public <E> E accept(XLValueVisitor<E> visitor) {
    return visitor.visitXLReference(this);
  }

}
