/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.simulator;

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
import com.mcleodmoores.xl4j.v1.xll.LowLevelExcelCallback;

/**
 * A mock implementation of the Excel interface for use in testing.
 */
public class SimulatedExcel implements Excel {
  private final Heap _heap;
  private final ReflectiveFunctionRegistry _functionRegistry;
  private final ExcelCallback _excelCallback;
  private final ExcelFunctionCallHandler _excelCallHandler;
  private final ReflectiveInvokerFactory _invokerFactory;
  private final TypeConverterRegistry _typeConverterRegistry;
  private final MockExcelFunctionEntryAccumulator _rawCallback;

  /**
   * Create an instance of the Excel interface suitable for testing.
   */
  public SimulatedExcel() {
    _heap = new ConcurrentHeap();
    _typeConverterRegistry = new CachingTypeConverterRegistry(new ScanningTypeConverterRegistry(this));
    _invokerFactory = new ReflectiveInvokerFactory(this, _typeConverterRegistry);
    _functionRegistry = new ReflectiveFunctionRegistry(_invokerFactory);
    _excelCallHandler = new DefaultExcelFunctionCallHandler(_functionRegistry, _heap);
    _rawCallback = new MockExcelFunctionEntryAccumulator();
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

}