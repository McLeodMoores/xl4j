/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.core;

import com.mcleodmoores.xl4j.v1.simulator.SimulatedExcel;
import com.mcleodmoores.xl4j.v1.xll.NativeExcel;

/**
 * Factory for getting instances of the Excel interface. This should not be called directly from application code, instances should be
 * injected as required.
 */
public final class ExcelFactory {
  /** Test mode property name */
  private static final String TEST_PROPERTY_NAME = "test.mode";
  /** True if the code is to be run in test mode */
  private static boolean s_testMode;

  /**
   * The factory.
   */
  private ExcelFactory() {
    final String property = System.getProperty(TEST_PROPERTY_NAME);
    if (property == null) {
      s_testMode = false;
    } else if (property.toUpperCase().charAt(0) == 'T') {
      s_testMode = true;
    } else {
      s_testMode = false;
    }
  }

  /**
   * Bill Pugh singleton pattern helper class removes synchronization requirement.
   */
  private static class NativeExcelHelper {
    /** An instance that communicates with the Excel plug-in */
    private static final Excel INSTANCE = new NativeExcel();
  }

  /**
   * Bill Pugh singleton pattern helper class removes synchronization requirement.
   */
  private static class MockExcelHelper {
    /** An instance of a mock implementation */
    private static final Excel INSTANCE = new SimulatedExcel();
  }

  /**
   * Get an instance of the Excel interface. Thread-safe.
   *
   * @return an instance of the Excel interface.
   */
  public static Excel getInstance() {
    if (s_testMode) {
      return MockExcelHelper.INSTANCE;
    }
    return NativeExcelHelper.INSTANCE;
  }

}
