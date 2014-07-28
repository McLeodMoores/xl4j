/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.util;

/**
 * Utility class for generating export names for the DLL.
 */
public final class ExportUtils {
  private ExportUtils() {
  }
  
  /**
   * Builds the name of the DLL native function export.
   * @param params  the number of machine words (sizeof(void *)) for the parameters.
   * @param exportNumber  the number of the export within this number of parameters.
   * @return a string of the export name
   */
  public static String buildExportName(final int params, final int exportNumber) {
    return "UDF_" + params + "_" + exportNumber;
  }
}
