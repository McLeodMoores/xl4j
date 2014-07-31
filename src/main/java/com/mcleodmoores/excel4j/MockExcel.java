package com.mcleodmoores.excel4j;

import java.io.File;

import com.mcleodmoores.excel4j.heap.WorksheetHeap;
import com.mcleodmoores.excel4j.javacode.InvokerFactory;
import com.mcleodmoores.excel4j.javacode.ReflectiveInvokerFactory;
import com.mcleodmoores.excel4j.mock.MockExcelFunctionRegistry;

/**
 * A mock implementation of the Excel interface for use in testing.
 */
public class MockExcel implements Excel {
  private final WorksheetHeap _heap;
  private final FunctionRegistry _functionRegistry;
  private final ExcelCallback _excelCallback;
  private final ExcelCallHandler _excelCallHandler;
  
  /**
   * Create an instance of the Excel interface suitable for testing.
   */
  public MockExcel() {
    _heap = new WorksheetHeap();
    _functionRegistry = new FunctionRegistry();
    _excelCallHandler = new DefaultExcelCallHandler(_functionRegistry, _heap);
    RawExcelCallback rawCallback = new MockExcelFunctionRegistry();
    _excelCallback = new ExcelCallbackAdapter(getDLLPath(), rawCallback);
  }
  
  @Override
  public byte[] getBinaryName(final long handle, final long length) {
    return new byte[0];
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
  public WorksheetHeap getWorksheetHeap() {
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
  public ExcelCallHandler getExcelCallHandler() {
    return _excelCallHandler;
  }

}
