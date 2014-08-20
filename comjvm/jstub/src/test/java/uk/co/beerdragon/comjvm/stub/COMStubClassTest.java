/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.stub;

import java.util.Iterator;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the {@link COMStubClass} class.
 */
@Test
public class COMStubClassTest {

  /**
   * Tests {@link COMStubClass#of} creates instances with appropriate caching.
   */
  public void testConstruction () {
    final COMStubClass<?> a1 = COMStubClass.of (Iterable.class);
    final COMStubClass<?> b = COMStubClass.of (Iterator.class);
    final COMStubClass<?> a2 = COMStubClass.of (Iterable.class);
    Assert.assertSame (a1, a2);
    Assert.assertNotSame (b, a1);
  }

}