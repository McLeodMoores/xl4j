/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.xll;

import com.mcleodmoores.xl4j.DefaultExcelConstructorCallHandler;
import com.mcleodmoores.xl4j.DefaultExcelFunctionCallHandler;
import com.mcleodmoores.xl4j.Excel;
import com.mcleodmoores.xl4j.ExcelConstructorCallHandler;
import com.mcleodmoores.xl4j.ExcelFunctionCallHandler;
import com.mcleodmoores.xl4j.FunctionRegistry;
import com.mcleodmoores.xl4j.callback.DefaultExcelCallback;
import com.mcleodmoores.xl4j.callback.ExcelCallback;
import com.mcleodmoores.xl4j.heap.Heap;
import com.mcleodmoores.xl4j.javacode.InvokerFactory;
import com.mcleodmoores.xl4j.javacode.ReflectiveInvokerFactory;
import com.mcleodmoores.xl4j.lowlevel.LowLevelExcelCallback;
import com.mcleodmoores.xl4j.typeconvert.CachingTypeConverterRegistry;
import com.mcleodmoores.xl4j.typeconvert.ScanningTypeConverterRegistry;
import com.mcleodmoores.xl4j.typeconvert.TypeConverterRegistry;

/**
 * Implementation of Excel interface that actually communicates with the XLL plug-in.
 */
public class NativeExcel implements Excel {
  private final Heap _heap;
  private final FunctionRegistry _functionRegistry;
  private final ExcelCallback _excelCallback;
  private final ExcelFunctionCallHandler _excelCallHandler;
  private final ExcelConstructorCallHandler _excelConstructorCallHandler;
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
    _excelConstructorCallHandler = new DefaultExcelConstructorCallHandler(_functionRegistry);
    _rawCallback = new XLLAccumulatingFunctionRegistry();
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

  /**
   * Get the low level callback handler so we can get accumulated registrations without callbacks.
   * 
   * @return the low level callback accumulator
   */
  @Override
  public LowLevelExcelCallback getLowLevelExcelCallback() {
    return _rawCallback;
  }

  @Override
  public ExcelFunctionCallHandler getExcelCallHandler() {
    return _excelCallHandler;
  }

  @Override
  public ExcelConstructorCallHandler getExcelConstructorCallHandler() {
    return _excelConstructorCallHandler;
  }

  @Override
  public TypeConverterRegistry getTypeConverterRegistry() {
    return _typeConverterRegistry;
  }
}
