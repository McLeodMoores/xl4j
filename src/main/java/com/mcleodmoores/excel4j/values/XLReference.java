package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeRef
 */
public final class XLReference implements XLValue {

  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLReference(this);
  }

}
