package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeStr.
 * Holds an Excel String.
 */
public final class XLString implements XLValue {

  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLString(this);
  }

}
