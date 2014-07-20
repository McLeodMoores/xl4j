/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm;

/**
 * Invocation service for passing methods invoked by Java calls to the underlying COM objects.
 * <p>
 * A {@link ClassMarshaller} is constructed for any classes or interfaces that are implemented in
 * COM. This will allocate numeric dispatch identifiers for all method calls. A
 * {@code MethodDispatcher} instance can be initialised from the marshaler and be informed of the
 * numeric identifiers that will be used. Whenever a Java object is constructed as a stub for the
 * underlying COM implementation an initialised dispatcher will be used to handle all calls made on
 * that stub.
 */
public abstract class MethodDispatcher {

  private static final Variant[] EMPTY = new Variant[0];

  /**
   * Notifies the dispatcher of the numeric dispatch identifier used for a method.
   * <p>
   * For example, a dispatcher for {@link Iterable} might be notified with a {@code dispatchId} of
   * {@code 1} for a {@code methodSignature} of {@code iterator()Ljava/util/Iterator;}.
   * 
   * @param dispatchId
   *          numberif dispatch identifier
   * @param methodSignature
   *          method name and signature, never {@code null}
   */
  public abstract void notifyDispatchId (int dispatchId, String methodSignature);

  /**
   * Indicates whether the dispatcher will handle the invocation, or whether the super-class should
   * be invoked instead.
   */
  public abstract boolean isDispatch (int dispatchId);

  /**
   * Invokes a method returning {@code void} with no arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeVoid(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   */
  public void invokeVoid (final Object self, final int dispatchId, final COMObject object) {
    invokeVoid (self, dispatchId, object, EMPTY);
  }

  /**
   * Invokes a method returning {@code boolean} with no arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeBoolean(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @return invocation result
   */
  public boolean invokeBoolean (final Object self, final int dispatchId, final COMObject object) {
    return invokeBoolean (self, dispatchId, object, EMPTY);
  }

  /**
   * Invokes a method returning {@code char} with no arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeChar(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @return invocation result
   */
  public char invokeChar (final Object self, final int dispatchId, final COMObject object) {
    return invokeChar (self, dispatchId, object, EMPTY);
  }

  /**
   * Invokes a method returning {@code byte} with no arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeByte(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @return invocation result
   */
  public byte invokeByte (final Object self, final int dispatchId, final COMObject object) {
    return invokeByte (self, dispatchId, object, EMPTY);
  }

  /**
   * Invokes a method returning {@code short} with no arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeShotr(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @return invocation result
   */
  public short invokeShort (final Object self, final int dispatchId, final COMObject object) {
    return invokeShort (self, dispatchId, object, EMPTY);
  }

  /**
   * Invokes a method returning {@code int} with no arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeInt(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @return invocation result
   */
  public int invokeInt (final Object self, final int dispatchId, final COMObject object) {
    return invokeInt (self, dispatchId, object, EMPTY);
  }

  /**
   * Invokes a method returning {@code long} with no arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeLogn(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @return invocation result
   */
  public long invokeLong (final Object self, final int dispatchId, final COMObject object) {
    return invokeLong (self, dispatchId, object, EMPTY);
  }

  /**
   * Invokes a method returning {@code float} with no arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeFloat(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @return invocation result
   */
  public float invokeFloat (final Object self, final int dispatchId, final COMObject object) {
    return invokeFloat (self, dispatchId, object, EMPTY);
  }

  /**
   * Invokes a method returning {@code double} with no arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeDouble(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @return invocation result
   */
  public double invokeDouble (final Object self, final int dispatchId, final COMObject object) {
    return invokeDouble (self, dispatchId, object, EMPTY);
  }

  /**
   * Invokes a method returning an object reference with no arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeObject(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @return invocation result
   */
  public Object invokeObject (final Object self, final int dispatchId, final COMObject object) {
    return invokeObject (self, dispatchId, object, EMPTY);
  }

  /**
   * Invokes a method returning {@code void} with one argument.
   * <p>
   * The default implementation will delegate to {@link #invokeVoid(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   */
  public void invokeVoid (final Object self, final int dispatchId, final COMObject object,
      final Variant p1) {
    invokeVoid (self, dispatchId, object, new Variant[] { p1 });
  }

  /**
   * Invokes a method returning {@code boolean} with one argument.
   * <p>
   * The default implementation will delegate to {@link #invokeBoolean(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @return invocation result
   */
  public boolean invokeBoolean (final Object self, final int dispatchId, final COMObject object,
      final Variant p1) {
    return invokeBoolean (self, dispatchId, object, new Variant[] { p1 });
  }

  /**
   * Invokes a method returning {@code char} with one argument.
   * <p>
   * The default implementation will delegate to {@link #invokeChar(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @return invocation result
   */
  public char invokeChar (final Object self, final int dispatchId, final COMObject object,
      final Variant p1) {
    return invokeChar (self, dispatchId, object, new Variant[] { p1 });
  }

  /**
   * Invokes a method returning {@code byte} with one argument.
   * <p>
   * The default implementation will delegate to {@link #invokeByte(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @return invocation result
   */
  public byte invokeByte (final Object self, final int dispatchId, final COMObject object,
      final Variant p1) {
    return invokeByte (self, dispatchId, object, new Variant[] { p1 });
  }

  /**
   * Invokes a method returning {@code short} with one argument.
   * <p>
   * The default implementation will delegate to {@link #invokeShort(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @return invocation result
   */
  public short invokeShort (final Object self, final int dispatchId, final COMObject object,
      final Variant p1) {
    return invokeShort (self, dispatchId, object, new Variant[] { p1 });
  }

  /**
   * Invokes a method returning {@code int} with one argument.
   * <p>
   * The default implementation will delegate to {@link #invokeInt(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @return invocation result
   */
  public int invokeInt (final Object self, final int dispatchId, final COMObject object,
      final Variant p1) {
    return invokeInt (self, dispatchId, object, new Variant[] { p1 });
  }

  /**
   * Invokes a method returning {@code long} with one argument.
   * <p>
   * The default implementation will delegate to {@link #invokeLong(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @return invocation result
   */
  public long invokeLong (final Object self, final int dispatchId, final COMObject object,
      final Variant p1) {
    return invokeLong (self, dispatchId, object, new Variant[] { p1 });
  }

  /**
   * Invokes a method returning {@code float} with one argument.
   * <p>
   * The default implementation will delegate to {@link #invokeFloat(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @return invocation result
   */
  public float invokeFloat (final Object self, final int dispatchId, final COMObject object,
      final Variant p1) {
    return invokeFloat (self, dispatchId, object, new Variant[] { p1 });
  }

  /**
   * Invokes a method returning {@code double} with one argument.
   * <p>
   * The default implementation will delegate to {@link #invokeDouble(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @return invocation result
   */
  public double invokeDouble (final Object self, final int dispatchId, final COMObject object,
      final Variant p1) {
    return invokeDouble (self, dispatchId, object, new Variant[] { p1 });
  }

  /**
   * Invokes a method returning an object reference with one argument.
   * <p>
   * The default implementation will delegate to {@link #invokeObject(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @return invocation result
   */
  public Object invokeObject (final Object self, final int dispatchId, final COMObject object,
      final Variant p1) {
    return invokeObject (self, dispatchId, object, new Variant[] { p1 });
  }

  /**
   * Invokes a method returning {@code void} with two arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeVoid(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   */
  public void invokeVoid (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2) {
    invokeVoid (self, dispatchId, object, new Variant[] { p1, p2 });
  }

  /**
   * Invokes a method returning {@code boolean} with two arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeVoid(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @return invocation result
   */
  public boolean invokeBoolean (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2) {
    return invokeBoolean (self, dispatchId, object, new Variant[] { p1, p2 });
  }

  /**
   * Invokes a method returning {@code char} with two arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeChar(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @return invocation result
   */
  public char invokeChar (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2) {
    return invokeChar (self, dispatchId, object, new Variant[] { p1, p2 });
  }

  /**
   * Invokes a method returning {@code byte} with two arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeByte(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @return invocation result
   */
  public byte invokeByte (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2) {
    return invokeByte (self, dispatchId, object, new Variant[] { p1, p2 });
  }

  /**
   * Invokes a method returning {@code short} with two arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeShort(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @return invocation result
   */
  public short invokeShort (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2) {
    return invokeShort (self, dispatchId, object, new Variant[] { p1, p2 });
  }

  /**
   * Invokes a method returning {@code int} with two arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeInt(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @return invocation result
   */
  public int invokeInt (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2) {
    return invokeInt (self, dispatchId, object, new Variant[] { p1, p2 });
  }

  /**
   * Invokes a method returning {@code long} with two arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeLong(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @return invocation result
   */
  public long invokeLong (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2) {
    return invokeLong (self, dispatchId, object, new Variant[] { p1, p2 });
  }

  /**
   * Invokes a method returning {@code float} with two arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeFloat(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @return invocation result
   */
  public float invokeFloat (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2) {
    return invokeFloat (self, dispatchId, object, new Variant[] { p1, p2 });
  }

  /**
   * Invokes a method returning {@code double} with two arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeDouble(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @return invocation result
   */
  public double invokeDouble (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2) {
    return invokeDouble (self, dispatchId, object, new Variant[] { p1, p2 });
  }

  /**
   * Invokes a method returning an object reference with two arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeObject(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @return invocation result
   */
  public Object invokeObject (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2) {
    return invokeObject (self, dispatchId, object, new Variant[] { p1, p2 });
  }

  /**
   * Invokes a method returning {@code void} with three arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeVoid(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   */
  public void invokeVoid (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3) {
    invokeVoid (self, dispatchId, object, new Variant[] { p1, p2, p3 });
  }

  /**
   * Invokes a method returning {@code boolean} with three arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeBoolean(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @return invocation result
   */
  public boolean invokeBoolean (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3) {
    return invokeBoolean (self, dispatchId, object, new Variant[] { p1, p2, p3 });
  }

  /**
   * Invokes a method returning {@code char} with three arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeChar(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @return invocation result
   */
  public char invokeChar (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3) {
    return invokeChar (self, dispatchId, object, new Variant[] { p1, p2, p3 });
  }

  /**
   * Invokes a method returning {@code byte} with three arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeByte(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @return invocation result
   */
  public byte invokeByte (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3) {
    return invokeByte (self, dispatchId, object, new Variant[] { p1, p2, p3 });
  }

  /**
   * Invokes a method returning {@code short} with three arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeShort(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @return invocation result
   */
  public short invokeShort (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3) {
    return invokeShort (self, dispatchId, object, new Variant[] { p1, p2, p3 });
  }

  /**
   * Invokes a method returning {@code int} with three arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeInt(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @return invocation result
   */
  public int invokeInt (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3) {
    return invokeInt (self, dispatchId, object, new Variant[] { p1, p2, p3 });
  }

  /**
   * Invokes a method returning {@code long} with three arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeLong(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @return invocation result
   */
  public long invokeLong (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3) {
    return invokeLong (self, dispatchId, object, new Variant[] { p1, p2, p3 });
  }

  /**
   * Invokes a method returning {@code float} with three arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeFloat(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @return invocation result
   */
  public float invokeFloat (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3) {
    return invokeFloat (self, dispatchId, object, new Variant[] { p1, p2, p3 });
  }

  /**
   * Invokes a method returning {@code double} with three arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeDouble(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @return invocation result
   */
  public double invokeDouble (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3) {
    return invokeDouble (self, dispatchId, object, new Variant[] { p1, p2, p3 });
  }

  /**
   * Invokes a method returning an object reference with three arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeObject(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @return invocation result
   */
  public Object invokeObject (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3) {
    return invokeObject (self, dispatchId, object, new Variant[] { p1, p2, p3 });
  }

  /**
   * Invokes a method returning {@code void} with four arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeVoid(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @param p4
   *          fourth parameter, never {@code null}
   */
  public void invokeVoid (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3, final Variant p4) {
    invokeVoid (self, dispatchId, object, new Variant[] { p1, p2, p3, p4 });
  }

  /**
   * Invokes a method returning {@code boolean} with four arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeBoolean(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @param p4
   *          fourth parameter, never {@code null}
   * @return invocation result
   */
  public boolean invokeBoolean (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3, final Variant p4) {
    return invokeBoolean (self, dispatchId, object, new Variant[] { p1, p2, p3, p4 });
  }

  /**
   * Invokes a method returning {@code char} with four arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeChar(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @param p4
   *          fourth parameter, never {@code null}
   * @return invocation result
   */
  public char invokeChar (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3, final Variant p4) {
    return invokeChar (self, dispatchId, object, new Variant[] { p1, p2, p3, p4 });
  }

  /**
   * Invokes a method returning {@code byte} with four arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeByte(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @param p4
   *          fourth parameter, never {@code null}
   * @return invocation result
   */
  public byte invokeByte (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3, final Variant p4) {
    return invokeByte (self, dispatchId, object, new Variant[] { p1, p2, p3, p4 });
  }

  /**
   * Invokes a method returning {@code short} with four arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeShort(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @param p4
   *          fourth parameter, never {@code null}
   * @return invocation result
   */
  public short invokeShort (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3, final Variant p4) {
    return invokeShort (self, dispatchId, object, new Variant[] { p1, p2, p3, p4 });
  }

  /**
   * Invokes a method returning {@code int} with four arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeInt(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @param p4
   *          fourth parameter, never {@code null}
   * @return invocation result
   */
  public int invokeInt (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3, final Variant p4) {
    return invokeInt (self, dispatchId, object, new Variant[] { p1, p2, p3, p4 });
  }

  /**
   * Invokes a method returning {@code long} with four arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeLong(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @param p4
   *          fourth parameter, never {@code null}
   * @return invocation result
   */
  public long invokeLong (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3, final Variant p4) {
    return invokeLong (self, dispatchId, object, new Variant[] { p1, p2, p3, p4 });
  }

  /**
   * Invokes a method returning {@code float} with four arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeFloat(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @param p4
   *          fourth parameter, never {@code null}
   * @return invocation result
   */
  public float invokeFloat (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3, final Variant p4) {
    return invokeFloat (self, dispatchId, object, new Variant[] { p1, p2, p3, p4 });
  }

  /**
   * Invokes a method returning {@code double} with four arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeDouble(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @param p4
   *          fourth parameter, never {@code null}
   * @return invocation result
   */
  public double invokeDouble (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3, final Variant p4) {
    return invokeDouble (self, dispatchId, object, new Variant[] { p1, p2, p3, p4 });
  }

  /**
   * Invokes a method returning an object reference with four arguments.
   * <p>
   * The default implementation will delegate to {@link #invokeObject(int,COMObject,Variant[])}.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param p1
   *          first parameter, never {@code null}
   * @param p2
   *          second parameter, never {@code null}
   * @param p3
   *          third parameter, never {@code null}
   * @param p4
   *          fourth parameter, never {@code null}
   * @return invocation result
   */
  public Object invokeObject (final Object self, final int dispatchId, final COMObject object,
      final Variant p1, final Variant p2, final Variant p3, final Variant p4) {
    return invokeObject (self, dispatchId, object, new Variant[] { p1, p2, p3, p4 });
  }

  /**
   * Invokes a method returning {@code void} with an arbitrary number of arguments.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param params
   *          parameters, never {@code null} or containing {@code null}
   */
  public abstract void invokeVoid (Object self, int dispatchId, COMObject object, Variant[] params);

  /**
   * Invokes a method returning {@code boolean} with an arbitrary number of arguments.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param params
   *          parameters, never {@code null} or containing {@code null}
   * @return invocation result
   */
  public abstract boolean invokeBoolean (Object self, int dispatchId, COMObject object,
      Variant[] params);

  /**
   * Invokes a method returning {@code char} with an arbitrary number of arguments.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param params
   *          parameters, never {@code null} or containing {@code null}
   * @return invocation result
   */
  public abstract char invokeChar (Object self, int dispatchId, COMObject object, Variant[] params);

  /**
   * Invokes a method returning {@code byte} with an arbitrary number of arguments.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param params
   *          parameters, never {@code null} or containing {@code null}
   * @return invocation result
   */
  public abstract byte invokeByte (Object self, int dispatchId, COMObject object, Variant[] params);

  /**
   * Invokes a method returning {@code short} with an arbitrary number of arguments.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param params
   *          parameters, never {@code null} or containing {@code null}
   * @return invocation result
   */
  public abstract short invokeShort (Object self, int dispatchId, COMObject object, Variant[] params);

  /**
   * Invokes a method returning {@code int} with an arbitrary number of arguments.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param params
   *          parameters, never {@code null} or containing {@code null}
   * @return invocation result
   */
  public abstract int invokeInt (Object self, int dispatchId, COMObject object, Variant[] params);

  /**
   * Invokes a method returning {@code long} with an arbitrary number of arguments.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param params
   *          parameters, never {@code null} or containing {@code null}
   * @return invocation result
   */
  public abstract long invokeLong (Object self, int dispatchId, COMObject object, Variant[] params);

  /**
   * Invokes a method returning {@code float} with an arbitrary number of arguments.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param params
   *          parameters, never {@code null} or containing {@code null}
   * @return invocation result
   */
  public abstract float invokeFloat (Object self, int dispatchId, COMObject object, Variant[] params);

  /**
   * Invokes a method returning {@code double} with an arbitrary number of arguments.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param params
   *          parameters, never {@code null} or containing {@code null}
   * @return invocation result
   */
  public abstract double invokeDouble (Object self, int dispatchId, COMObject object,
      Variant[] params);

  /**
   * Invokes a method returning an object reference with an arbitrary number of arguments.
   * 
   * @param self
   *          Java object corresponding to the stub which is dispatching this call, never
   *          {@code null}
   * @param dispatchId
   *          numeric method identifier
   * @param object
   *          target object to dispatch the method to, never {@code null}
   * @param params
   *          parameters, never {@code null} or containing {@code null}
   * @return invocation result
   */
  public abstract Object invokeObject (Object self, int dispatchId, COMObject object,
      Variant[] params);

}