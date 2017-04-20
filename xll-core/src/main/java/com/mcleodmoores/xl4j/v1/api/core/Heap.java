package com.mcleodmoores.xl4j.v1.api.core;

public interface Heap {

  /**
   * Get a handle for an object, or allocate one if not currently in the heap.
   *
   * @param object
   *          the object to store
   * @return the object's handle
   */
  long getHandle(Object object);

  /**
   * Get an object, given the handle.
   *
   * @param handle
   *          the handle for the object
   * @return the object referred to by the handle
   */
  Object getObject(long handle);

  /**
   *
   * @param activeHandles
   *          list of identifiers for objects that have been seen since the last snap
   * @return the number of handles created since the last snap, gives measure of churn to adjust GC frequency
   */
  long cycleGC(long[] activeHandles);

}