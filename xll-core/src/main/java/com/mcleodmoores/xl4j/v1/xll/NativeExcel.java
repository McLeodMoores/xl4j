/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.xll;

import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.core.ExcelCallback;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFunctionCallHandler;
import com.mcleodmoores.xl4j.v1.api.core.FunctionRegistry;
import com.mcleodmoores.xl4j.v1.api.core.Heap;
import com.mcleodmoores.xl4j.v1.api.core.InvokerFactory;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.xl4j.v1.core.ConcurrentHeap;
import com.mcleodmoores.xl4j.v1.core.DefaultExcelCallback;
import com.mcleodmoores.xl4j.v1.core.DefaultExcelFunctionCallHandler;
import com.mcleodmoores.xl4j.v1.core.ReflectiveFunctionRegistry;
import com.mcleodmoores.xl4j.v1.invoke.ReflectiveInvokerFactory;
import com.mcleodmoores.xl4j.v1.typeconvert.CachingTypeConverterRegistry;
import com.mcleodmoores.xl4j.v1.typeconvert.ScanningTypeConverterRegistry;

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
   * Create an instance of the Excel interface suitable that gets called by native code.
   */
  public NativeExcel() {
    _heap = new ConcurrentHeap();
    _typeConverterRegistry = new CachingTypeConverterRegistry(new ScanningTypeConverterRegistry(this));
    _invokerFactory = new ReflectiveInvokerFactory(this, _typeConverterRegistry);
    _functionRegistry = new ReflectiveFunctionRegistry(_invokerFactory);
    _excelCallHandler = new DefaultExcelFunctionCallHandler(_functionRegistry, _heap);
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
  public TypeConverterRegistry getTypeConverterRegistry() {
    return _typeConverterRegistry;
  }
}
