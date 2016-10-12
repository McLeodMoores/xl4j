/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.lowlevel;


/**
 * The Excel low-level callback interface.  This may or may not be necessary. 
 */
public interface LowLevelExcelCallback {
  /**
   * Excel callback interface to register a function.
   * @param exportNumber  the export number allocated to this function
   * @param functionExportName  the name of the C export to handle this function call, not null
   * @param isVarArgs  true, if the function has a variable argument list
   * @param functionSignature  a string representing the functions return type, parameter types and calling permission, not null
   * @param functionWorksheetName  the name of the function as it is to appear on the worksheet, case sensitive, not null
   * @param argumentNames  a comma separated list of function argument names (no padding spaces)
   * @param functionType  an integer representing the function type (1=Function, 2=Command)
   * @param functionCategory  the paste function category (see StandardFunctionCategories)
   * @param acceleratorKey  for commands, represents a CTRL-SHIFT-&lt;acceleratorKey&gt; shortcut.  If not used, pass XLNil or XLMissing
   * @param helpTopic  the help topic
   * @param description  a description of this function or command
   * @param argsHelp  a varargs/array of help strings for each parameter
   * @return the function registration number
   */
  // CHECKSTYLE:OFF -- says we shouldn't have this many parameters.  Take it up with Microsoft.
  int xlfRegister(final int exportNumber,
                  final String functionExportName,
                  final boolean isVarArgs,
                  final String functionSignature,
                  final String functionWorksheetName,
                  final String argumentNames,
                  final int functionType,
                  final String functionCategory,
                  final String acceleratorKey,
                  final String helpTopic,
                  final String description,
                  final String... argsHelp);
  // CHECKSTYLE:ON
  /*
   * This block is to be ignored:
   * return code, see below
   *   0 = success 
   *   1 = macro halted
   *   2 = invalid function number 
   *   4 = invalid number of arguments 
   *   8 = invalid OPER structure   
   *  16 = stack overflow   
   *  32 = command failed  
   *  64 = uncalced cell
   * 128 = not allowed during multi-threaded calc
   */
}
