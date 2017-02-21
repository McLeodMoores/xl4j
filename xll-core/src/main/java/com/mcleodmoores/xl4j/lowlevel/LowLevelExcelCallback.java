/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.lowlevel;

/**
 * The Excel low-level callback interface. This may or may not be necessary.
 */
public interface LowLevelExcelCallback {

  /**
   * Excel callback interface to register a function.
   *
   * @param exportNumber
   *          the export number allocated to this function
   * @param functionExportName
   *          the name of the C export to handle this function call, not null
   * @param isVarArgs
   *          true, if the function has a variable argument list
   * @param isLongRunning
   *          true if the function is likely to be long-running
   * @param isAutoAsynchronous
   *          true if the function should be made called in a thread drawn from a pool and excel notified
   *          asynchronously on completion.  Allows long running functions not to block Excel
   * @param isManualAsynchronous
   *          true if the function should handle asynchronous execution itself.  Currently this is a
   *          synonym for isAutoAsynchronou, although it's really just a placeholder until the functionality
   *          is implemented.  Do not use.
   * @param isCallerRequired
   *          true if the function should be provided with an extra argument detailing the calling context,
   *          i.e. which cell or cells are being calculated.  This is not currently supported and is just
   *          a placeholder until the functionality is implemented.  Do not use.
   * @param functionSignature
   *          a string representing the functions return type, parameter types and calling permission, not null
   * @param functionWorksheetName
   *          the name of the function as it is to appear on the worksheet, case sensitive, not null
   * @param parameterNames
   *          a comma separated list of function parameter names (no padding spaces)
   * @param functionType
   *          an integer representing the function type (1=Function, 2=Command)
   * @param functionCategory
   *          the paste function category
   * @param acceleratorKey
   *          for commands, represents a CTRL-SHIFT-&lt;acceleratorKey&gt; shortcut. If not used, pass XLNil or XLMissing
   * @param helpTopic
   *          the help topic
   * @param description
   *          a description of this function or command
   * @param argsHelp
   *          a varargs/array of help strings for each parameter
   * @return the function registration number
   */
  // CHECKSTYLE:OFF -- says we shouldn't have this many parameters. Take it up with Microsoft.
  int xlfRegister(int exportNumber, String functionExportName, boolean isVarArgs, boolean isLongRunning,
      boolean isAutoAsynchronous, boolean isManualAsynchronous, boolean isCallerRequired,
      String functionSignature, String functionWorksheetName, String parameterNames, int functionType,
      String functionCategory, String acceleratorKey, String helpTopic, String description, String... argsHelp);
  // CHECKSTYLE:ON
  /*
   * This block is to be ignored: return code, see below
   * 0 = success
   * 1 = macro halted
   * 2 = invalid function number
   * 4 = invalid number of arguments
   * 8 = invalid OPER structure
   * 16 = stack overflow
   * 32 = command failed
   * 64 = uncalced cell
   * 128 = not allowed during multi-threaded calc
   */
}
