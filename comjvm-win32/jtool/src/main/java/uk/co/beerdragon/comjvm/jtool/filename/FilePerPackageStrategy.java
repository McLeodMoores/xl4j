/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.filename;

/**
 * File naming strategy to produce one file per package.
 * <p>
 * For a class named {@code foo.bar.Something} then a filename is created for the package, using
 * camel case, to give {@code FooBar}.
 */
public class FilePerPackageStrategy implements FilenameStrategy {

  // FilenameStrategy

  @Override
  public String nameFor (final String className) {
    final int suffix = className.lastIndexOf ('.');
    if (suffix <= 0) {
      return "JavaLang";
    }
    final StringBuilder sb = new StringBuilder (className.length ());
    boolean caps = true;
    for (int i = 0; i < suffix; i++) {
      final char c = className.charAt (i);
      if (caps) {
        sb.append (Character.toUpperCase (c));
        caps = false;
      } else {
        if (c == '.') {
          caps = true;
        } else {
          sb.append (c);
        }
      }
    }
    return sb.toString ();
  }

}