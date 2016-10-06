/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.msvc;

import org.testng.Assert;
import org.testng.annotations.Test;

import uk.co.beerdragon.comjvm.jtool.JavaType;

/**
 * Tests the {@link HungarianNamingPrefix} class.
 */
@Test
public class HungarianNamingPrefixTest {

  /**
   * Tests the conversion to input parameter types.
   */
  @Test
  public void testToIdlParameterType () {
    final JavaType.Visitor<String> hnp = new HungarianNamingPrefix ();
    Assert.assertEquals (JavaType.BOOLEAN_TYPE.accept (hnp), "f");
    Assert.assertEquals (JavaType.CHAR_TYPE.accept (hnp), "ch");
    Assert.assertEquals (JavaType.BYTE_TYPE.accept (hnp), "b");
    Assert.assertEquals (JavaType.SHORT_TYPE.accept (hnp), "w");
    Assert.assertEquals (JavaType.INT_TYPE.accept (hnp), "l");
    Assert.assertEquals (JavaType.LONG_TYPE.accept (hnp), "ll");
    Assert.assertEquals (JavaType.FLOAT_TYPE.accept (hnp), "f");
    Assert.assertEquals (JavaType.DOUBLE_TYPE.accept (hnp), "d");
    Assert.assertEquals (new JavaType.ObjectType ("java.lang.String").accept (hnp), "p");
    Assert.assertEquals (new JavaType.ArrayType (JavaType.INT_TYPE).accept (hnp), "a");
  }

}