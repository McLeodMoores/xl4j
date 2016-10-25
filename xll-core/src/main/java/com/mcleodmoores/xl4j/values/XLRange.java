/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.values;

import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;

/**
 * Represents a single rectangular range in Excel. This class is usually passed into the factory method (of()) of XLLocalReference or
 * XLMultiReference. Note that this class lives outside the XLValue hierarchy, but is used as arguments to some of it's factory methods.
 */
public final class XLRange {
  private final int _rowFirst;
  private final int _rowLast;
  private final int _columnFirst;
  private final int _columnLast;

  private XLRange(final int rowFirst, final int rowLast, final int columnFirst, final int columnLast) {
    _rowFirst = rowFirst;
    _rowLast = rowLast;
    _columnFirst = columnFirst;
    _columnLast = columnLast;
  }

  /**
   * Static factory method for creating ranges.
   * 
   * @param rowFirst
   *          the first row in the range.
   * @param rowLast
   *          the last row in the range.
   * @param columnFirst
   *          the first column in the range.
   * @param columnLast
   *          the last column in the range.
   * @return an instance representing the range.
   */
  public static XLRange of(final int rowFirst, final int rowLast, final int columnFirst, final int columnLast) {
    ArgumentChecker.notNegative(rowFirst, "rowFirst");
    ArgumentChecker.notNegative(rowLast, "rowLast");
    ArgumentChecker.notNegative(columnFirst, "columnFirst");
    ArgumentChecker.notNegative(columnLast, "columnLast");
    if (rowFirst > rowLast) {
      throw new Excel4JRuntimeException("rowFirst must be <= rowLast");
    }
    if (columnFirst > columnLast) {
      throw new Excel4JRuntimeException("columnFirst must be <= columnLast");
    }
    return new XLRange(rowFirst, rowLast, columnFirst, columnLast);
  }

  /**
   * Static factory method for creating ranges of just a single cell.
   * 
   * @param row
   *          the row
   * @param column
   *          the column
   * @return an instance representing the range.
   */
  public static XLRange ofCell(final int row, final int column) {
    ArgumentChecker.notNegative(row, "row");
    ArgumentChecker.notNegative(column, "column");
    return new XLRange(row, row, column, column);
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

  /**
   * @return true, if the range is a single column or single cell
   */
  public boolean isSingleColumn() {
    return _columnFirst == _columnLast;
  }

  /**
   * @return true, if the range is a single row or single cell
   */
  public boolean isSingleRow() {
    return _rowFirst == _rowLast;
  }

  /**
   * @return true, if the range is for a single cell
   */
  public boolean isSingleCell() {
    return _rowFirst == _rowLast && _columnFirst == _columnLast;
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
    final XLRange other = (XLRange) obj;
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
    if (isSingleCell()) {
      return "XLRange[Single Cell row=" + _rowFirst + ", column=" + _columnFirst + "]";
    }
    if (isSingleColumn()) {
      return "XLRange[Single Column rows=" + _rowFirst + " to " + _rowLast + ", column=" + _columnFirst + "]";
    }
    if (isSingleRow()) {
      return "XLRange[Single Row row=" + _rowFirst + ", columns=" + _columnFirst + " to " + _columnLast + "]";
    }
    return "XLRange[Range rows=" + _rowFirst + " to " + _rowLast + ", columns=" + _columnFirst + " to " + _columnLast + "]";
  }

}
