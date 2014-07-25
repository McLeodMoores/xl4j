/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;


/**
 * High-level Excel callback interface.
 */
public interface ExcelCallback {
  /**
   * Register a function or command with Excel.
   * @param functionDefinition  the function definition, not null
   * @param functionExportName  the name of the function in the DLL to handle the calls
   */
  void registerFunction(final FunctionDefinition functionDefinition, final String functionExportName);
}
