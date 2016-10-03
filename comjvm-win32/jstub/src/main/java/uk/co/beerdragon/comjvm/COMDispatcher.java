/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm;

import java.util.Arrays;

import uk.co.beerdragon.comjvm.stub.COMStubClass;
import uk.co.beerdragon.comjvm.util.ClassMethods;

/**
 * Main helper class exposed to the native code. Instances of this are created by
 * {@link COMHostSession#dispatcher} on behalf of the COM host for it to use to construct Java stubs
 * that will marshal calls back to native methods on the host.
 */
public class COMDispatcher<T> {

  private final COMHostSession _session;

  private final int[] _dispIds;

  private final COMStubClass<T> _stub;

  private static int[] dispIds (final ClassMethods methods, final String[] dispSignatures) {
    final int[] dispIds = new int[methods.size ()];
    Arrays.fill (dispIds, -1);
    for (int i = 0; i < dispSignatures.length; i++) {
      final Integer targetId = methods.get (dispSignatures[i]);
      if (targetId != null) {
        dispIds[targetId.intValue ()] = i;
      }
    }
    return dispIds;
  }

  /* package */COMDispatcher (final COMHostSession session, final Class<T> type,
      final String[] dispSignatures) {
    _session = session;
    _stub = COMStubClass.of (type);
    _dispIds = dispIds (_stub.getMethods (), dispSignatures);
  }

  /* package */int[] copyDispIds () {
    return _dispIds.clone ();
  }

  /* package */COMHostSession getSession () {
    return _session;
  }

  /**
   * Creates a new Java stub that will marshal calls to the given object identifier.
   * <p>
   * Before this method returns the reference count on the object will be incremented, at least
   * once, by {@link COMHostSession#addRef}. Any duplication or cloning of the stub may result in
   * additional reference count increases. After the last stub instance, and any clones, have been
   * garbage collected the reference count will have been decremented an equal amount through calls
   * to {@link COMHostSession#release} allowing the COM object to be destroyed if appropriate.
   * 
   * @param objectId
   *          the host allocated object identifier
   * @return the stub instance, never {@code null}
   */
  public T stub (final int objectId) {
    return _stub.instance (this, objectId);
  }

}