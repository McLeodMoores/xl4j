/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Interface to be invoked from Excel.
 */
public interface ExcelConstructorCallHandler {

  /**
   * Creation of a new instance.
   * @param exportNumber  the number of the export in this block with the same number of parameters
   * @param args  the arguments to pass to the constructor
   * @return  the object to pass back to Excel
   */
  XLValue newInstance(int exportNumber, XLValue... args);
}
