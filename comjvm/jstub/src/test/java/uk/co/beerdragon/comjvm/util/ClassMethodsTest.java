/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.util;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the {@link ClassMethods} class.
 */
@Test
public class ClassMethodsTest {

  /**
   * Test interface for building a dispatch table from. The full method set, of this alone, is:
   * <ol>
   * <li>bar(I)V
   * <li>clone()Ljava/lang/Object;
   * <li>equals(Ljava/lang/Object;)Z
   * <li>finalize()V
   * <li>foo()I
   * <li>hashCode()I
   * <li>toString()Ljava/lang/String;
   * </ol>
   */
  public interface SampleIFace {

    int foo ();

    void bar (int i);

  }

  /**
   * Test class for building a dispatch table from. The full method set, of this alone, is:
   * <ol>
   * <li>clone()Ljava/lang/Object;
   * <li>equals(Ljava/lang/Object;)Z
   * <li>finalize()V
   * <li>hashCode()I
   * <li>toString()Ljava/lang/String;
   * </ol>
   */
  public class SampleBase {

    public final void bar (final int i) {
    }

  }

  /**
   * Test class for building a dispatch table from. The full method set, of this alone, is:
   * <ol>
   * <li>clone()Ljava/lang/Object;
   * <li>equals(Ljava/lang/Object;)Z
   * <li>finalize()V
   * <li>foo()I
   * <li>hashCode()I
   * <li>toString()Ljava/lang/String;
   * </ol>
   */
  public class Sample extends SampleBase implements SampleIFace {

    @Override
    public int foo () {
      return 0;
    }

  }

  /**
   * Tests the collation from an interface.
   */
  public void testInterface () {
    final ClassMethods methods = new ClassMethods (SampleIFace.class);
    Assert.assertEquals (methods.size (), 7);
    Assert.assertEquals (methods.get ("hashCode()I"), (Integer)5);
    Assert.assertEquals (methods.get ("bar()V"), null);
  }

  /**
   * Tests the collation from a simple class.
   */
  public void testClass1 () {
    final ClassMethods methods = new ClassMethods (SampleBase.class);
    Assert.assertEquals (methods.size (), 5);
    Assert.assertEquals (methods.get ("hashCode()I"), (Integer)3);
    Assert.assertEquals (methods.get ("bar()V"), null);
  }

  /**
   * Tests the collation from a complex class (extending another, and implementing an interface).
   */
  public void testClass2 () {
    final ClassMethods methods = new ClassMethods (Sample.class);
    Assert.assertEquals (methods.size (), 6);
    Assert.assertEquals (methods.get ("hashCode()I"), (Integer)4);
    Assert.assertEquals (methods.get ("bar()V"), null);
  }

  /**
   * Checks that all methods have a deterministic index.
   */
  public void testMethods () {
    final ClassMethods instance = new ClassMethods (SampleIFace.class);
    for (final ClassMethods.MethodInfo method : instance.all ()) {
      Assert.assertNotNull (method.getIndex ());
      Assert.assertNotNull (method.getMethod ());
    }
    // TODO: check all indices are unique
  }

}