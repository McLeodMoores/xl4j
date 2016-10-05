package com.mcleodmoores.excel4j.simulator;

import com.mcleodmoores.excel4j.DefaultExcelConstructorCallHandler;
import com.mcleodmoores.excel4j.DefaultExcelFunctionCallHandler;
import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.ExcelConstructorCallHandler;
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
 * A mock implementation of the Excel interface for use in testing.
 */
public class SimulatedExcel implements Excel {
  private final Heap _heap;
  private final FunctionRegistry _functionRegistry;
  private final ExcelCallback _excelCallback;
  private final ExcelFunctionCallHandler _excelCallHandler;
  private final ExcelConstructorCallHandler _excelConstructorCallHandler;
  private final ReflectiveInvokerFactory _invokerFactory;
  private final TypeConverterRegistry _typeConverterRegistry;
  private final MockExcelFunctionRegistry _rawCallback;

  /**
   * Create an instance of the Excel interface suitable for testing.
   */
  public SimulatedExcel() {
    _heap = new Heap();
    _typeConverterRegistry = new CachingTypeConverterRegistry(new ScanningTypeConverterRegistry(this));
    _invokerFactory = new ReflectiveInvokerFactory(this, _typeConverterRegistry);
    _functionRegistry = new FunctionRegistry(_invokerFactory);
    _excelCallHandler = new DefaultExcelFunctionCallHandler(_functionRegistry, _heap);
    _excelConstructorCallHandler = new DefaultExcelConstructorCallHandler(_functionRegistry);
    _rawCallback = new MockExcelFunctionRegistry();
    _excelCallback = new DefaultExcelCallback(_rawCallback);
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

  @Override
  public ExcelFunctionCallHandler getExcelCallHandler() {
    return _excelCallHandler;
  }

  @Override
  public TypeConverterRegistry getTypeConverterRegistry() {
    return _typeConverterRegistry;
  }

  @Override
  public LowLevelExcelCallback getLowLevelExcelCallback() {
    return _rawCallback;
  }

  @Override
  public ExcelConstructorCallHandler getExcelConstructorCallHandler() {
    return _excelConstructorCallHandler;
  }
}