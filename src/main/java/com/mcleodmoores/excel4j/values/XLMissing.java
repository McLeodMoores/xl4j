package com.mcleodmoores.excel4j.values;

/**
 * Java representation of the xloper type xltypeMissing.
 * Instances of this class are passed when arguments to functions taking XLValue are missing.
 */
public final class XLMissing implements XLValue {

  private XLMissing() {
  }
  
  /**
   * Bill Pugh singleton pattern helper class removes synchronization requirement.
   */
  private static class XLMissingHelper {
    private static final XLMissing INSTANCE = new XLMissing();
  }
  
  /**
   * Get an instance of an XLMissing.
   * @return a singleton instance
   */
  public XLMissing getInstance() {
    return XLMissingHelper.INSTANCE;
  }
  
  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLMissing(this);
  }

  // default hashCode and equals will suffice here.
  
  @Override
  public String toString() {
    return "XLMissing";
  }
}
