/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm;

import org.apache.commons.lang3.ArrayUtils;

import uk.co.beerdragon.comjvm.ex.COMException;
import uk.co.beerdragon.comjvm.ex.NoSuchObjectError;
import uk.co.beerdragon.comjvm.ex.NotImplementedException;
import uk.co.beerdragon.comjvm.util.ArgumentBuffer;

/**
 * SPI that the COM hosting environment must provide. The minimum required implementation is
 * straightforward but will incur a lot of overhead from array allocations and primitive type
 * boxing. It is expected that a debugging environment may implement only the minimum required,
 * whereas the full C++ host will provide a native implementation of all methods.
 */
public abstract class COMHostSession {

  /**
   * Creates a new dispatcher for use by the COM hosting environment to create stubs for native
   * objects.
   * <p>
   * The dispatch signatures are in JLS form, regardless of any actual names present in the COM
   * implementation (for example in an IDL file or otherwise recognised by {@code IDispatch}, and in
   * the order of dispatch identifier. Thus the index of a signature into the {@code dispSignatures}
   * array will be that method's dispatch identifier.
   * <p>
   * It is not necessary for a COM object to implement all methods available on the class being
   * stubbed. Only those defined in the dispatch signatures will be routed to the COM host. Anything
   * else will cause super-class methods to be invoked or {@link NotImplementedException} if the
   * method was abstract.
   * <p>
   * If the set of supported methods is not fully known at invocation time then a maximal set of
   * dispatch signatures should be passed. Any methods that are not then available on the object
   * instance may result in COM error {@code E_NOTIMPL}, thrown as {@link NotImplementedException},
   * which will cause the stub to not attempt further invocations and use either the super-class
   * method or re-throw the exception.
   * 
   * @param <T>  
   *          the Java type that is implemented as a COM object
   * @param type
   *          the Java type that is implemented as a COM object
   * @param dispSignatures
   *          the array of dispatch methods that the COM object will support
   * @return the dispatcher instance
   */
  public final <T> COMDispatcher<T> dispatcher (final Class<T> type, final String[] dispSignatures) {
    return new COMDispatcher<T> (this, type, dispSignatures);
  }

  /**
   * Increments the reference count on the underlying COM object.
   * 
   * @param objectId
   *          the object identifier
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   */
  public abstract void addRef (int objectId);

  /**
   * Decrements the reference count on the underlying COM object. When the count reaches zero the
   * object identifier will become invalid, and may be reused, and the COM object may be destroyed.
   * 
   * @param objectId
   *          the object identifier
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   */
  public abstract void release (int objectId);

  /**
   * Invokes a no-arg method call, returning an object reference, on a COM object.
   * <p>
   * If a sub-class does not define this, then {@link #dispatchA} will be used.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public Object dispatch0A (final int objectId, final int dispId) {
    return dispatchA (objectId, dispId, ArrayUtils.EMPTY_OBJECT_ARRAY);
  }

  /**
   * Invokes a no-arg method call, with no return value, on a COM object.
   * <p>
   * If a sub-class does not define this, then {@link #dispatchV} will be used.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public void dispatch0V (final int objectId, final int dispId) {
    dispatchV (objectId, dispId, ArrayUtils.EMPTY_OBJECT_ARRAY);
  }

  /**
   * Invokes a no-arg method call, returning an {@code int}, on a COM object.
   * <p>
   * If a sub-class does not define this, then {@link #dispatchI} will be used.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public int dispatch0I (final int objectId, final int dispId) {
    return dispatchI (objectId, dispId, ArrayUtils.EMPTY_OBJECT_ARRAY);
  }

  /**
   * Invokes a no-arg method call, returning a {@code long}, on a COM object.
   * <p>
   * If a sub-class does not define this, then {@link #dispatchL} will be used.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public long dispatch0L (final int objectId, final int dispId) {
    return dispatchL (objectId, dispId, ArrayUtils.EMPTY_OBJECT_ARRAY);
  }

  /**
   * Invokes a no-arg method call, returning a {@code float}, on a COM object.
   * <p>
   * If a sub-class does not define this, then {@link #dispatchF} will be used.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public float dispatch0F (final int objectId, final int dispId) {
    return dispatchF (objectId, dispId, ArrayUtils.EMPTY_OBJECT_ARRAY);
  }

  /**
   * Invokes a no-arg method call, returning a {@code double}, on a COM object.
   * <p>
   * If a sub-class does not define this, then {@link #dispatchD} will be used.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public double dispatch0D (final int objectId, final int dispId) {
    return dispatchD (objectId, dispId, ArrayUtils.EMPTY_OBJECT_ARRAY);
  }

  /**
   * Invokes a method call with one argument type, returning an object reference, on a COM object.
   * <p>
   * If a sub-class does not define this, then {@link #dispatchA} will be used.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @param t1
   *          an {@link ArgumentBuffer} member containing parameter values
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public Object dispatch1A (final int objectId, final int dispId, final Object t1) {
    return dispatchA (objectId, dispId, new Object[] { t1 });
  }

  /**
   * Invokes a method call with one argument type and no return value on a COM object.
   * <p>
   * If a sub-class does not define this, then {@link #dispatchV} will be used.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @param t1
   *          an {@link ArgumentBuffer} member containing parameter values
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public void dispatch1V (final int objectId, final int dispId, final Object t1) {
    dispatchV (objectId, dispId, new Object[] { t1 });
  }

  /**
   * Invokes a method call with one argument type, returning an {@code int}, on a COM object.
   * <p>
   * If a sub-class does not define this, then {@link #dispatchI} will be used.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @param t1
   *          an {@link ArgumentBuffer} member containing parameter values
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public int dispatch1I (final int objectId, final int dispId, final Object t1) {
    return dispatchI (objectId, dispId, new Object[] { t1 });
  }

  /**
   * Invokes a method call with one argument type, returning a {@code long}, on a COM object.
   * <p>
   * If a sub-class does not define this, then {@link #dispatchL} will be used.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @param t1
   *          an {@link ArgumentBuffer} member containing parameter values
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public long dispatch1L (final int objectId, final int dispId, final Object t1) {
    return dispatchL (objectId, dispId, new Object[] { t1 });
  }

  /**
   * Invokes a method call with one argument type, returning a {@code float}, on a COM object.
   * <p>
   * If a sub-class does not define this, then {@link #dispatchF} will be used.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @param t1
   *          an {@link ArgumentBuffer} member containing parameter values
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public float dispatch1F (final int objectId, final int dispId, final Object t1) {
    return dispatchF (objectId, dispId, new Object[] { t1 });
  }

  /**
   * Invokes a method call with one argument type, returning a {@code double}, on a COM object.
   * <p>
   * If a sub-class does not define this, then {@link #dispatchD} will be used.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @param t1
   *          an {@link ArgumentBuffer} member containing parameter values
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public double dispatch1D (final int objectId, final int dispId, final Object t1) {
    return dispatchD (objectId, dispId, new Object[] { t1 });
  }

  /**
   * Invokes a method call with two argument types, returning an object reference, on a COM object.
   * <p>
   * If a sub-class does not define this, then {@link #dispatchA} will be used.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @param t1
   *          an {@link ArgumentBuffer} member containing parameter values
   * @param t2
   *          an {@link ArgumentBuffer} member containing parameter values
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public Object dispatch2A (final int objectId, final int dispId, final Object t1, final Object t2) {
    return dispatchA (objectId, dispId, new Object[] { t1, t2 });
  }

  /**
   * Invokes a method call with two argument types and no return value on a COM object.
   * <p>
   * If a sub-class does not define this, then {@link #dispatchV} will be used.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @param t1
   *          an {@link ArgumentBuffer} member containing parameter values
   * @param t2
   *          an {@link ArgumentBuffer} member containing parameter values
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public void dispatch2V (final int objectId, final int dispId, final Object t1, final Object t2) {
    dispatchV (objectId, dispId, new Object[] { t1, t2 });
  }

  /**
   * Invokes a method call with two argument types, returning an {@code int}, on a COM object.
   * <p>
   * If a sub-class does not define this, then {@link #dispatchI} will be used.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @param t1
   *          an {@link ArgumentBuffer} member containing parameter values
   * @param t2
   *          an {@link ArgumentBuffer} member containing parameter values
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public int dispatch2I (final int objectId, final int dispId, final Object t1, final Object t2) {
    return dispatchI (objectId, dispId, new Object[] { t1, t2 });
  }

  /**
   * Invokes a method call with two argument types, returning a {@code long}, on a COM object.
   * <p>
   * If a sub-class does not define this, then {@link #dispatchL} will be used.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @param t1
   *          an {@link ArgumentBuffer} member containing parameter values
   * @param t2
   *          an {@link ArgumentBuffer} member containing parameter values
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public long dispatch2L (final int objectId, final int dispId, final Object t1, final Object t2) {
    return dispatchL (objectId, dispId, new Object[] { t1, t2 });
  }

  /**
   * Invokes a method call with two argument types, returning a {@code float}, on a COM object.
   * <p>
   * If a sub-class does not define this, then {@link #dispatchF} will be used.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @param t1
   *          an {@link ArgumentBuffer} member containing parameter values
   * @param t2
   *          an {@link ArgumentBuffer} member containing parameter values
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public float dispatch2F (final int objectId, final int dispId, final Object t1, final Object t2) {
    return dispatchF (objectId, dispId, new Object[] { t1, t2 });
  }

  /**
   * Invokes a method call with one argument type, returning a {@code double}, on a COM object.
   * <p>
   * If a sub-class does not define this, then {@link #dispatchD} will be used.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @param t1
   *          an {@link ArgumentBuffer} member containing parameter values
   * @param t2
   *          an {@link ArgumentBuffer} member containing parameter values          
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public double dispatch2D (final int objectId, final int dispId, final Object t1, final Object t2) {
    return dispatchD (objectId, dispId, new Object[] { t1, t2 });
  }

  // TODO: The most parameters we'll ever need is 5!

  /**
   * Invokes a method call, with an arbitrary number of type parameters, on a COM object.
   * <p>
   * If the method returns a primitive type the value must be boxed to create the return value. If
   * the method has no return value then it must return {@code null}.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @param ts
   *          parameter arrays, one per type in the order defined in {@link ArgumentBuffer}
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public abstract Object dispatchA (int objectId, int dispId, Object[] ts);

  /**
   * Invokes a method call, with an arbitrary number of type parameters and no return result, on a
   * COM object.
   * <p>
   * If a sub-class does not define this then {@link #dispatchA} will be used and its {@code null}
   * return ignored.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @param ts
   *          parameter arrays, one per type in the order defined in {@link ArgumentBuffer}
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public void dispatchV (final int objectId, final int dispId, final Object[] ts) {
    dispatchA (objectId, dispId, ts);
  }

  /**
   * Invokes a method call, with an arbitrary number of type parameters and a {@code int} result, on
   * a COM object.
   * <p>
   * If a sub-class does not define this then {@link #dispatchA} will be used and its boxed result
   * decoded.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @param ts
   *          parameter arrays, one per type in the order defined in {@link ArgumentBuffer}
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public int dispatchI (final int objectId, final int dispId, final Object[] ts) {
    return dispatchI (objectId, dispId, ts);
  }

  /**
   * Invokes a method call, with an arbitrary number of type parameters and a {@code long} result,
   * on a COM object.
   * <p>
   * If a sub-class does not define this then {@link #dispatchA} will be used and its boxed result
   * decoded.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @param ts
   *          parameter arrays, one per type in the order defined in {@link ArgumentBuffer}
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public long dispatchL (final int objectId, final int dispId, final Object[] ts) {
    return (Long)dispatchA (objectId, dispId, ts);
  }

  /**
   * Invokes a method call, with an arbitrary number of type parameters and a {@code float} result,
   * on a COM object.
   * <p>
   * If a sub-class does not define this then {@link #dispatchA} will be used and its boxed result
   * decoded.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @param ts
   *          parameter arrays, one per type in the order defined in {@link ArgumentBuffer}
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public float dispatchF (final int objectId, final int dispId, final Object[] ts) {
    return (Float)dispatchA (objectId, dispId, ts);
  }

  /**
   * Invokes a method call, with an arbitrary number of type parameters and a {@code double} result,
   * on a COM object.
   * <p>
   * If a sub-class does not define this then {@link #dispatchA} will be used and its boxed result
   * decoded.
   * 
   * @param objectId
   *          the COM object identifier, as passed to {@link COMDispatcher#stub}
   * @param dispId
   *          the method dispatch identifier
   * @param ts
   *          parameter arrays, one per type in the order defined in {@link ArgumentBuffer}
   * @return the result of the method call
   * @throws COMException
   *           if the COM object method returned an error code
   * @throws NoSuchObjectError
   *           if the object identifier is invalid
   * @throws NoSuchMethodError
   *           if the dispatch identifier is invalid
   */
  public double dispatchD (final int objectId, final int dispId, final Object[] ts) {
    return (Double)dispatchA (objectId, dispId, ts);
  }

}