/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the {@link JavaType} class.
 */
@Test
public class JavaTypeTest {

  /**
   * Tests the conversion to input parameter types.
   */
  public void testToString () {
    Assert.assertEquals (JavaType.BOOLEAN_TYPE.toString (), "BOOL");
    Assert.assertEquals (JavaType.CHAR_TYPE.toString (), "CHAR");
    Assert.assertEquals (JavaType.BYTE_TYPE.toString (), "I1");
    Assert.assertEquals (JavaType.SHORT_TYPE.toString (), "I2");
    Assert.assertEquals (JavaType.INT_TYPE.toString (), "I4");
    Assert.assertEquals (JavaType.LONG_TYPE.toString (), "I8");
    Assert.assertEquals (JavaType.FLOAT_TYPE.toString (), "FLOAT");
    Assert.assertEquals (JavaType.DOUBLE_TYPE.toString (), "DOUBLE");
    Assert.assertEquals (new JavaType.ObjectType ("java.lang.String").toString (),
        "java.lang.String");
    Assert.assertEquals (new JavaType.ArrayType (JavaType.INT_TYPE).toString (), "I4[]");
  }

}