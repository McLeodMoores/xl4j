/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

/**
 * Factory for different type of Excel interface.
 */
public final class ExcelFactory {
  
  private ExcelFactory() {
  }
  /**
   * Helper class avoids need for synchronization lock.
   */
  private static final class TestExcelSingletonHelper {
    private static final Excel INSTANCE = new ExcelTest();
  }
  
  /**
   * Get a mock version of the Excel interface, for testing.
   * @return a mock implementation of the Excel interface
   */
  public static Excel getMockInstance() {
    return TestExcelSingletonHelper.INSTANCE;
  }
}
