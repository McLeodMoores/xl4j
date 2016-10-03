/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.filename;

/**
 * File naming strategy to produce one file per class.
 * <p>
 * For a class named {@code foo.bar.Something} then a filename is created for the package, using
 * camel case, to give {@code FooBarSomething}.
 */
public class FilePerClassStrategy implements FilenameStrategy {

  /**
   * Implementation of the naming strategy. This is the implementation of {@link #nameFor} and
   * should normally be accessed via a {@link FilenameStrategy} indirection to support flexibility.
   * Only call this directly if the exact behaviour of this implementation is what will be required
   * in every circumstance.
   * 
   * @param className
   *          class name, not {@code null}
   * @return the package and class name concatenated in camel case, never {@code null}
   */
  public static String createName (final String className) {
    final StringBuilder sb = new StringBuilder (className.length ());
    if (className.lastIndexOf ('.') <= 0) {
      sb.append ("JavaLang");
    }
    boolean caps = true;
    for (int i = 0; i < className.length (); i++) {
      final char c = className.charAt (i);
      if (caps) {
        sb.append (Character.toUpperCase (c));
        caps = false;
      } else {
        if (c == '.') {
          caps = true;
        } else if (c == '$') {
          sb.append ('_');
        } else {
          sb.append (c);
        }
      }
    }
    return sb.toString ();
  }

  // FilenameStrategy

  @Override
  public String nameFor (final String className) {
    return createName (className);
  }

}