package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeMissing
 */
public final class XLMissing implements XLValue {

  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLMissing(this);
  }

}
