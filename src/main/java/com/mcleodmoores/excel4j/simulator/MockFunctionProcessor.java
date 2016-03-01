/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.simulator;

import java.util.Arrays;

import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.ExcelFunctionCallHandler;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLValue;
import com.mcleodmoores.excel4j.xll.XLLAccumulatingFunctionRegistry;
import com.mcleodmoores.excel4j.xll.XLLAccumulatingFunctionRegistry.LowLevelEntry;

/**
 *
 */
public class MockFunctionProcessor {
  private final Excel _excel = ExcelFactory.getInstance();
  private final LowLevelEntry[] _entries;
  private final ExcelFunctionCallHandler _excelCallHandler;

  /**
   * Create a function processor.
   */
  public MockFunctionProcessor() {
    _excel.getFunctionRegistry().registerFunctions(_excel.getExcelCallback());
    final XLLAccumulatingFunctionRegistry excelFunctionRegistry = (XLLAccumulatingFunctionRegistry) _excel.getExcelCallback().getLowLevelExcelCallback();
    _entries = excelFunctionRegistry.getEntries();
    _excelCallHandler = _excel.getExcelCallHandler();
  }

  /**
   * Invoke a function.
   * @param functionName the name of the function
   * @param args the arguments
   * @return the result
   */
  public XLValue invoke(final String functionName, final XLValue... args) {
    int exportNumber = -1;
    for (final LowLevelEntry entry : _entries) {
      if (entry._functionWorksheetName.equals(functionName)) {
        exportNumber = entry._exportNumber;
      }
    }
    final Object[] newArgs = new Object[args.length];
    System.arraycopy(args, 0, newArgs, 0, args.length);
    try {
      return _excelCallHandler.invoke(exportNumber, args);
    } catch (IllegalArgumentException | ClassCastException e) {
      throw new Excel4JRuntimeException("Problem invoking function " + functionName + " with args " + Arrays.toString(newArgs), e);
    }
  }
}
