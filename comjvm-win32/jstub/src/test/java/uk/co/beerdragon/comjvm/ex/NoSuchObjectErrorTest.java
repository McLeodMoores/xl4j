/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.ex;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the {@link NoSuchObjectError} class.
 */
@Test
public class NoSuchObjectErrorTest {

  /**
   * Tests the querying methods.
   */
  public void testQueries () {
    final NoSuchObjectError instance = new NoSuchObjectError (42, null);
    Assert.assertEquals (-1701753722, instance.hashCode ());
    Assert.assertEquals ("42", instance.getMessage ());
    Assert.assertEquals ("localhost", instance.getHostDescription ());
    Assert.assertEquals ("No object 42 at localhost", instance.toString ());
  }

  /**
   * Tests the {@link Object#equals} implementation.
   */
  public void testEquals () {
    final NoSuchObjectError a1 = new NoSuchObjectError (42, "a");
    final NoSuchObjectError a2 = new NoSuchObjectError (42, "b");
    final NoSuchObjectError b1 = new NoSuchObjectError (69, "a");
    final NoSuchObjectError b2 = new NoSuchObjectError (69, "b");
    Assert.assertTrue (a1.equals (a1));
    Assert.assertTrue (a1.equals (new NoSuchObjectError (42, "a")));
    Assert.assertFalse (a1.equals (a2));
    Assert.assertFalse (a1.equals (b1));
    Assert.assertFalse (a1.equals (b2));
    Assert.assertFalse (a1.equals (null));
    Assert.assertFalse (a1.equals ("Foo"));
  }

}
