package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeError
 */
public enum XLError implements XLValue {
  Null,
  Div0,
  Value,
  Ref,
  Name,
  Num,
  NA;
  
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLError(this);
  }

}
