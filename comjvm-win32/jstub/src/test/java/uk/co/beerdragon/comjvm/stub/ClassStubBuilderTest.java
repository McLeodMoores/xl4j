/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.stub;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Arrays;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import uk.co.beerdragon.comjvm.COMHostSession;
import uk.co.beerdragon.comjvm.util.ClassMethods;

/**
 * Tests the {@link ClassStubBuilder} class.
 */
@Test
public class ClassStubBuilderTest {

  public static class Base {

    public void bar (final int i) {
    }

    public final void foo (final int i) {
    }

  }

  public static class Sample extends Base {

    public int foo () {
      return 42;
    }

    public final int bar () {
      return 42;
    }

  }

  /**
   * Tests the build logic can create a class with a viable constructor that will pass calls onto a
   * COM host for dispatch.
   */
  @Test (enabled = false)
  // TODO: Disabled because there is an exception below
  public void testSample () throws Exception {
    final ClassStubBuilder<Sample> builder = new ClassStubBuilder<Sample> (Sample.class);
    final Class<? extends Sample> stubClass = builder
        .build (new ClassMethods (Sample.class).all ());

    // Verify constructor; <init>(COMHostSession session, int[] dispIds, int objectId)
    final Constructor<?>[] constructors = stubClass.getConstructors ();
    Assert.assertEquals (constructors.length, 1);
    final Class<?>[] types = constructors[0].getParameterTypes ();
    Assert.assertEquals (types[0], COMHostSession.class);
    Assert.assertEquals (types[1], Array.newInstance (Integer.TYPE, 0).getClass ());
    Assert.assertEquals (types[2], Integer.TYPE);

    // TODO: Verify the methods from SAMPLE all exist
    // TODO: Verify the methods from BASE all exist
    // TODO: Verify the methods from OBJECT all exist

    // Try instantiating the class
    final int[] dispIds = new int[7];
    Arrays.fill (dispIds, -1);
    final Object instance = constructors[0].newInstance (Mockito.mock (COMHostSession.class),
        dispIds, 42);
    Assert.assertTrue (instance instanceof Sample);

    // Throw an exception because there are TODOs above
    throw new UnsupportedOperationException ();
  }

}