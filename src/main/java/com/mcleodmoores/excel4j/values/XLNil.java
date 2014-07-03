package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeNil
 */
public final class XLNil implements XLValue {

  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLNil(this);
  }

}
