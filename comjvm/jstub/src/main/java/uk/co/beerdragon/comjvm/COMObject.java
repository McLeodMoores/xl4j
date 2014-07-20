/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm;

/**
 * Java reference to an arbitrary object, normally implementing {@code IJDispatch}, in the COM
 * layer.
 */
public final class COMObject {

  private final int _index;

  private final COMJvmSession _session;

  /* package */COMObject (final int index, final COMJvmSession session) {
    _index = index;
    _session = session;
  }

  @Override
  protected void finalize () {
    _session.release (_index);
  }

  public Variant invoke (final int dispatchId) {
    throw new UnsupportedOperationException ();
  }

  public Variant invoke (final int dispatchId, final Variant p1) {
    throw new UnsupportedOperationException ();
  }

  public Variant invoke (final int dispatchId, final Variant p1, final Variant p2) {
    throw new UnsupportedOperationException ();
  }

  public Variant invoke (final int dispatchId, final Variant p1, final Variant p2, final Variant p3) {
    throw new UnsupportedOperationException ();
  }

  public Variant invoke (final int dispatchId, final Variant p1, final Variant p2,
      final Variant p3, final Variant p4) {
    throw new UnsupportedOperationException ();
  }

  public Variant invoke (final int dispatchId, final Variant p1, final Variant p2,
      final Variant p3, final Variant p4, final Variant p5) {
    throw new UnsupportedOperationException ();
  }

  public Variant invoke (final int dispatchId, final Variant[] params) {
    throw new UnsupportedOperationException ();
  }

  // Object

  @Override
  public final boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof COMObject)) return false;
    final COMObject other = (COMObject)o;
    return (other._session == _session) && (other._index == _index);
  }

  @Override
  public final int hashCode () {
    return _session.hashCode () ^ _index;
  }

}