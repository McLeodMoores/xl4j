/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Interface to be invoked from Excel.
 */
public interface ExcelFunctionCallHandler {
  /**
   * Invocation of a static method.
   * @param exportNumber  the number of the export in this block with the same number of parameters
   * @param args  the arguments to pass to the method
   * @return the value to pass back to Excel
   */
  XLValue invoke(int exportNumber, XLValue... args);
}
