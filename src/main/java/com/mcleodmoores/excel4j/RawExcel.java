/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

/**
 * Interface Excel itself would call.
 */
public final class RawExcel {
  
  private RawExcel() {
  }
  
  /**
   * Entry point called from excel when the plug-in is loaded.
   * This should register any worksheet functions/commands/macros.
   * @return 1
   */
  public static int xlAutoOpen() {
    Excel excel = ExcelFactory.getInstance();
    excel.getFunctionRegistry().registerFunctions(excel.getExcelCallback());
    return 1; // must return 1.
  }
  
  
}
