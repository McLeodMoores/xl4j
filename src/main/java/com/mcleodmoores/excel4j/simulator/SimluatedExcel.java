package com.mcleodmoores.excel4j.simulator;

import java.io.File;

import com.mcleodmoores.excel4j.DefaultExcelFunctionCallHandler;
import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.ExcelFunctionCallHandler;
import com.mcleodmoores.excel4j.FunctionRegistry;
import com.mcleodmoores.excel4j.callback.DefaultExcelCallback;
import com.mcleodmoores.excel4j.callback.ExcelCallback;
import com.mcleodmoores.excel4j.heap.Heap;
import com.mcleodmoores.excel4j.javacode.InvokerFactory;
import com.mcleodmoores.excel4j.javacode.ReflectiveInvokerFactory;
import com.mcleodmoores.excel4j.lowlevel.LowLevelExcelCallback;

/**
 * A mock implementation of the Excel interface for use in testing.
 */
public class SimluatedExcel implements Excel {
  private final Heap _heap;
  private final FunctionRegistry _functionRegistry;
  private final ExcelCallback _excelCallback;
  private final ExcelFunctionCallHandler _excelCallHandler;
  
  /**
   * Create an instance of the Excel interface suitable for testing.
   */
  public SimluatedExcel() {
    _heap = new Heap();
    _functionRegistry = new FunctionRegistry();
    _excelCallHandler = new DefaultExcelFunctionCallHandler(_functionRegistry, _heap);
    LowLevelExcelCallback rawCallback = new MockExcelFunctionRegistry();
    _excelCallback = new DefaultExcelCallback(getDLLPath(), rawCallback);
  }
  
  /**
   * @return the DLL path
   */
  private File getDLLPath() {
    return new File("NOEYEDDEAR");
  }

  @Override
  public InvokerFactory getInvokerFactory() {
    return new ReflectiveInvokerFactory();
  }
  
  @Override
  public Heap getHeap() {
    return _heap;
  }

  @Override
  public FunctionRegistry getFunctionRegistry() {
    return _functionRegistry;
  }

  @Override
  public ExcelCallback getExcelCallback() {
    return _excelCallback;
  }
  
  @Override
  public ExcelFunctionCallHandler getExcelCallHandler() {
    return _excelCallHandler;
  }

}
