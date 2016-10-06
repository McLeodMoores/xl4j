/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */

package uk.co.beerdragon.comjvm.jtool.msvc;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import uk.co.beerdragon.comjvm.jtool.post.PostProcessingStrategy;

/**
 * Tests the {@link IdlPostProcessor} class.
 */
@Test
public class IdlPostProcessorTest {

  /**
   * Tests that all interface declarations are forward declared.
   */
  public void testGatherForwardInterfaces () {
    final PostProcessingStrategy processor = new IdlPostProcessor (new File (".")) {

      @Override
      boolean idlExists (final String name) {
        return false;
      }
    };
    final List<String> result = processor.postProcess (Arrays.asList (
        "] interface IJavaLangFoo : IJavaLangObject {", "}",
        "interface IJJavaLangFoo : IJJavaLangObject {", "}"));
    Assert.assertTrue (result.contains ("interface IJavaLangFoo;"));
    Assert.assertTrue (result.contains ("interface IJJavaLangFoo;"));
  }

  /**
   * Tests that any interfaces from other packages are imported.
   */
  public void testExternalInterfaceImports () {
    final PostProcessingStrategy processor = new IdlPostProcessor (new File (".")) {

      @Override
      boolean idlExists (final String name) {
        return "JavaUtil.idl".equals (name);
      }
    };
    final List<String> result = processor.postProcess (Arrays.asList (
        "interface IJavaLangFoo : IJavaUtilBar {", "}",
        "] interface IJJavaLangFoo : IJJavaUtilBar {", "}"));
    Assert.assertTrue (result.contains ("#include \"JavaUtil.idl\""));
  }

}
