/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm;

/**
 * The COM {@code IJvm} instance managing this JVM. Typically there is a single implementation of
 * this that makes native calls to the COM object that loaded this JVM in process. In the case of
 * there being multiple application clients, they will typically share the {@code IJvm} instance.
 * Alternative {@code IJvmConnector} implementations are available which might result in multiple
 * {@code IJvm} instances in different applications which are all making calls to a single physical
 * JVM; this will result in multiple {@code COMJvmSession} instances, one for each of the
 * {@code IJvm} instances.
 */
public abstract class COMJvmSession {

  /**
   * Creates a new session instance.
   */
  protected COMJvmSession () {
  }

  /**
   * Creates a COM object reference for use by Java methods.
   * <p>
   * The {@code IUnknown} should have its reference count incremented before this call to keep the
   * object alive while the Java object is active. When the Java stub is garbage collected it will
   * make a call to {@link #release} to allow the underlying object to be discarded if appropriate.
   * 
   * @param index
   *          object index, as managed by the {@code IJvm}. Negative values indicate a {@code null}
   *          reference.
   * @return a stub object referencing the underlying {@code IUnknown}, or {@code null} if the index
   *         is negative
   */
  protected final COMObject object (final int index) {
    if (index >= 0) {
      return new COMObject (index, this);
    } else {
      return null;
    }
  }

  /**
   * Decrements the reference count on the underlying {@code IUnknown} following garbage collection
   * of a {@link COMObject}.
   * 
   * @param object
   *          the object index
   */
  protected abstract void release (int object);

}