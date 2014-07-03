package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeNum
 */
public final class XLNumber implements XLValue {

  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLNumber(this);
  }

}
