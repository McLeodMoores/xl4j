package com.mcleodmoores.excel4j;

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
}
