/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.filename;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the {@link FilePerClassStrategy} class.
 */
@Test
public class FilePerClassStrategyTest {

  /**
   * Tests {@link FilePerClassStrategy#nameFor} handles reasonable inputs correctly.
   */
  public void testNameFor () {
    final FilenameStrategy strategy = new FilePerClassStrategy ();
    Assert.assertEquals (strategy.nameFor ("java.util.Set"), "JavaUtilSet");
    Assert.assertEquals (strategy.nameFor ("java.util.HashSet"), "JavaUtilHashSet");
    Assert.assertEquals (strategy.nameFor ("java.lang.Iterable"), "JavaLangIterable");
    Assert.assertEquals (strategy.nameFor ("Foo"), "JavaLangFoo");
  }

}