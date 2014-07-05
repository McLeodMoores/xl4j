package com.mcleodmoores.excel4j.values;


/**
 * Java representation of the xloper type xltypeNil.
 * This is used to represent completely blank cells in XLValueRanges.
 */
public final class XLNil implements XLValue {
  private XLNil() {
  }
  
  /**
   * Bill Pugh singleton pattern helper class removes synchronization requirement.
   */
  private static class XLNilHelper {
    private static final XLNil INSTANCE = new XLNil();
  }
  
  /**
   * Get an instance of an XLNil.
   * @return a singleton instance
   */
  public static XLNil getInstance() {
    return XLNilHelper.INSTANCE;
  }
  
  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLNil(this);
  }

  // default hashCode and equals will suffice here.
  
  @Override
  public String toString() {
    return "XLNil";
  }
}
