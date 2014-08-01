package com.mcleodmoores.excel4j.xll;

import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.ExcelFunctionCallHandler;
import com.mcleodmoores.excel4j.FunctionRegistry;
import com.mcleodmoores.excel4j.callback.ExcelCallback;
import com.mcleodmoores.excel4j.heap.Heap;
import com.mcleodmoores.excel4j.javacode.InvokerFactory;

/**
 * Implementation of Excel interface that actually communicates with the XLL plug-in.
 */
public class NativeExcel implements Excel {

  @Override
  public InvokerFactory getInvokerFactory() {
    return null;
  }

  @Override
  public Heap getHeap() {
    return null;
  }

  @Override
  public FunctionRegistry getFunctionRegistry() {
    return null;
  }

  @Override
  public ExcelCallback getExcelCallback() {
    return null;
  }

  @Override
  public ExcelFunctionCallHandler getExcelCallHandler() {
    return null;
  }
}
