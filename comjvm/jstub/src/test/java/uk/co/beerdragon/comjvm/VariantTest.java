/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/**
 * Tests the {@link Variant} class.
 */
@Test
public class VariantTest {

  public void testByte () {
    assertEquals ((byte)42, Variant.of ((byte)42).getByteValue ());
  }

  public void testShort () {
    assertEquals ((short)42, Variant.of ((short)42).getShortValue ());
  }

  public void testInt () {
    assertEquals (42, Variant.of (42).getIntValue ());
  }

  public void testLong () {
    assertEquals (42, Variant.of ((long)42).getLongValue ());
  }

  public void testBoolean () {
    assertEquals (true, Variant.of (true).getBooleanValue ());
    assertEquals (false, Variant.of (false).getBooleanValue ());
  }

  public void testChar () {
    assertEquals ('X', Variant.of ('X').getCharValue ());
  }

  public void testFloat () {
    assertEquals (3.14f, Variant.of (3.14f).getFloatValue ());
  }

  public void testDouble () {
    assertEquals (3.14, Variant.of (3.14).getDoubleValue ());
  }

  public void testObject () {
    assertEquals ("Foo", Variant.of ("Foo").getObjectValue ());
    assertEquals (null, Variant.of (null).getObjectValue ());
  }

}