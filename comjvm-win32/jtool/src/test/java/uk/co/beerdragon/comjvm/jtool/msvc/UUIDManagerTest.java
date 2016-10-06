/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.msvc;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the {@link UUIDManager} class.
 */
@Test
public class UUIDManagerTest {

  public void testBasicOperation () {
    final UUIDManager manager = new UUIDManager ();
    final String a = manager.uuidFor ("IJFooBar");
    Assert.assertNotNull (a);
    final String b = manager.uuidFor ("IJBarFoo");
    Assert.assertNotNull (b);
    Assert.assertNotEquals (a, b);
    final String c = manager.uuidFor ("IJFooBar");
    Assert.assertEquals (c, a);
  }

  public void testLoad () throws IOException {
    final Reader r = new StringReader (
        "IBar=6c2baf02-f25a-471e-b1c0-15f6c2d200a9\nIFoo=a788d7b9-eac3-4911-8cc9-8676f8b54f32\n");
    final UUIDManager manager = new UUIDManager (r);
    Assert.assertEquals (manager.uuidFor ("IFoo"), "a788d7b9-eac3-4911-8cc9-8676f8b54f32");
    Assert.assertEquals (manager.uuidFor ("IBar"), "6c2baf02-f25a-471e-b1c0-15f6c2d200a9");
  }

  public void testSave () throws IOException {
    final UUIDManager manager = new UUIDManager ();
    final String a = manager.uuidFor ("IFoo");
    final String b = manager.uuidFor ("IBar");
    final CharArrayWriter w = new CharArrayWriter ();
    manager.save (w);
    Assert.assertEquals (
        new String (w.toCharArray ()),
        "IBar=" + b + System.getProperty ("line.separator") + "IFoo=" + a
            + System.getProperty ("line.separator"));
  }

}