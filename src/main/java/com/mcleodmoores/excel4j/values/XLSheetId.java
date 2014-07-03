/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.values;

/**
 * Simple wrapper for an Excel sheet ID.
 */
public final class XLSheetId {
  private int _sheetId;

  private XLSheetId(final int sheetId) {
    _sheetId = sheetId;
  }
  
  /**
   * Static factory method to create and instance of an XLSheetId.
   * @param sheetId the sheet ID
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
}
