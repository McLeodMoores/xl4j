package com.mcleodmoores.excel4j;

/**
 * Factory for getting instances of the Excel interface.  
 * This should not be called directly from application code, instances should be injected as required.
 */
public final class ExcelFactory {
  
  private ExcelFactory() {
  }
  
  /**
   * Bill Pugh singleton pattern helper class removes synchronization requirement.
   */
  private static class COMExcelHelper {
    private static final Excel INSTANCE = new COMExcel();
  }
  
  /**
   * Get an instance of the Excel interface.
   * Thread-safe.
   * @return an instance of the Excel interface.
   */
  public static Excel getInstance() {
    return COMExcelHelper.INSTANCE;
  }
  
  /**
   * Bill Pugh singleton pattern helper class removes synchronization requirement.
   */
  private static class MockExcelHelper {
    private static final Excel INSTANCE = new MockExcel();
  }
  
  /**
   * Get a mock instance of the Excel interface.
   * Thread-safe.
   * @return a mock instance of the Excel interface.
   */
  public static Excel getMockInstance() {
    return MockExcelHelper.INSTANCE;
  }
  
  
}
