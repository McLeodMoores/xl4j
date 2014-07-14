package com.mcleodmoores.excel4j;

import com.mcleodmoores.excel4j.javacode.InvokerFactory;

/**
 * Implementation of Excel interface that actually communicates with the XLL plug-in.
 */
public class COMExcel implements Excel {
  @Override
  public byte[] getBinaryName(final long handle, final long length) {
    return null;
  }

  @Override
  public InvokerFactory getInvokerFactory() {
    return null;
  }
}
