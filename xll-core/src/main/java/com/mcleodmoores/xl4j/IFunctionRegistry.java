/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import com.mcleodmoores.xl4j.callback.ExcelCallback;

/**
 *
 */
public interface IFunctionRegistry {

  void registerFunctions(ExcelCallback callback);

  FunctionDefinition getFunctionDefinition(int exportNumber);
}
