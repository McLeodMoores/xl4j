/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import com.mcleodmoores.excel4j.simulator.FunctionEntry;
import com.mcleodmoores.excel4j.simulator.MockDLLExports;
import com.mcleodmoores.excel4j.simulator.MockExcelFunctionRegistry;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * 
 */
public class MockFunctionProcessor {
  private MockExcelFunctionRegistry _mockExcelFunctionRegistry;
  private MockDLLExports _mockDLLExports;
  
  /**
   * Public constructor.
   */
  public MockFunctionProcessor() {
    Excel excel = ExcelFactory.getInstance();
    ExcelFunctionCallHandler excelCallHandler = excel.getExcelCallHandler();
    _mockExcelFunctionRegistry = new MockExcelFunctionRegistry();
    _mockDLLExports = new MockDLLExports(excelCallHandler);
  }
  /**
   * Invoke a worksheet function or command.
   * @param functionName  the name of the function
   * @param args  the arguments to pass to the function
   * @return the result of calling the function
   */
  public XLValue callFunction(final String functionName, final XLValue[] args) {
    FunctionEntry functionEntry = _mockExcelFunctionRegistry.getFunctionEntry(functionName);
    try {
      return (XLValue) functionEntry.getEntryPointMethod().invoke(_mockDLLExports, (Object[]) args);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new Excel4JRuntimeException("Error when invoking " + functionName + " with " + Arrays.toString(args));
    }
  }
}
