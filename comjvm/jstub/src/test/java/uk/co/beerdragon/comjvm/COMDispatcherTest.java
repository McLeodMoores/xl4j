/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import uk.co.beerdragon.comjvm.util.ClassMethodsTest;

/**
 * Tests the {@link COMDispatcher} class.
 */
@Test
public class COMDispatcherTest {

  /**
   * Tests the dispatch identifiers when mapping methods from an interface.
   */
  public void testDispIdsInterface () {
    final COMDispatcher<ClassMethodsTest.SampleIFace> dispatcher = new COMDispatcher<ClassMethodsTest.SampleIFace> (
        Mockito.mock (COMHostSession.class), ClassMethodsTest.SampleIFace.class, new String[] {
            "foo()J", "foo([I)I", "bar(I)V", "Bar(I)V", "foo()I" });
    final int[] dispIds = dispatcher.copyDispIds ();
    Assert.assertEquals (dispIds, new int[] { 2, -1, -1, -1, 4, -1, -1 });
  }

  /**
   * Tests the dispatch identifiers when mapping methods from a class.
   */
  public void testDispIdsClass1 () {
    final COMDispatcher<ClassMethodsTest.SampleBase> dispatcher = new COMDispatcher<ClassMethodsTest.SampleBase> (
        Mockito.mock (COMHostSession.class), ClassMethodsTest.SampleBase.class, new String[] {
            "foo()J", "foo([I)I", "bar(I)V", "Bar(I)V", "foo()I" });
    final int[] dispIds = dispatcher.copyDispIds ();
    Assert.assertEquals (dispIds, new int[] { -1, -1, -1, -1, -1 });
  }

  /**
   * Tests the dispatch identifiers when mapping methods from a class.
   */
  public void testDispIdsClass2 () {
    final COMDispatcher<ClassMethodsTest.Sample> dispatcher = new COMDispatcher<ClassMethodsTest.Sample> (
        Mockito.mock (COMHostSession.class), ClassMethodsTest.Sample.class, new String[] {
            "foo()J", "foo([I)I", "bar(I)V", "Bar(I)V", "foo()I" });
    final int[] dispIds = dispatcher.copyDispIds ();
    Assert.assertEquals (dispIds, new int[] { -1, -1, -1, 4, -1, -1 });
  }

}