package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeSRef
 * It represents a reference to a single block of cells on the current sheet.
 */
public final class XLLocalReference implements XLValue {

  private XLRange _range;

  private XLLocalReference(final XLRange range) {
    _range = range;
  }
  
  /**
   * Static factory method to create an instance of XLLocalReference.
   * @param range a single contiguous 2D range of cells
   * @return an instance of XLLocalReference
   */
  public static XLLocalReference of(final XLRange range) {
    return new XLLocalReference(range);
  }
  
  /**
   * @return the range, not null
   */
  public XLRange getRange() {
    return _range;
  }
  
  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLLocalReference(this);
  }

}
