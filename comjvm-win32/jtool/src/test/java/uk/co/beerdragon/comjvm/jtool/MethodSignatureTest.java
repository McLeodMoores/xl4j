/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the {@link MethodSignature} class.
 */
@Test
public class MethodSignatureTest {

  /**
   * Tests the constructor can parse primitive and object reference arguments.
   */
  public void testPrimitiveAndObjectArguments () {
    final MethodSignature signature = new MethodSignature ("(ZCBSIJFDLjava/lang/String;[I)V");
    final List<JavaType> arguments = signature.getArguments ();
    Assert.assertEquals (arguments.get (0), JavaType.BOOLEAN_TYPE);
    Assert.assertEquals (arguments.get (1), JavaType.CHAR_TYPE);
    Assert.assertEquals (arguments.get (2), JavaType.BYTE_TYPE);
    Assert.assertEquals (arguments.get (3), JavaType.SHORT_TYPE);
    Assert.assertEquals (arguments.get (4), JavaType.INT_TYPE);
    Assert.assertEquals (arguments.get (5), JavaType.LONG_TYPE);
    Assert.assertEquals (arguments.get (6), JavaType.FLOAT_TYPE);
    Assert.assertEquals (arguments.get (7), JavaType.DOUBLE_TYPE);
    Assert.assertEquals (arguments.get (8), new JavaType.ObjectType ("java.lang.String"));
    Assert.assertEquals (arguments.get (9), new JavaType.ArrayType (JavaType.INT_TYPE));
  }

  /**
   * Tests parsing the return type from a method signature.
   */
  public void testReturnTypes () {
    Assert.assertEquals (new MethodSignature ("()V").getReturn (), null);
    Assert.assertEquals (new MethodSignature ("()Z").getReturn (), JavaType.BOOLEAN_TYPE);
    Assert.assertEquals (new MethodSignature ("()C").getReturn (), JavaType.CHAR_TYPE);
    Assert.assertEquals (new MethodSignature ("()B").getReturn (), JavaType.BYTE_TYPE);
    Assert.assertEquals (new MethodSignature ("()S").getReturn (), JavaType.SHORT_TYPE);
    Assert.assertEquals (new MethodSignature ("()I").getReturn (), JavaType.INT_TYPE);
    Assert.assertEquals (new MethodSignature ("()J").getReturn (), JavaType.LONG_TYPE);
    Assert.assertEquals (new MethodSignature ("()F").getReturn (), JavaType.FLOAT_TYPE);
    Assert.assertEquals (new MethodSignature ("()D").getReturn (), JavaType.DOUBLE_TYPE);
    Assert.assertEquals (new MethodSignature ("()Ljava/lang/String;").getReturn (),
        new JavaType.ObjectType ("java.lang.String"));
    Assert.assertEquals (new MethodSignature ("()[I").getReturn (), new JavaType.ArrayType (
        JavaType.INT_TYPE));
  }

}