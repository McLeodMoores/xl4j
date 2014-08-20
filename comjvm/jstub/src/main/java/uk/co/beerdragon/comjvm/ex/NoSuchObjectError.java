/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.ex;

import org.apache.commons.lang3.StringUtils;

/**
 * Thrown if an application attempts to dispatch a method to an invalid object identifier. Object
 * identifiers are managed by the declared stubs and will only be invalidated at the host COM
 * environment when the stub has fallen out of scope, preventing further method dispatches. This
 * exception can only occur if there is a fault in the stubbing logic or the host COM environment
 * has failed in a fashion which cannot otherwise be reported.
 */
public final class NoSuchObjectError extends Error {

  private static final long serialVersionUID = 1L;

  private static final String DEFAULT_HOST_DESCRIPTION = "localhost";

  private transient int _hashCode;

  private final String _hostDescription;

  /**
   * Creates a new instance.
   * 
   * @param objectId
   *          the object identifier which was invalid with respect to the COM environment
   * @param hostDescription
   *          a diagnostic string that may appear in logs. This should identify the host environment
   *          where possible; for example by including a process identifier and/or executable image
   *          name. If {@code null} or an empty string is passed then a default string will be used
   *          to avoid the risk of {@link NullPointerException} in any diagnostic reporting
   */
  public NoSuchObjectError (final int objectId, final String hostDescription) {
    super (Integer.toString (objectId));
    _hostDescription = StringUtils.defaultIfBlank (hostDescription, DEFAULT_HOST_DESCRIPTION);
    readResolve ();
  }

  /**
   * Returns the diagnostic host description string.
   * 
   * @return the host description, never {@code null}
   */
  public String getHostDescription () {
    return _hostDescription;
  }

  private Object readResolve () {
    _hashCode = toString ().hashCode ();
    return this;
  }

  // Object

  @Override
  public int hashCode () {
    return _hashCode;
  }

  @Override
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof NoSuchObjectError)) return false;
    final NoSuchObjectError other = (NoSuchObjectError)o;
    return getMessage ().equals (other.getMessage ())
        && getHostDescription ().equals (other.getHostDescription ());
  }

  @Override
  public String toString () {
    return "No object " + getMessage () + " at " + getHostDescription ();
  }

}