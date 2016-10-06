/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.ex;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception wrapper for error codes returned by COM object methods.
 * <p>
 * Any method dispatches that do not result in a success code will have the error {@code HRESULT}
 * wrapped as a {@link COMException} and be thrown.
 * <p>
 * Note that some error types, such as {@code E_NOTIMPL}, may have special meaning for the Java
 * stubs and not be directly propagated to the caller.
 */
public class COMException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private static final Map<Integer, Class<? extends COMException>> s_exceptions = new HashMap<Integer, Class<? extends COMException>> ();

  static {
    s_exceptions.put (HRESULT.E_ABORT, OperationAbortedException.class);
    s_exceptions.put (HRESULT.E_ACCESSDENIED, GeneralAccessDeniedException.class);
    s_exceptions.put (HRESULT.E_FAIL, UnspecifiedFailureException.class);
    s_exceptions.put (HRESULT.E_HANDLE, InvalidHandleException.class);
    s_exceptions.put (HRESULT.E_INVALIDARG, InvalidArgumentException.class);
    s_exceptions.put (HRESULT.E_NOINTERFACE, NoSuchInterfaceException.class);
    s_exceptions.put (HRESULT.E_NOTIMPL, NotImplementedException.class);
    s_exceptions.put (HRESULT.E_OUTOFMEMORY, OutOfMemoryException.class);
    s_exceptions.put (HRESULT.E_POINTER, InvalidPointerException.class);
    s_exceptions.put (HRESULT.E_UNEXPECTED, UnexpectedFailureException.class);
  }

  /**
   * The wrapped {@code HRESULT} code.
   */
  private final int _hresult;

  /**
   * Creates a new instance.
   * 
   * @param hresult
   *          the {@code HRESULT} error code
   */
  protected COMException (final int hresult) {
    super (HRESULT.toString (hresult));
    _hresult = hresult;
  }

  /**
   * Creates a new instance, using a declared sub-class if any exist.
   * 
   * @param hresult
   *          the {@code HRESULT} error code
   * @return the exception instance to be thrown, never {@code null}
   */
  public static COMException of (final int hresult) {
    final Class<? extends COMException> clazz = s_exceptions.get (hresult);
    if (clazz != null) {
      try {
        return clazz.newInstance ();
      } catch (final Exception e) {
        throw new IllegalArgumentException ("Bad " + clazz.getSimpleName () + " for "
            + HRESULT.toString (hresult), e);
      }
    } else {
      return new COMException (hresult);
    }
  }

  /**
   * Creates the boxed {@link HRESULT} wrapping the status code.
   * 
   * @return the {@code HRESULT} instance, never {@code null}
   */
  public HRESULT toHRESULT () {
    return new HRESULT (_hresult);
  }

  /**
   * Tests if this is a customer specified error code.
   * 
   * @return {@code true} if this is a custom code, {@code false} if this is Microsoft defined.
   */
  public boolean isCustomer () {
    return HRESULT.isCustomer (_hresult);
  }

  /**
   * Returns the facility code.
   * 
   * @return the facility code
   */
  public int getFacility () {
    return HRESULT.getFacility (_hresult);
  }

  /**
   * Returns the failure code.
   * 
   * @return the failure code
   */
  public int getCode () {
    return HRESULT.getCode (_hresult);
  }

  // Object

  @Override
  public final int hashCode () {
    return _hresult;
  }

  @Override
  public final boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof COMException)) return false;
    return ((COMException)o)._hresult == _hresult;
  }

  @Override
  public String toString () {
    return HRESULT.toString (_hresult);
  }

}