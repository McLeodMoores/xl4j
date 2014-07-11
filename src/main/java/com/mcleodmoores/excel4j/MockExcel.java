package com.mcleodmoores.excel4j;

/**
 * A mock implementation of the Excel interface for use in testing.
 */
public class MockExcel implements Excel {
  
  @Override
  public byte[] getBinaryName(final long handle, final long length) {
    return new byte[0];
  }

}
