package com.mcleodmoores.excel4j;

/**
 * Java representation of the xloper type xltypeSRef
 * It represents a reference to a cell on the current sheet.
 */
public class XLLocalReference implements XLValue {

  public <E> E accept(XLValueVisitor<E> visitor) {
    return visitor.visitXLLocalReference(this);
  }

}
