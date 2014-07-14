package com.mcleodmoores.excel4j;

import com.mcleodmoores.excel4j.javacode.InvokerFactory;
import com.mcleodmoores.excel4j.javacode.ReflectiveInvokerFactory;

/**
 * A mock implementation of the Excel interface for use in testing.
 */
public class MockExcel implements Excel {
  
  @Override
  public byte[] getBinaryName(final long handle, final long length) {
    return new byte[0];
  }

  @Override
  public InvokerFactory getInvokerFactory() {
    return new ReflectiveInvokerFactory();
  }

}
