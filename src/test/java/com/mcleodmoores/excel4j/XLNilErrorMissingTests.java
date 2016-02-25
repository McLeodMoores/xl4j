package com.mcleodmoores.excel4j;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.values.XLError;
import com.mcleodmoores.excel4j.values.XLMissing;
import com.mcleodmoores.excel4j.values.XLNil;

/**
 * Unit tests for XLNil, XLMissing and a couple of comparisons with XLError.
 */
public class XLNilErrorMissingTests {

  @Test
  public void testNil() {
    final XLNil nil = XLNil.INSTANCE;
    final XLNil nil2 = XLNil.INSTANCE;
    Assert.assertEquals(nil, nil);
    Assert.assertEquals(nil.hashCode(), nil.hashCode());
    Assert.assertEquals(nil, nil2);
    Assert.assertEquals(nil.hashCode(), nil2.hashCode());

    Assert.assertNotEquals(null, nil);
    Assert.assertNotEquals(XLError.Div0, nil);
  }

  @Test
  public void testNilToString() {
    Assert.assertEquals(XLNil.INSTANCE.toString(), "XLNil");
  }

  @Test
  public void testMissing() {
    final XLMissing missing = XLMissing.INSTANCE;
    final XLMissing missing2 = XLMissing.INSTANCE;
    Assert.assertEquals(missing, missing);
    Assert.assertEquals(missing, missing2);
    Assert.assertEquals(missing.hashCode(), missing.hashCode());
    Assert.assertEquals(missing.hashCode(), missing2.hashCode());

    Assert.assertNotEquals(missing, null);
    Assert.assertNotEquals(missing, XLError.NA);
  }

  @Test
  public void testMissingToString() {
    Assert.assertEquals(XLMissing.INSTANCE.toString(), "XLMissing");
  }
}
