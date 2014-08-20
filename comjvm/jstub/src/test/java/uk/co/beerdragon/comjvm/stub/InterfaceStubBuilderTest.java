/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.stub;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import uk.co.beerdragon.comjvm.COMHostSession;
import uk.co.beerdragon.comjvm.ex.NotImplementedException;
import uk.co.beerdragon.comjvm.util.ClassMethods;

/**
 * Tests the {@link InterfaceStubBuilder} class.
 */
@Test
public class InterfaceStubBuilderTest {

  public static interface Base {

    int bar ();

  }

  public static interface Sample extends Base {

    int foo ();

    void bar (int i);

  }

  /**
   * Tests the build logic can create a class with a viable constructor that will pass calls onto a
   * COM host for dispatch.
   */
  public void testBuild () throws Exception {
    final InterfaceStubBuilder<Sample> builder = new InterfaceStubBuilder<Sample> (Sample.class);
    final Class<? extends Sample> stubClass = builder
        .build (new ClassMethods (Sample.class).all ());

    // Verify constructor; <init>(COMHostSession session, int[] dispIds, int objectId)
    final Constructor<?>[] constructors = stubClass.getConstructors ();
    Assert.assertEquals (constructors.length, 1);
    final Class<?>[] types = constructors[0].getParameterTypes ();
    Assert.assertEquals (types[0], COMHostSession.class);
    Assert.assertEquals (types[1], Array.newInstance (Integer.TYPE, 0).getClass ());
    Assert.assertEquals (types[2], Integer.TYPE);

    Method method;

    // Verify the methods from Sample exist
    method = stubClass.getMethod ("foo");
    Assert.assertEquals (method.getModifiers (), Modifier.PUBLIC);
    Assert.assertSame (method.getDeclaringClass (), stubClass);
    Assert.assertEquals (method.getReturnType (), Integer.TYPE);
    method = stubClass.getMethod ("bar", Integer.TYPE);
    Assert.assertEquals (method.getModifiers (), Modifier.PUBLIC);
    Assert.assertSame (method.getDeclaringClass (), stubClass);
    Assert.assertEquals (method.getReturnType (), Void.TYPE);

    // Verify the method from Base exists
    method = stubClass.getMethod ("bar");
    Assert.assertEquals (method.getModifiers (), Modifier.PUBLIC);
    Assert.assertSame (method.getDeclaringClass (), stubClass);
    Assert.assertEquals (method.getReturnType (), Integer.TYPE);

    // Verify the methods from Object exist
    method = stubClass.getMethod ("equals", Object.class);
    Assert.assertEquals (method.getModifiers (), Modifier.PUBLIC);
    Assert.assertSame (method.getDeclaringClass (), stubClass);
    Assert.assertEquals (method.getReturnType (), Boolean.TYPE);
    method = stubClass.getMethod ("finalize");
    Assert.assertEquals (method.getModifiers (), Modifier.PUBLIC);
    Assert.assertSame (method.getDeclaringClass (), stubClass);
    Assert.assertEquals (method.getReturnType (), Void.TYPE);
    method = stubClass.getMethod ("hashCode");
    Assert.assertEquals (method.getModifiers (), Modifier.PUBLIC);
    Assert.assertSame (method.getDeclaringClass (), stubClass);
    Assert.assertEquals (method.getReturnType (), Integer.TYPE);
    method = stubClass.getMethod ("toString");
    Assert.assertEquals (method.getModifiers (), Modifier.PUBLIC);
    Assert.assertSame (method.getDeclaringClass (), stubClass);
    Assert.assertEquals (method.getReturnType (), String.class);

    // Try instantiating the class
    final int[] dispIds = new int[8];
    for (int i = 0; i < dispIds.length; i++) {
      dispIds[i] = i;
    }
    final COMHostSession session = Mockito.mock (COMHostSession.class);
    final Object instance = constructors[0].newInstance (session, dispIds, 42);
    Assert.assertTrue (instance instanceof Sample);

    // Try calling the methods
    final Sample sample = (Sample)instance;
    Mockito.when (session.dispatch0I (42, 0)).thenReturn (69);
    Mockito.doReturn (-1).when (session).dispatch1I (42, 3, new Object[] { null });
    Mockito.doReturn (314).when (session).dispatch0I (42, 5);
    Mockito.doReturn (999).when (session).dispatch0I (42, 6);
    Mockito.when (session.dispatch0A (42, 7)).thenReturn ("Foo");
    Assert.assertEquals (sample.bar (), 69);
    sample.bar (101);
    Mockito.verify (session).dispatch1V (42, 1, new int[] { 101 });
    Assert.assertEquals (sample.equals (null), true);
    Assert.assertEquals (sample.foo (), 314);
    Assert.assertEquals (sample.hashCode (), 999);
    Assert.assertEquals (sample.toString (), "Foo");
  }

  private Sample instance () throws Throwable {
    final InterfaceStubBuilder<Sample> builder = new InterfaceStubBuilder<Sample> (Sample.class);
    final Class<? extends Sample> stubClass = builder
        .build (new ClassMethods (Sample.class).all ());
    final int[] dispIds = new int[8];
    Arrays.fill (dispIds, -1);
    final COMHostSession session = Mockito.mock (COMHostSession.class);
    return stubClass.getConstructor (COMHostSession.class, int[].class, Integer.TYPE).newInstance (
        session, dispIds, 42);
  }

  /**
   * Tests that an interface method not implemented by COM throws an exception.
   */
  @Test (expectedExceptions = NotImplementedException.class)
  public void testNotImplementedSampleFoo () throws Throwable {
    instance ().foo ();
  }

  /**
   * Tests that an interface method not implemented by COM throws an exception.
   */
  @Test (expectedExceptions = NotImplementedException.class)
  public void testNotImplementedSampleBar () throws Throwable {
    instance ().bar (42);
  }

  /**
   * Tests that an interface method not implemented by COM throws an exception.
   */
  @Test (expectedExceptions = NotImplementedException.class)
  public void testNotImplementedBarBar () throws Throwable {
    instance ().bar ();
  }

  /**
   * Tests that an object method not implemented by COM uses the super-class.
   */
  public void testNotImplementedEquals () throws Throwable {
    final Sample sample = instance ();
    Assert.assertFalse (sample.equals (null));
    Assert.assertTrue (sample.equals (sample));
    Assert.assertFalse (sample.equals (instance ()));
  }

  /**
   * Tests that an object method not implemented by COM uses the super-class.
   */
  public void testNotImplementedHashCode () throws Throwable {
    instance ().hashCode ();
  }

  /**
   * Tests that an object method not implemented by COM uses the super-class.
   */
  public void testNotImplementedToString () throws Throwable {
    Assert.assertNotNull (instance ().toString ());
  }

}