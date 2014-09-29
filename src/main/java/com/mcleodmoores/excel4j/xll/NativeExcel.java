package com.mcleodmoores.excel4j.xll;

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
import com.mcleodmoores.excel4j.typeconvert.CachingTypeConverterRegistry;
import com.mcleodmoores.excel4j.typeconvert.ScanningTypeConverterRegistry;
import com.mcleodmoores.excel4j.typeconvert.TypeConverterRegistry;

/**
 * Implementation of Excel interface that actually communicates with the XLL plug-in.
 */
public class NativeExcel implements Excel {
  private final Heap _heap;
  private final FunctionRegistry _functionRegistry;
  private final ExcelCallback _excelCallback;
  private final ExcelFunctionCallHandler _excelCallHandler;
  private final ReflectiveInvokerFactory _invokerFactory;
  private final TypeConverterRegistry _typeConverterRegistry;
  private final XLLAccumulatingFunctionRegistry _rawCallback;
  
  /**
   * Create an instance of the Excel interface suitable for testing.
   */
  public NativeExcel() {
    _heap = new Heap();
    _typeConverterRegistry = new CachingTypeConverterRegistry(new ScanningTypeConverterRegistry(this));
    _invokerFactory = new ReflectiveInvokerFactory(this, _typeConverterRegistry);
    _functionRegistry = new FunctionRegistry(_invokerFactory);
    _excelCallHandler = new DefaultExcelFunctionCallHandler(_functionRegistry, _heap);
    _rawCallback = new XLLAccumulatingFunctionRegistry();
    _excelCallback = new DefaultExcelCallback(getDLLPath(), _rawCallback);
  }
  
  /**
   * @return the DLL path
   */
  private File getDLLPath() {
    return new File("NOEYEDDEAR");
  }
  
  @Override
  public InvokerFactory getInvokerFactory() {
    return _invokerFactory;
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
  
  /**
   * Get the low level callback handler so we can get accumulated registrations without callbacks.
   * @return the low level callback accumulator
   */
  public LowLevelExcelCallback getLowLevelExcelCallback() {
    return _rawCallback;
  }
  
  @Override
  public ExcelFunctionCallHandler getExcelCallHandler() {
    return _excelCallHandler;
  }

  @Override
  public TypeConverterRegistry getTypeConverterRegistry() {
    return _typeConverterRegistry;
  }
}
