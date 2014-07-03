/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.values;

/**
 * Represents a single rectangular range in Excel.  
 * This class is usually passed into the factory method of XLLocalReference or XLMultiReference.
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
}
