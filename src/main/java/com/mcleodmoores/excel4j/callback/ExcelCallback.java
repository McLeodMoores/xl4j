/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.callback;

import com.mcleodmoores.excel4j.ConstructorDefinition;
import com.mcleodmoores.excel4j.FunctionDefinition;
import com.mcleodmoores.excel4j.lowlevel.LowLevelExcelCallback;


/**
 * High-level Excel callback interface.
 */
public interface ExcelCallback {
  /**
   * Register a function or command with Excel.
   * @param functionDefinition  the function definition, not null
   */
  void registerFunction(final FunctionDefinition functionDefinition);

  void registerConstructor(ConstructorDefinition classDefinition);

  /**
   * Look up a binary blob given the Windows HANDLE type and length.  As HANDLE
   * reduces to (void *), it's width is platform dependent and so longs are
   * used here as they should cover both 32-bit and 64-bit use cases.
   * @param handle the Windows HANDLE type, cast to a Java long.
   * @param length the length of the block.
   * @return the binary blob
   */
  byte[] getBinaryName(final long handle, final long length);

  /**
   * @return the underlying low-level callback interface
   */
  LowLevelExcelCallback getLowLevelExcelCallback();
}
