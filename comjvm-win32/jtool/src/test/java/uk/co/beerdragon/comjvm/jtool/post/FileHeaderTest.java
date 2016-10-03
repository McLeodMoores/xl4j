/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.post;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the {@link FileHeader} class.
 */
@Test
public class FileHeaderTest {

  /**
   * Tests the file reading constructor and behaviour of {@link FileHeader#postProcess} to prepend
   * the contents read.
   */
  public void testPostProcess () throws IOException {
    final List<String> input = Arrays.asList ("Foo", "Bar");
    final PostProcessingStrategy instance = new FileHeader (new StringReader ("Woot\nDog\n"));
    final List<String> output = instance.postProcess (input);
    Assert.assertEquals (output, Arrays.asList ("Woot", "Dog", "Foo", "Bar"));
  }

}