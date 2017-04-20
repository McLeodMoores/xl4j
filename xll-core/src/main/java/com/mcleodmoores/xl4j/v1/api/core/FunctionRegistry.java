/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.core;

/**
 * Common interface for function registries.
 */
public interface FunctionRegistry {
  /**
   * Provides the registry with a callback interface that it should callback on to register each function
   * it contains.
   * @param callback  the callback object, on which to call register
   */
  void registerFunctions(ExcelCallback callback);

  /**
   * Get a function definition for a particular export number
   * @param exportNumber  the ordinal export number assigned to the function we're interested in. 
   * @return the function definition for the provided export number
   */
  FunctionDefinition getFunctionDefinition(int exportNumber);
}
