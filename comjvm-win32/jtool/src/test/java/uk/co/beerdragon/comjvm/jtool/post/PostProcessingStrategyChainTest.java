/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.post;

import java.util.Collections;
import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the {@link PostProcessingStrategyChain} class.
 */
@Test
public class PostProcessingStrategyChainTest {

  /**
   * Tests a chain of two post processors applies both in the correct order.
   */
  public void testComposition () {
    final PostProcessingStrategy post1 = Mockito.mock (PostProcessingStrategy.class);
    final PostProcessingStrategy post2 = Mockito.mock (PostProcessingStrategy.class);
    final PostProcessingStrategy chain = new PostProcessingStrategyChain (post1, post2);
    final List<String> in = Collections.singletonList ("Foo");
    Mockito.when (post1.postProcess (in)).thenReturn (Collections.singletonList ("Bar"));
    Mockito.when (post2.postProcess (Collections.singletonList ("Bar"))).thenReturn (
        Collections.singletonList ("Cow"));
    final List<String> out = chain.postProcess (in);
    Assert.assertEquals (out, Collections.singletonList ("Cow"));
  }

}