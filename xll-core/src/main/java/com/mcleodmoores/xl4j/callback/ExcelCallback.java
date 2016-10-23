/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.callback;

import com.mcleodmoores.xl4j.ClassConstructorDefinition;
import com.mcleodmoores.xl4j.ClassMethodDefinition;
import com.mcleodmoores.xl4j.ConstructorDefinition;
import com.mcleodmoores.xl4j.FunctionDefinition;
import com.mcleodmoores.xl4j.lowlevel.LowLevelExcelCallback;

/**
 * High-level Excel callback interface.
 */
public interface ExcelCallback {

  /**
   * Register a function or command with Excel.
   * 
   * @param functionDefinition
   *          the function definition, not null
   */
  void registerFunction(final FunctionDefinition functionDefinition);

  /**
   * Register a constructor with Excel.
   * 
   * @param classDefinition
   *          the class definition, not null
   */
  void registerConstructor(ConstructorDefinition classDefinition);

  /**
   * Register all visible constructors in a class with Excel.
   * 
   * @param classDefinition
   *          the class definition, not null
   */
  void registerConstructorsForClass(ClassConstructorDefinition classDefinition);

  /**
   * Register all visible methods in a class with Excel.
   * 
   * @param classDefinition
   *          the class definition, not null
   */
  void registerMethodsForClass(ClassMethodDefinition classDefinition);

  /**
   * Look up a binary blob given the Windows HANDLE type and length. As HANDLE reduces to (void *), it's width is platform dependent and so
   * longs are used here as they should cover both 32-bit and 64-bit use cases.
   * 
   * @param handle
   *          the Windows HANDLE type, cast to a Java long.
   * @param length
   *          the length of the block.
   * @return the binary blob
   */
  byte[] getBinaryName(final long handle, final long length);

  /**
   * @return the underlying low-level callback interface
   */
  LowLevelExcelCallback getLowLevelExcelCallback();
}
