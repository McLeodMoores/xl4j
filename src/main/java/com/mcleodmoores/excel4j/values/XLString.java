package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeStr
 */
public final class XLString implements XLValue {

  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLString(this);
  }

}
