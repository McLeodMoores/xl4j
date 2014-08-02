/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.simulator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * 
 */
public class MockFunctionProcessor {
  private final Excel _excel = ExcelFactory.getInstance();
  
  public MockFunctionProcessor() {
    _excel.getFunctionRegistry().registerFunctions(_excel.getExcelCallback());
  }
  /**
   * Invoke a function.
   * @param functionName the name of the function
   * @param args the arguments
   * @return the result
   */
  public XLValue invoke(final String functionName, final XLValue... args) {
    MockExcelFunctionRegistry excelFunctionRegistry = new MockExcelFunctionRegistry();
    FunctionEntry functionEntry = excelFunctionRegistry.getFunctionEntry(functionName);
    Object dllTable = new MockDLLExports(_excel.getExcelCallHandler());
    Method entryPointMethod = functionEntry.getEntryPointMethod();
    try {
      return (XLValue) entryPointMethod.invoke(dllTable, (Object[]) args);
    } catch (IllegalAccessException | IllegalArgumentException | ClassCastException | InvocationTargetException e) {
      throw new Excel4JRuntimeException("Problem invoking function " + functionName + " with args " + Arrays.toString(args), e);
    }
  }
}
