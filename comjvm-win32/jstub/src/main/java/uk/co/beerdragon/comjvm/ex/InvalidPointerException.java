/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.ex;

/**
 * Specialisation of {@code COMException} for {@code E_POINTER}.
 */
public final class InvalidPointerException extends COMException {

  private static final long serialVersionUID = 1L;

  /**
   * Creates a new instance.
   * <p>
   * Instances are normally created by {@link COMException#of} rather than calling this constructor
   * directly.
   */
  public InvalidPointerException () {
    super (HRESULT.E_POINTER);
  }

  // COMException

  @Override
  public String toString () {
    return "E_POINTER";
  }

}