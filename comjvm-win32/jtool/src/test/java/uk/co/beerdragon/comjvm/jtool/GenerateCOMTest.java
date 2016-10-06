/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the {@link GenerateCOM} class.
 */
@Test
public class GenerateCOMTest {

  /**
   * Tests {@link GenerateCOM#getLogger} and {@link GenerateCOM#setLogger} behave correctly.
   */
  public void testLogger () {
    final GenerateCOM instance = new GenerateCOM ();
    Assert.assertNotNull (instance.getLogger ());
    final Logger value = Mockito.mock (Logger.class);
    instance.setLogger (value);
    Assert.assertSame (value, instance.getLogger ());
  }

  /**
   * Tests {@link GenerateCOM#getClasses}, {@link GenerateCOM#setClasses},
   * {@link GenerateCOM#addClass} and {@link GenerateCOM#removeClass} behave correctly.
   */
  public void testClasses () {
    final GenerateCOM instance = new GenerateCOM ();
    Assert.assertNotNull (instance.getClasses ());
    Assert.assertTrue (instance.getClasses ().isEmpty ());
    instance.addClass ("java.lang.CharSequence");
    instance.addClass ("java.lang.Iterable");
    Assert.assertEquals (instance.getClasses ().size (), 2);
    instance.removeClass ("java.lang.Iterable");
    Assert.assertEquals (instance.getClasses ().size (), 1);
    instance.setClasses (Arrays.asList ("java.util.Iterator", "java.lang.Comparable"));
    Assert.assertEquals (instance.getClasses ().size (), 2);
  }

  /**
   * Tests {@link GenerateCOM#getOutputPath} and {@link GenerateCOM#setOutputPath} behave correctly.
   */
  public void testOutputPath () {
    final GenerateCOM instance = new GenerateCOM ();
    Assert.assertNotNull (instance.getOutputPath ());
    final String path = "Foo";
    instance.setOutputPath (path);
    Assert.assertSame (instance.getOutputPath (), path);
  }

  /**
   * Tests {@link GenerateCOM#runLogger} passes all root classes to the processor along with any
   * additional classes that the processor requests.
   */
  public void testRun () {
    final GenerateCOM instance = new GenerateCOM ();
    final List<String> processed = new ArrayList<String> (4);
    instance.addClass ("A");
    instance.addClass ("B");
    instance.setClassProcessor (new ClassProcessor () {

      @Override
      public void process (final String className) {
        processed.add (className);
        if ("A".equals (className)) {
          requireAdditionalClass ("X");
          requireAdditionalClass ("Y");
        } else if ("B".equals (className)) {
          requireAdditionalClass ("X");
        }
      }
    });
    instance.setOutputPath ("target");
    instance.run ();
    Assert.assertEquals (processed.size (), 4);
    Assert.assertTrue (processed.containsAll (Arrays.asList ("A", "B", "X", "Y")));
  }

  /**
   * Tests the program entry point.
   */
  public void testEntryPoint () {
    GenerateCOM.main (new String[] { "-dtarget/test-output/sample", "java.lang.Iterable" });
  }

}