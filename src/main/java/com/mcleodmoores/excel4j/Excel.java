package com.mcleodmoores.excel4j;

public class Excel {

  private Excel() {
  }
  
  /**
   * Bill Pugh singleton pattern helper class removes synchronization requirement.
   */
  private static class ExcelHelper {
    private static final Excel s_instance = new Excel();
  }
  
  public static Excel getInstance() {
    return ExcelHelper.s_instance;
  }
  
  public byte[] getBinaryName(long handle, long length) {
    // TODO: Call into XLL to lock, copy and unlock handle 
    return new byte[0];
  }
}
