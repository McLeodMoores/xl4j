/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.util;

/**
 * Utility class for generating export names for the DLL.
 */
public final class ExportUtils {
  private ExportUtils() {
  }
  
  /**
   * Builds the name of the DLL native function export.
   * @param exportNumber  the number of the export within this number of parameters.
   * @return a string of the export name
   */
  public static String buildExportName(final int exportNumber) {
    return "UDF_" + exportNumber;
  }
}
