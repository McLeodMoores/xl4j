package com.mcleodmoores.xl4j.v1;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.testng.annotations.Test;
import org.testng.collections.Lists;

import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLBigData;
import com.mcleodmoores.xl4j.v1.api.values.XLBoolean;
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLInteger;
import com.mcleodmoores.xl4j.v1.api.values.XLLocalReference;
import com.mcleodmoores.xl4j.v1.api.values.XLMissing;
import com.mcleodmoores.xl4j.v1.api.values.XLMultiReference;
import com.mcleodmoores.xl4j.v1.api.values.XLNil;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLRange;
import com.mcleodmoores.xl4j.v1.api.values.XLSheetId;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.api.values.XLValueVisitor;
import com.mcleodmoores.xl4j.v1.api.values.XLValueVisitorAdapter;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit test for XLValueVisitor[Adapter].
 */
public class XLValueVisitorTests {
  private static final List<XLValue> VALUES = Lists.newArrayList(
      XLBigData.of("Hello"), XLBoolean.from(true), XLError.Name, XLInteger.of(25),
      XLLocalReference.of(XLRange.ofCell(0, 0)), XLMissing.INSTANCE,
      XLMultiReference.of(XLSheetId.of(1234), XLRange.ofCell(0, 0)), XLNil.INSTANCE,
      XLNumber.of(987.654321), XLString.of("Hello World"), XLArray.of(new XLValue[][] { { XLError.NA } })
      );

  private static final List<String> EXPECTED = Lists.newArrayList(
      "XLBigData", "XLBoolean", "XLError", "XLInteger", "XLLocalReference", "XLMissing",
      "XLMultiReference", "XLNil", "XLNumber", "XLString", "XLArray", "XLObject"
      );

  /**
   * Tests the XLValue visitor.
   */
  @Test
  public void testVisitor() {
    final XLValueVisitor<String> visitor = new XLValueVisitor<String>() {
      @Override
      public String visitXLString(final XLString value) {
        return "XLString";
      }

      @Override
      public String visitXLBoolean(final XLBoolean value) {
        return "XLBoolean";
      }

      @Override
      public String visitXLBigData(final XLBigData value) {
        return "XLBigData";
      }

      @Override
      public String visitXLError(final XLError value) {
        return "XLError";
      }

      @Override
      public String visitXLInteger(final XLInteger value) {
        return "XLInteger";
      }

      @Override
      public String visitXLLocalReference(final XLLocalReference value) {
        return "XLLocalReference";
      }

      @Override
      public String visitXLMissing(final XLMissing value) {
        return "XLMissing";
      }

      @Override
      public String visitXLNil(final XLNil value) {
        return "XLNil";
      }

      @Override
      public String visitXLNumber(final XLNumber value) {
        return "XLNumber";
      }

      @Override
      public String visitXLMultiReference(final XLMultiReference value) {
        return "XLMultiReference";
      }

      @Override
      public String visitXLArray(final XLArray value) {
        return "XLArray";
      }

      @Override
      public String visitXLObject(final XLObject value) {
        return "XLObject";
      }
    };

    final Iterator<String> iter = EXPECTED.iterator();
    for (final XLValue value : VALUES) {
      final String expected = iter.next();
      final String actual = value.accept(visitor);
      assertEquals(actual, expected);
    }
  }

  /**
   * Tests the visitor adapter.
   */
  @Test
  public void testAdapter() {
    final XLValueVisitorAdapter<Void> adapter = new XLValueVisitorAdapter<>();
    final Iterator<String> iter = EXPECTED.iterator();
    for (final XLValue value : VALUES) {
      final String expected = iter.next();
      try {
        value.accept(adapter);
      } catch (final XL4JRuntimeException e4jre) {
        assertTrue(e4jre.getMessage().contains(expected), "Error message \"" + e4jre.getMessage() + "\" should contain the string \"" + expected + "\":");
      }
    }
  }


}
