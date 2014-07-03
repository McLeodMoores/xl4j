package com.mcleodmoores.excel4j;

/**
 * General interface to Excel. 
 * Call getInstance() to get a (thread-safe) instance.
 */
public final class Excel {

  private Excel() {
  }
  
  /**
   * Bill Pugh singleton pattern helper class removes synchronization requirement.
   */
  private static class ExcelHelper {
    private static final Excel INSTANCE = new Excel();
  }
  
  public static Excel getInstance() {
    return ExcelHelper.INSTANCE;
  }
  
  public byte[] getBinaryName(final long handle, final long length) {
    // TODO: Call into XLL to lock, copy and unlock handle 
    return new byte[0];
  }
}
