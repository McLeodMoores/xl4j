/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * The Excel callback interface. 
 */
public interface RawExcelCallback {
  /**
   * Excel callback interface to register a function.
   * @param dllPath  the path the DLL in the file system, not null
   * @param functionExportName  the name of the C export to handle this function call, not null
   * @param functionSignature  a string representing the functions return type, parameter types and calling permission, not null
   * @param functionWorksheetName  the name of the function as it is to appear on the worksheet, case sensitive, not null
   * @param argumentNames  a comma separated list of function argument names (no padding spaces)
   * @param functionType  an integer representing the function type (1=Function, 2=Command)
   * @param functionCategory  the paste function category (see StandardFunctionCategories)
   * @param notUsed  not used, pass XLNil or XLMissing
   * @param helpTopic  the help topic
   * @param description  a description of this function or command
   * @param argsHelp  a varargs/array of help strings for each parameter
   * @return the return code, see below
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
  // CHECKSTYLE:OFF -- says we shouldn't have this many parameters.  Take it up with Microsoft.
  XLValue xlfRegister(final XLString dllPath,
                  final XLString functionExportName,
                  final XLString functionSignature,
                  final XLString functionWorksheetName,
                  final XLString argumentNames,
                  final XLValue functionType,
                  final XLValue functionCategory,
                  final XLValue notUsed,
                  final XLValue helpTopic,
                  final XLValue description,
                  final XLValue... argsHelp);
  // CHECKSTYLE:ON
}
