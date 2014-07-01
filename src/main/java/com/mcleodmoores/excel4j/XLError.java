package com.mcleodmoores.excel4j;

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
  
  public <E> E accept(XLValueVisitor<E> visitor) {
    return visitor.visitXLError(this);
  }

}
