package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeSRef
 * It represents a reference to a cell on the current sheet.
 */
public final class XLLocalReference implements XLValue {

  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLLocalReference(this);
  }

}
