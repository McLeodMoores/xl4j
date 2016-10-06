/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool;

/**
 * Callback service notifying that an additional class must be processed.
 */
public interface AdditionalClasses {

  /**
   * Requests an additional class be processed.
   * 
   * @param className
   *          class required, not {@code null}
   */
  void add (String className);

}
