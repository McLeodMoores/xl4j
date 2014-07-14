package com.mcleodmoores.excel4j;

import com.mcleodmoores.excel4j.javacode.InvokerFactory;

/**
 * Interface to provide access to Excel services and callbacks.
 */
public interface Excel {
  /**
   * Look up a binary blob given the Windows HANDLE type and length.  As HANDLE
   * reduces to (void *), it's width is platform dependent and so longs are
   * used here as they should cover both 32-bit and 64-bit use cases.
   * @param handle the Windows HANDLE type, cast to a Java long.
   * @param length the length of the block.
   * @return the binary blob
   */
  byte[] getBinaryName(final long handle, final long length);
  /**
   * Get the local invoker factory to allow Excel types to be bound to java constructors, methods and fields.
   * @return an instance of an invoker factory
   */
  InvokerFactory getInvokerFactory();
}
