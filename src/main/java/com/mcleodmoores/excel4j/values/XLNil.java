package com.mcleodmoores.excel4j.values;


/**
 * Java representation of the xloper type xltypeNil.
 * This is used to represent completely blank cells in XLValueRanges.
 */
public enum XLNil implements XLValue {
  /**
   * Singleton instance.  
   */
  INSTANCE;
    
  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLNil(this);
  }
  
  @Override
  public String toString() {
    return "XLNil";
  }
}
