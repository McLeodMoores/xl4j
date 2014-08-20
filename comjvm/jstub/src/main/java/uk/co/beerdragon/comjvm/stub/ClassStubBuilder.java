/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.stub;

/**
 * Specialisation of {@link StubBuilder} producing stubs for Java classes.
 * 
 * @param<T> the class being stubbed
 */
/* package */class ClassStubBuilder<T> extends StubBuilder<T> {

  /**
   * Creates a new instance.
   * 
   * @param clazz
   *          the class to stub, not {@code null}
   */
  public ClassStubBuilder (final Class<T> clazz) {
    super (clazz);
  }

  // StubBuilder

  @Override
  String getSuperClassName () {
    return getClazz ().getName ();
  }

  @Override
  String[] getInterfaceNames () {
    final Class<?>[] interfaces = getClazz ().getInterfaces ();
    final String[] interfaceNames = new String[interfaces.length];
    for (int i = 0; i < interfaces.length; i++) {
      interfaceNames[i] = interfaces[i].getName ();
    }
    return interfaceNames;
  }

}