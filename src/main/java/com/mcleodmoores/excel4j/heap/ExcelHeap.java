package com.mcleodmoores.excel4j.heap;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;

/**
 * Class to store objects and allocate handles for objects.
 */
public enum ExcelHeap {
  /**
   * Singleton instance.
   */
  INSTANCE;
  
  private ConcurrentHashMap<Long, Object> _handleToObj;
  private ConcurrentHashMap<Object, Long> _objToHandle;
  
  private AtomicLong _sequence = new AtomicLong(System.currentTimeMillis());
  private HashSet<Long> _keySnap;
  
  /**
   * Constructor.
   */
  ExcelHeap() {
    _handleToObj = new ConcurrentHashMap<Long, Object>();
    _objToHandle = new ConcurrentHashMap<Object, Long>();
  }
  
  /**
   * Get a handle for an object, or allocate one if not currently in the heap.
   * @param object the object to store
   * @return the object's handle
   */
  public long getHandle(final Object object) {
    Long key = _objToHandle.get(object);
    if (key != null) {
      return key;
    } else {
      long newKey = _sequence.getAndIncrement();
      synchronized (this) {
        _handleToObj.put(newKey, object);
        _objToHandle.put(object, newKey);
      }
      return newKey;
    }
  }
  
  /**
   * Get an object, given the handle.
   * @param handle the handle for the object
   * @return the object referred to by the handle
   */
  public Object getObject(final long handle) {
    Object object = _handleToObj.get(handle);
    if (object != null) {
      return object;
    } else {
      throw new Excel4JRuntimeException("Cannot find object with handle " + handle);
    }
  }
  
  /**
   * Start a garbage collection pass.
   */
  public void startGC() {
    _keySnap = new HashSet<Long>(_handleToObj.keySet());
  }
  
  /**
   * Mark an object as being live.
   * @param handle the handle to mark as live
   */
  public void markObjectAsLive(final long handle) {
    _keySnap.remove(handle);
  }
  
  /**
   * Remove any objects that aren't live, minimizing locking period.
   */
  public void endGC() {
    for (long handle : _keySnap) {
      Object o = _handleToObj.get(handle);
      synchronized (this) {
        _handleToObj.remove(handle);
        _objToHandle.remove(o);
      }
    }
  }
  
}
