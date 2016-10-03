/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.filename;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the {@link FileExtensionStrategy} class.
 */
@Test
public class FileExtensionStrategyTest {

  /**
   * Tests {@link FileExtensionStrategy#nameFor} appends the extension.
   */
  public void testNameFor () {
    final FilenameStrategy underlying = Mockito.mock (FilenameStrategy.class);
    final FilenameStrategy strategy = new FileExtensionStrategy (underlying, "h");
    Mockito.when (underlying.nameFor ("java.lang.Iterable")).thenReturn ("Foo");
    Assert.assertEquals (strategy.nameFor ("java.lang.Iterable"), "Foo.h");
  }

}