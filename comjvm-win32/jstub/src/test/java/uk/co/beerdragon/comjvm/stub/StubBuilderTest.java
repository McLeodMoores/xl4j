/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.stub;

import java.util.HashSet;
import java.util.Set;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import uk.co.beerdragon.comjvm.COMHostSession;
import uk.co.beerdragon.comjvm.ex.NotImplementedException;
import uk.co.beerdragon.comjvm.util.ClassMethods;

/**
 * Tests the {@link StubBuilder} class.
 */
public class StubBuilderTest {

  /**
   * Tests {@link StubBuilder#of} creates instances of the correct type.
   */
  @Test
  public void testConstruction () {
    @SuppressWarnings ("rawtypes")
    final StubBuilder<Set> a = StubBuilder.of (Set.class);
    @SuppressWarnings ("rawtypes")
    final StubBuilder<HashSet> b = StubBuilder.of (HashSet.class);
    Assert.assertNotNull (a);
    Assert.assertNotNull (b);
    Assert.assertEquals (InterfaceStubBuilder.class, a.getClass ());
    Assert.assertEquals (ClassStubBuilder.class, b.getClass ());
  }

  // build() is tested as part of the sub-class tests

  // TODO: Test the edge cases for method generation with various signatures

  public static class FallbackTest {

    @Override
    public String toString () {
      return "FALLBACK";
    }

  }

  /**
   * Tests the fallback logic. The COM dispatch will occur for the first call, throw
   * {@link NotImplementedException} and then be bypassed on subsequent calls.
   */
  @Test
  public void testFallback () throws Throwable {
    final StubBuilder<FallbackTest> builder = StubBuilder.of (FallbackTest.class);
    final Class<? extends FallbackTest> clazz = builder
        .build (new ClassMethods (FallbackTest.class).all ());
    final COMHostSession session = Mockito.mock (COMHostSession.class);
    final FallbackTest stub = clazz
        .getConstructor (COMHostSession.class, int[].class, Integer.TYPE).newInstance (session,
            new int[] { 1, 2, 3, 4, 5 }, 0);
    Mockito.doReturn ("Foo").when (session).dispatch0A (0, 5);
    Assert.assertEquals (stub.toString (), "Foo");
    Mockito.verify (session, Mockito.times (1)).dispatch0A (0, 5);
    Mockito.doThrow (new NotImplementedException ()).when (session).dispatch0A (0, 5);
    Assert.assertEquals (stub.toString (), "FALLBACK");
    Mockito.verify (session, Mockito.times (2)).dispatch0A (0, 5);
    Assert.assertEquals (stub.toString (), "FALLBACK");
    Mockito.verify (session, Mockito.times (2)).dispatch0A (0, 5);
  }

}