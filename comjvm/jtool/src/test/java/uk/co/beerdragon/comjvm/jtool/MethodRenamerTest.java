/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the {@link MethodRenamer} class.
 */
@Test
public class MethodRenamerTest {

  public void testInitialCaps () {
    final MethodRenamer renamer = new MethodRenamer (Arrays.asList ("hashCode()I"));
    Assert.assertEquals (renamer.nameFor ("hashCode()I"), "HashCode");
  }

  public void testIJObjectCollisions () {
    final MethodRenamer renamer = new MethodRenamer (Arrays.asList ("addRef()V", "release()V",
        "queryInterface()V", "queryJNI()V"));
    Assert.assertEquals (renamer.nameFor ("addRef()V"), "_AddRef");
    Assert.assertEquals (renamer.nameFor ("release()V"), "_Release");
    Assert.assertEquals (renamer.nameFor ("queryInterface()V"), "_QueryInterface");
    Assert.assertEquals (renamer.nameFor ("queryJNI()V"), "_QueryJNI");
  }

  public void testMethodOverloads () {
    final MethodRenamer renamer = new MethodRenamer (
        Arrays.asList ("foo(I)V", "foo(J)V", "foo(D)V"));
    Assert.assertEquals (renamer.nameFor ("foo(I)V"), "Foo2");
    Assert.assertEquals (renamer.nameFor ("foo(J)V"), "Foo3");
    Assert.assertEquals (renamer.nameFor ("foo(D)V"), "Foo1");
  }

  public void testMethodOverloadCollisions () {
    final MethodRenamer renamer = new MethodRenamer (Arrays.asList ("foo(I)V", "foo2(I)V",
        "foo(J)V", "foo2(J)V", "addRef()V", "_addRef()V"));
    Assert.assertEquals (renamer.nameFor ("foo(I)V"), "Foo1");
    Assert.assertEquals (renamer.nameFor ("foo2(I)V"), "Foo3");
    Assert.assertEquals (renamer.nameFor ("foo(J)V"), "Foo2");
    Assert.assertEquals (renamer.nameFor ("foo2(J)V"), "Foo4");
    Assert.assertEquals (renamer.nameFor ("addRef()V"), "_AddRef2");
    Assert.assertEquals (renamer.nameFor ("_addRef()V"), "_AddRef1");
  }

}