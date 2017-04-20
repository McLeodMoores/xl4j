/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.values;

/**
 * Simple wrapper for an Excel sheet ID.
 */
public final class XLSheetId {
  private final int _sheetId;

  private XLSheetId(final int sheetId) {
    _sheetId = sheetId;
  }

  /**
   * Static factory method to create and instance of an XLSheetId.
   * 
   * @param sheetId
   *          the sheet ID
   * @return an instance
   */
  public static XLSheetId of(final int sheetId) {
    return new XLSheetId(sheetId);
  }

  /**
   * @return the sheet id
   */
  public int getSheetId() {
    return _sheetId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _sheetId;
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
    if (!(obj instanceof XLSheetId)) {
      return false;
    }
    final XLSheetId other = (XLSheetId) obj;
    if (_sheetId != other._sheetId) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "XLSheetId[sheetId=" + _sheetId + "]";
  }

}
