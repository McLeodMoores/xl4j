/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.values;

/**
 * Represents a single rectangular range in Excel.  
 * This class is usually passed into the factory method (of()) of XLLocalReference or XLMultiReference.
 * Note that this class lives outside the XLValue hierarchy, but is used as arguments to some of it's factory methods.
 */
public final class XLRange {
  private int _rowFirst;
  private int _rowLast;
  private int _columnFirst;
  private int _columnLast;

  private XLRange(final int rowFirst, final int rowLast, final int columnFirst, final int columnLast) {
    _rowFirst = rowFirst;
    _rowLast = rowLast;
    _columnFirst = columnFirst;
    _columnLast = columnLast;
  }
  
  /**
   * Static factory method for creating ranges.
   * @param rowFirst the first row in the range.
   * @param rowLast the last row in the range.
   * @param columnFirst the first column in the range.
   * @param columnLast the last column in the range.
   * @return an instance representing the range.
   */
  public static XLRange of(final int rowFirst, final int rowLast, final int columnFirst, final int columnLast) {
    return new XLRange(rowFirst, rowLast, columnFirst, columnLast);
  }
  
  /**
   * @return the rowFirst
   */
  public int getRowFirst() {
    return _rowFirst;
  }

  /**
   * @return the rowLast
   */
  public int getRowLast() {
    return _rowLast;
  }

  /**
   * @return the columnFirst
   */
  public int getColumnFirst() {
    return _columnFirst;
  }

  /**
   * @return the columnLast
   */
  public int getColumnLast() {
    return _columnLast;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _columnFirst;
    result = prime * result + _columnLast;
    result = prime * result + _rowFirst;
    result = prime * result + _rowLast;
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
    if (!(obj instanceof XLRange)) {
      return false;
    }
    XLRange other = (XLRange) obj;
    if (_columnFirst != other._columnFirst) {
      return false;
    }
    if (_columnLast != other._columnLast) {
      return false;
    }
    if (_rowFirst != other._rowFirst) {
      return false;
    }
    if (_rowLast != other._rowLast) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "XLRange[rowFirst=" + _rowFirst + ", rowLast=" + _rowLast + ", columnFirst=" + _columnFirst + ", columnLast=" + _columnLast + "]";
  }
  
}
