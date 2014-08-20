/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.stub;

/**
 * Specialisation of {@link StubBuilder} producing stubs for Java interfaces.
 * 
 * @param<T> the interface being stubbed
 */
/* package */class InterfaceStubBuilder<T> extends StubBuilder<T> {

  /**
   * Creates a new instance.
   * 
   * @param clazz
   *          the interface to stub, not {@code null}
   */
  public InterfaceStubBuilder (final Class<T> clazz) {
    super (clazz);
  }

  // StubBuilder

  @Override
  /* package */String getSuperClassName () {
    return "java/lang/Object";
  }

  @Override
  /* package */String[] getInterfaceNames () {
    return new String[] { getClazz ().getName ().replace ('.', '/') };
  }

}