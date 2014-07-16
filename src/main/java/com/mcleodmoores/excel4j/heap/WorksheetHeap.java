package com.mcleodmoores.excel4j.heap;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLSheetId;

/**
 * Class to store objects and allocate handles for objects.
 */
public class WorksheetHeap {
  
  private ConcurrentHashMap<Long, Object> _handleToObj;
  private ConcurrentHashMap<Object, Long> _objToHandle;
  
  private AtomicLong _sequence;
  private HashSet<Long> _keySnap;
  private XLSheetId _sheetId;
  
  /**
   * Construct a worksheet heap.
   * TODO: Need some sort of check-pointing as current GC won't work without freezing sheet operations.
   * @param sheetId the worksheet id that this heap is for
   */
  public WorksheetHeap(final XLSheetId sheetId) {
    _sheetId = sheetId;
    _handleToObj = new ConcurrentHashMap<Long, Object>();
    _objToHandle = new ConcurrentHashMap<Object, Long>();
    // we try and create the handle counter by combining the local MAC, the time and the sheet id.
    // this should minimize the possibility of stale handles in sheets being interpreted as valid.
    long baseHandle;
    Enumeration<NetworkInterface> networkInterfaces;
    try {
      networkInterfaces = NetworkInterface.getNetworkInterfaces();
      if (networkInterfaces.hasMoreElements()) {
        NetworkInterface networkInterface = networkInterfaces.nextElement();
        byte[] hardwareAddress = networkInterface.getHardwareAddress();
        byte[] extendedTo64bits = new byte[8];
        // we assume the hardware address is going to be 6 bytes, but we handle if it isn't, but top out at 8 bytes
        System.arraycopy(hardwareAddress, 0, extendedTo64bits, 0, Math.min(hardwareAddress.length, 8));
        ByteBuffer byteBuffer = ByteBuffer.wrap(extendedTo64bits);
        baseHandle = byteBuffer.getLong();
      } else {
        baseHandle = new SecureRandom().nextLong();
      }
    } catch (SocketException e) {
      baseHandle = new SecureRandom().nextLong();
    }
    baseHandle += System.currentTimeMillis() / 1000; // we only need seconds.
    baseHandle += sheetId.getSheetId() * (2 ^ 16); // include the sheet id as well.
    _sequence = new AtomicLong(baseHandle);
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
      synchronized (object) { // should be low contention at least, can we get rid of this lock?
        // check no one snuck in while we were waiting with the same object.
        Long keyAgain = _objToHandle.get(object);
        if (keyAgain != null) {  
          return keyAgain;
        }
        long newKey = _sequence.getAndIncrement();
        // we don't need locking here because no one has the key yet.
        // theoretically the user passing getHandle could concurrently call it twice with the same object, but
        // that's why we synchronize on object and re-check once we have the lock.
        _handleToObj.put(newKey, object);
        _objToHandle.put(object, newKey);
        return newKey;
      }
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
   * @return the sheet id
   */
  public XLSheetId getSheetId() {
    return _sheetId;
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
      // We don't need locking here because no one is going to look these up because they're garbage!
      _handleToObj.remove(handle);
      _objToHandle.remove(o);
    }
  }
  
}
