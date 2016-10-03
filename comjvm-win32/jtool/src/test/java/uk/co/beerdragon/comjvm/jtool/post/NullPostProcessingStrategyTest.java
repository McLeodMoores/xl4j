/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.post;

import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the {@link NullPostProcessingStrategy} class.
 */
@Test
public class NullPostProcessingStrategyTest {

  /**
   * Tests {@link NullPostProcessingStrategy#postProcess} returns the input unchanged.
   */
  @SuppressWarnings ({ "rawtypes", "unchecked" })
  public void testPostProcess () {
    final List mock = Mockito.mock (List.class);
    final PostProcessingStrategy instance = new NullPostProcessingStrategy ();
    Assert.assertSame (instance.postProcess (mock), mock);
  }

}