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
  private XLSheetId _sheetId;

  private XLReference(final XLSheetId sheetId, final List<XLRange> ranges) {
    _sheetId = sheetId;
    _ranges = ranges;
  }
  
  /**
   * Static factory method to create an instance of XLReference.
   * @param sheetId the worksheet ID
   * @param range a single contiguous 2D range of cells
   * @return an instance of XLLocalReference
   */
  public static XLReference of(final XLSheetId sheetId, final XLRange range) {
    ArgumentChecker.notNull(sheetId, "sheetId");
    ArgumentChecker.notNull(range, "range");
    return new XLReference(sheetId, Collections.singletonList(range));
  }
  
  /**
   * Static factory method to create an instance of XLReference.
   * @param sheetId the worksheet ID
   * @param ranges a list of contiguous 2D range of cells
   * @return an instance of XLReference
   */
  public static XLReference of(final XLSheetId sheetId, final List<XLRange> ranges) {
    ArgumentChecker.notNull(sheetId, "sheetId");
    ArgumentChecker.notNullOrEmpty(ranges, "ranges");
    return new XLReference(sheetId, ranges);
  }
  
  /**
   * Static factory method to create an instance of XLReference.
   * @param sheetId the worksheet ID
   * @param ranges a vararg of contiguous 2D range of cells
   * @return an instance of XLReference
   */
  public static XLReference of(final XLSheetId sheetId, final XLRange... ranges) {
    ArgumentChecker.notNull(sheetId, "sheetId");
    ArgumentChecker.notNullOrEmpty(ranges, "ranges");
    return new XLReference(sheetId, Arrays.asList(ranges));
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
  
  /**
   * @return the worksheet ID
   */
  public XLSheetId getSheetId() {
    return _sheetId;
  }
  
  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLReference(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_ranges == null) ? 0 : _ranges.hashCode());
    result = prime * result + ((_sheetId == null) ? 0 : _sheetId.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof XLReference)) {
      return false;
    }
    XLReference other = (XLReference) obj;
    if (!_ranges.equals(other._ranges)) {
      return false;
    }
    if (!_sheetId.equals(other._sheetId)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "XLReference[sheetId=" + _sheetId + ", ranges=" + _ranges + "]";
  }

}
