/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.lowlevel;

import com.mcleodmoores.xl4j.Excel;
import com.mcleodmoores.xl4j.ExcelFactory;

/**
 * Interface Excel itself would call.
 */
public final class LowLevelExcel {

  private LowLevelExcel() {
  }

  /**
   * Entry point called from excel when the plug-in is loaded. This should register any worksheet functions/commands/macros.
   * 
   * @return 1
   */
  public static int xlAutoOpen() {
    final Excel excel = ExcelFactory.getInstance();
    excel.getFunctionRegistry().registerFunctions(excel.getExcelCallback());
    return 1; // must return 1.
  }

}
