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
  
  /**
   * Get an instance of the Excel interface.
   * Thread-safe.
   * @return an instance of the Excel interface.
   */
  public static Excel getInstance() {
    return ExcelHelper.INSTANCE;
  }
  
  /**
   * Look up a binary blob given the Windows HANDLE type and length.  As HANDLE
   * reduces to (void *), it's width is platform dependent and so longs are
   * used here as they should cover both 32-bit and 64-bit use cases.
   * @param handle the Windows HANDLE type, cast to a Java long.
   * @param length the length of the block.
   * @return the binary blob
   */
  public byte[] getBinaryName(final long handle, final long length) {
    // TODO: Call into XLL to lock, copy and unlock handle 
    return new byte[0];
  }
}
