package com.mcleodmoores.excel4j.heap;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.excel4j.util.ConcurrentIdentityHashMap;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;

/**
 * Class to store objects and allocate handles for objects.
 */
public class Heap {
  private static final Logger LOGGER = LoggerFactory.getLogger(Heap.class);

  private static final int MILLIS_PER_SECOND = 1000;
  private static final int BYTES_IN_64BITS = 8;
  private final ConcurrentHashMap<Long, Object> _handleToObj;
  private final ConcurrentIdentityHashMap<Object, Long> _objToHandle;

  private final AtomicLong _sequence;
  private long _snapHandle;

  //TODO: Need some sort of check-pointing as current GC won't work without freezing sheet operations. #44
  /**
   * Construct a heap.
   */
  public Heap() {
    _handleToObj = new ConcurrentHashMap<>();
    _objToHandle = new ConcurrentIdentityHashMap<>();
    // we try and create the handle counter by combining the local MAC, the time and the sheet id.
    // this should minimize the possibility of stale handles in sheets being interpreted as valid.
    long baseHandle;
    Enumeration<NetworkInterface> networkInterfaces;
    try {
      networkInterfaces = NetworkInterface.getNetworkInterfaces();
      if (networkInterfaces.hasMoreElements()) {
        final NetworkInterface networkInterface = networkInterfaces.nextElement();
        final byte[] hardwareAddress = networkInterface.getHardwareAddress();
        final byte[] extendedTo64bits = new byte[BYTES_IN_64BITS];
        if (hardwareAddress != null) {
          // we assume the hardware address is going to be 6 bytes, but we handle if it isn't, but top out at 8 bytes
          System.arraycopy(hardwareAddress, 0, extendedTo64bits, 0, Math.min(hardwareAddress.length, BYTES_IN_64BITS));
          final ByteBuffer byteBuffer = ByteBuffer.wrap(extendedTo64bits);
          baseHandle = byteBuffer.getLong();
        } else {
          baseHandle = new SecureRandom().nextLong();
        }
      } else {
        baseHandle = new SecureRandom().nextLong();
      }
    } catch (final SocketException e) {
      baseHandle = new SecureRandom().nextLong();
    }
    baseHandle += System.currentTimeMillis() / MILLIS_PER_SECOND; // we only need seconds.
    _sequence = new AtomicLong(baseHandle);
  }

  /**
   * Get a handle for an object, or allocate one if not currently in the heap.
   * @param object the object to store
   * @return the object's handle
   */
  public long getHandle(final Object object) {
    final Long key = _objToHandle.get(object);
    if (key != null) {
      return key;
    } else {
      synchronized (object) { // should be low contention at least, can we get rid of this lock?
        // check no one snuck in while we were waiting with the same object.
        final Long keyAgain = _objToHandle.get(object);
        if (keyAgain != null) {
          return keyAgain;
        }
        final long newKey = _sequence.getAndIncrement();
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
    final Object object = _handleToObj.get(handle);
    if (object != null) {
      return object;
    } else {
      throw new Excel4JRuntimeException("Cannot find object with handle " + handle);
    }
  }

  /**
   * Start a garbage collection pass.
   */
  private void startGC() {
    _snapHandle = _sequence.get();
  }

  /**
   * Remove any objects that aren't live, minimizing locking period.
   */
  private void endGC(final long[] activeHandles) {
    Arrays.sort(activeHandles);
    final Iterator<Entry<Long, Object>> iterator = _handleToObj.entrySet().iterator();
    long removed = 0;
    while (iterator.hasNext()) {
      final Entry<Long, Object> next = iterator.next();
      if (next.getKey() >= _snapHandle) {
        continue; // skip as we might have missed it in our scan because it was created after we started
      }
      if (Arrays.binarySearch(activeHandles, next.getKey()) < 0) {
        iterator.remove(); // didn't find handle, meaning it's not active, gc.
        _objToHandle.remove(next.getValue());
        removed++;
      }
    }
    LOGGER.info(removed + " objects removed during GC pass");
  }

  /**
   *
   * @param activeHandles  list of identifiers for objects that have been seen since the last snap
   * @return the number of handles created since the last snap, gives measure of churn to adjust GC frequency
   */
  public long cycleGC(final long[] activeHandles) {
    final long snapBefore = _snapHandle;
    endGC(activeHandles);
    startGC();
    return _snapHandle - snapBefore; // object allocated during cycle
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("WorksheetHeap[\n");
    for (final Entry<Long, Object> entry : _handleToObj.entrySet()) {
      final String number = Long.toString(entry.getKey());
      sb.append("  ");
      sb.append(number);
      sb.append(" = > ");
      sb.append(entry.getValue().toString());
      sb.append("\n");
    }
    sb.append("]");
    return sb.toString();
  }
}
