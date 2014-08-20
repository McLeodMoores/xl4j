/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.stub;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.ObjectUtils;

import uk.co.beerdragon.comjvm.COMDispatcher;
import uk.co.beerdragon.comjvm.COMHostSession;
import uk.co.beerdragon.comjvm.util.ClassMethods;

/**
 * Wrapper for Java class/interface stub implementations. This acts as a factory to create instances
 * which can communicate with the underlying COM object in the host environment.
 * 
 * @param <T>
 *          the stubbed class or interface
 */
public final class COMStubClass<T> {

  @SuppressWarnings ("rawtypes")
  private static final Class[] STUB_CONSTRUCTOR_PARAMETERS = new Class[] { COMHostSession.class,
      int[].class, Integer.TYPE };

  /**
   * Cache of stub types.
   */
  private static final ConcurrentMap<Class<?>, COMStubClass<?>> s_stubClasses = new ConcurrentHashMap<Class<?>, COMStubClass<?>> ();

  private final ClassMethods _methods;

  private final Constructor<? extends T> _stub;

  private COMStubClass (final Class<T> clazz) {
    _methods = new ClassMethods (clazz);
    final Class<? extends T> stubClass = StubBuilder.of (clazz).build (_methods.all ());
    try {
      _stub = stubClass.getConstructor (STUB_CONSTRUCTOR_PARAMETERS);
    } catch (final Exception e) {
      throw new IllegalArgumentException ("Can't stub '" + clazz + "'", e);
    }
  }

  /**
   * Obtains a new instance. An internal cache is used so that only the first request for each class
   * type results in the overhead of a stub class being generated.
   * 
   * @param clazz
   *          the class or interface to stub, not {@code null}
   * @return the stub wrapper, never {@code null}
   */
  @SuppressWarnings ("unchecked")
  public static <T> COMStubClass<T> of (final Class<T> clazz) {
    COMStubClass<?> stub = s_stubClasses.get (clazz);
    if (stub == null) {
      stub = new COMStubClass<T> (clazz);
      stub = ObjectUtils.defaultIfNull (s_stubClasses.putIfAbsent (clazz, stub), stub);
    }
    return (COMStubClass<T>)stub;
  }

  /**
   * Queries the methods defined on the stub.
   * 
   * @return the method definitions, never {@code null}
   */
  public ClassMethods getMethods () {
    return _methods;
  }

  /**
   * Creates a new stub instance.
   * 
   * @param dispatcher
   *          the callback route to the COM host environment, never {@code null}
   * @param objectId
   *          the host allocated object identifier
   * @return the stub instance, never {@code null}
   */
  public T instance (final COMDispatcher<T> dispatcher, final int objectId) {
    // TODO
    throw new UnsupportedOperationException ();
  }

}