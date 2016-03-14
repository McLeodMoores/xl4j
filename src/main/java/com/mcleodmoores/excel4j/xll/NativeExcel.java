package com.mcleodmoores.excel4j.xll;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.FileAppender;

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
//    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
//    FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
//    fileAppender.setContext((Context) LoggerFactory.getILoggerFactory());
//    fileAppender.setName("timestamp");
//    // set the file name
//    fileAppender.setFile("log" + System.currentTimeMillis() + ".log");
//
//    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
//    encoder.setContext((Context) LoggerFactory.getILoggerFactory());
//    encoder.setPattern("%r %thread %level - %msg%n");
//    encoder.start();
//
//    fileAppender.setEncoder(encoder);
//    fileAppender.start();
//    root.addAppender(fileAppender);
    
    _heap = new Heap();
    _typeConverterRegistry = new CachingTypeConverterRegistry(new ScanningTypeConverterRegistry(this));
    _invokerFactory = new ReflectiveInvokerFactory(this, _typeConverterRegistry);
    _functionRegistry = new FunctionRegistry(_invokerFactory);
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
