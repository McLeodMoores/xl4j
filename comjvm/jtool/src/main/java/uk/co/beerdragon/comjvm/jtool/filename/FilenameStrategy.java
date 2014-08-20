/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.filename;

/**
 * Strategy for naming files containing code for a given class. Files may either be specific to a
 * single class, or multiple classes may be grouped into single files (for example on a per-package
 * basis).
 */
public interface FilenameStrategy {

  /**
   * Looks up the name to use for a class. For example, an implementation might indicate that
   * {@code FooBarImpl.c} should be created for class {@code FooBar}.
   * 
   * @param className
   *          class name to look up, not {@code null}
   * @return the filename, not {@code null}
   */
  String nameFor (String className);

}