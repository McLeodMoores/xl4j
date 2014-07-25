/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.values;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mcleodmoores.excel4j.util.ArgumentChecker;

/**
 * Java representation of the xloper type xltypeRef (aka xltypeMRef).
 * This is usually preferable over XLLocalReference even if you're not referring
 * to multiple ranges because it's keyed to a particular sheetId.
 */
public final class XLMultiReference implements XLValue {

  private final List<XLRange> _ranges;
  private final XLSheetId _sheetId;

  private XLMultiReference(final XLSheetId sheetId, final List<XLRange> ranges) {
    _sheetId = sheetId;
    _ranges = ranges;
  }

  /**
   * Static factory method to create an instance of XLReference.
   * @param sheetId the worksheet ID
   * @param range a single contiguous 2D range of cells
   * @return an instance of XLLocalReference
   */
  public static XLMultiReference of(final XLSheetId sheetId, final XLRange range) {
    ArgumentChecker.notNull(sheetId, "sheetId");
    ArgumentChecker.notNull(range, "range");
    return new XLMultiReference(sheetId, Collections.singletonList(range));
  }

  /**
   * Static factory method to create an instance of XLReference.
   * @param sheetId the worksheet ID
   * @param ranges a list of contiguous 2D range of cells
   * @return an instance of XLReference
   */
  public static XLMultiReference of(final XLSheetId sheetId, final List<XLRange> ranges) {
    ArgumentChecker.notNull(sheetId, "sheetId");
    ArgumentChecker.notNullOrEmpty(ranges, "ranges");
    return new XLMultiReference(sheetId, ranges);
  }

  /**
   * Static factory method to create an instance of XLReference.
   * @param sheetId the worksheet ID
   * @param ranges a vararg of contiguous 2D range of cells
   * @return an instance of XLReference
   */
  public static XLMultiReference of(final XLSheetId sheetId, final XLRange... ranges) {
    ArgumentChecker.notNull(sheetId, "sheetId");
    ArgumentChecker.notNullOrEmpty(ranges, "ranges");
    return new XLMultiReference(sheetId, Arrays.asList(ranges));
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
    return visitor.visitXLMultiReference(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _ranges.hashCode();
    result = prime * result + _sheetId.hashCode();
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
    if (!(obj instanceof XLMultiReference)) {
      return false;
    }
    final XLMultiReference other = (XLMultiReference) obj;
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
    if (isSingleRange()) {
      return "XLMultiReference[sheetId=" + _sheetId.getSheetId() + ", range=" + getSingleRange() + "]";
    } else {
      return "XLMultiReference[sheetId=" + _sheetId.getSheetId() + ", ranges=" + _ranges + "]";
    }
  }

}
