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
 * Tests the {@link FilePerPackageStrategy} class.
 */
@Test
public class FilePerPackageStrategyTest {

  /**
   * Tests {@link FilePerPackageStrategy#nameFor} handles reasonable inputs correctly.
   */
  public void testNameFor () {
    final FilenameStrategy strategy = new FilePerPackageStrategy ();
    Assert.assertEquals (strategy.nameFor ("java.util.Set"), "JavaUtil");
    Assert.assertEquals (strategy.nameFor ("java.util.HashSet"), "JavaUtil");
    Assert.assertEquals (strategy.nameFor ("java.lang.Iterable"), "JavaLang");
    Assert.assertEquals (strategy.nameFor ("Foo"), "JavaLang");
  }

}