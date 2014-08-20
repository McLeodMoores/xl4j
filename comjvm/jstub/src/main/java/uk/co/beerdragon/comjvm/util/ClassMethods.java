/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.generic.Type;

/**
 * Helper for querying the method definitions in a class or interface.
 * <p>
 * This contains the principal logic for allocating local dispatch identifiers. Methods are gathered
 * in a deterministic fashion and the corresponding index is part of the metadata returned. An array
 * of integers can then be allocated, with each slot corresponding to a Java class or interface
 * method that is being stubbed, and the value in each slot the target dispatch identifier for an
 * underlying COM implementation.
 */
public class ClassMethods {

  /**
   * Method metadata representation. This is the union of the information available through
   * reflection and the dispatch index calculated by the internal algorithm.
   */
  public static final class MethodInfo {

    private final Method _method;

    private final Integer _index;

    private MethodInfo (final Method method, final int index) {
      _method = method;
      _index = index;
    }

    /**
     * Returns the reflection metadata for the method.
     * 
     * @return the metadata, never {@code null}
     */
    public Method getMethod () {
      return _method;
    }

    /**
     * Returns the deterministic index for the method.
     * 
     * @return the index
     */
    public Integer getIndex () {
      return _index;
    }

  }

  private final Map<String, Object> _methods = new HashMap<String, Object> ();

  private static void gatherMethods (final Class<?> type, final Map<String, Object> signatures) {
    for (final Class<?> iface : type.getInterfaces ()) {
      gatherMethods (iface, signatures);
    }
    if (type.getSuperclass () != null) {
      gatherMethods (type.getSuperclass (), signatures);
    } else {
      if (type.isInterface ()) {
        gatherMethods (Object.class, signatures);
      }
    }
    for (final Method method : type.getDeclaredMethods ()) {
      final int modifiers = method.getModifiers ();
      if (Modifier.isStatic (modifiers)
          || !(Modifier.isPublic (modifiers) || Modifier.isProtected (modifiers))) {
        continue;
      }
      final String signature = method.getName () + Type.getSignature (method);
      if (Modifier.isFinal (modifiers)) {
        signatures.remove (signature);
      } else {
        signatures.put (signature, method);
      }
    }
  }

  /**
   * Creates a new instance.
   * 
   * @param type
   *          the class or interface to examine, not {@code null}
   */
  public ClassMethods (final Class<?> type) {
    gatherMethods (type, _methods);
    final String[] targetSignatures = _methods.keySet ().toArray (new String[_methods.size ()]);
    Arrays.sort (targetSignatures);
    for (int i = 0; i < targetSignatures.length; i++) {
      _methods.put (targetSignatures[i], new MethodInfo (
          (Method)_methods.get (targetSignatures[i]), i));
    }
  }

  /**
   * Counts the number of defined methods. The methods will have local dispatch indices from 0
   * (inclusive) to this value (exclusive).
   * 
   * @return the number of methods
   */
  public int size () {
    return _methods.size ();
  }

  /**
   * Returns the local index for the given method signature.
   * 
   * @param signature
   *          the method signature to check, not {@code null}
   * @return the index, or {@code null} if the signature if not recognised
   */
  public Integer get (final String signature) {
    final Object info = _methods.get (signature);
    if (info != null) {
      return ((MethodInfo)info).getIndex ();
    } else {
      return null;
    }
  }

  /**
   * Returns the collection of all methods. The ordering of the methods is not defined; use
   * {@link MethodInfo#getIndex} to determine where in the local dispatch array it occurs.
   * 
   * @return all methods, never {@code null}
   */
  @SuppressWarnings ({ "rawtypes", "unchecked" })
  public Iterable<MethodInfo> all () {
    return (Collection)_methods.values ();
  }

}