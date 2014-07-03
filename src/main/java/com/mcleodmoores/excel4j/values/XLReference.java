package com.mcleodmoores.excel4j.values;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mcleodmoores.excel4j.util.ArgumentChecker;

/**
 * Java representation of the xloper type xltypeRef.
 */
public final class XLReference implements XLValue {

  private List<XLRange> _ranges;

  private XLReference(final List<XLRange> ranges) {
    _ranges = ranges;
  }
  
  /**
   * Static factory method to create an instance of XLReference.
   * @param range a single contiguous 2D range of cells
   * @return an instance of XLLocalReference
   */
  public static XLReference of(final XLRange range) {
    ArgumentChecker.notNull(range, "range");
    return new XLReference(Collections.singletonList(range));
  }
  
  /**
   * Static factory method to create an instance of XLReference.
   * @param ranges a list of contiguous 2D range of cells
   * @return an instance of XLReference
   */
  public static XLReference of(final List<XLRange> ranges) {
    ArgumentChecker.notNullOrEmpty(ranges, "ranges");
    return new XLReference(ranges);
  }
  
  /**
   * Static factory method to create an instance of XLReference.
   * @param ranges a vararg of contiguous 2D range of cells
   * @return an instance of XLReference
   */
  public static XLReference of(final XLRange... ranges) {
    ArgumentChecker.notNullOrEmpty(ranges, "ranges");
    return new XLReference(Arrays.asList(ranges));
  }
  
  /**
   * @return the range, not null
   */
  public List<XLRange> getRanges() {
    return _ranges;
  }
  
  /**
   * @return true, if a single range
   */
  public boolean isSingleRange() {
    return _ranges.size() == 1;
  }
  
  /**
   * @return the range if single range, or first range if multiple ranges.
   */
  public XLRange getSingleRange() {
    return _ranges.get(0);
  }
  
  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLReference(this);
  }

}
