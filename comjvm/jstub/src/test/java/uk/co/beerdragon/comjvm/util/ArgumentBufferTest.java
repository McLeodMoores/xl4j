/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.util;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the {@link ArgumentBuffer} class.
 */
@Test
public class ArgumentBufferTest {

  /**
   * Tests the static allocation method.
   */
  public void testAlloc () {
    ArgumentBuffer args = ArgumentBuffer.alloc (4);
    Assert.assertTrue (args.a.length >= 4);
    args = ArgumentBuffer.alloc (32);
    Assert.assertTrue (args.a.length >= 32);
  }

}