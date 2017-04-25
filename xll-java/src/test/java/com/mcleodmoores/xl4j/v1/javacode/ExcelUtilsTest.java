/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLMissing;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.simulator.MockFunctionProcessor;

/**
 * Unit tests for {@link ExcelUtils}.
 */
public class ExcelUtilsTest {
  private static final MockFunctionProcessor PROCESSOR = MockFunctionProcessor.getInstance();

  /**
   * Tests the "Array" function.
   */
  @Test
  public void testArrayFunction() {
    final XLValue[] inputs = new XLValue[] { XLString.of("LABEL"), XLNumber.of(10), XLNumber.of(30), XLNumber.of(40), XLNumber.of(60)};
    final XLValue result = PROCESSOR.invoke("Array", inputs);
    assertTrue(result instanceof XLArray);
    final XLValue[][] values = ((XLArray) result).getArray();
    assertEquals(values.length, 1);
    for (int i = 0; i < values[0].length; i++) {
      assertEquals(values[0][i], inputs[i]);
    }
  }

  /**
   * Tests the "Null" function.
   */
  @Test
  public void testNullFunction() {
    final XLValue result = PROCESSOR.invoke("Null", new XLValue[0]);
    assertSame(result, XLMissing.INSTANCE);
  }

  /**
   * Tests the "Prefix" function.
   */
  @Test
  public void testPrefixFunctionForSingleString() {
    final XLValue prefix = XLString.of("/");
    final XLValue result = PROCESSOR.invoke("Prefix", prefix, XLString.of("LABEL"));
    assertTrue(result instanceof XLArray);
    final XLValue[][] values = ((XLArray) result).getArray();
    assertEquals(values.length, 1);
    assertEquals(values[0].length, 1);
    final XLString s = (XLString) values[0][0];
    assertEquals(s.getValue(), "/LABEL");
  }

  /**
   * Tests the "Prefix" function.
   */
  @Test
  public void testPrefixFunctionForSingleNumber() {
    final XLValue prefix = XLString.of("/");
    final XLValue result = PROCESSOR.invoke("Prefix", prefix, XLNumber.of(0));
    assertTrue(result instanceof XLArray);
    final XLValue[][] values = ((XLArray) result).getArray();
    assertEquals(values.length, 1);
    assertEquals(values[0].length, 1);
    final XLNumber n = (XLNumber) values[0][0];
    assertEquals(n.getValue(), 0, 1e-15);
  }

  /**
   * Tests the "Prefix" function.
   */
  @Test
  public void testPrefixFunctionForMultipleStrings() {
    final XLValue prefix = XLString.of("/");
    final XLValue result = PROCESSOR.invoke("Prefix", prefix, XLString.of("LABEL_1"), XLString.of("LABEL_2"), XLString.of("LABEL_3"), XLString.of("LABEL_4"));
    assertTrue(result instanceof XLArray);
    final XLValue[][] values = ((XLArray) result).getArray();
    assertEquals(values.length, 1);
    assertEquals(values[0].length, 4);
    for (int i = 0; i < 4; i++) {
      final XLString s = (XLString) values[0][i];
      assertEquals(s.getValue(), "/LABEL_" + (i + 1));
    }
  }

  /**
   * Tests the "Prefix" function.
   */
  @Test
  public void testPrefixFunctionForMultipleValues() {
    final XLValue prefix = XLString.of("/");
    final XLValue result = PROCESSOR.invoke("Prefix", prefix, XLString.of("LABEL_1"), XLNumber.of(1), XLNumber.of(2), XLString.of("LABEL_4"));
    assertTrue(result instanceof XLArray);
    final XLValue[][] values = ((XLArray) result).getArray();
    assertEquals(values.length, 1);
    assertEquals(values[0].length, 4);
    assertEquals(((XLString) values[0][0]).getValue(), "/LABEL_1");
    assertEquals(((XLNumber) values[0][1]).getValue(), 1, 1e-15);
    assertEquals(((XLNumber) values[0][2]).getValue(), 2, 1e-15);
    assertEquals(((XLString) values[0][3]).getValue(), "/LABEL_4");
  }

  /**
   * Tests the "Prefix" function.
   */
  @Test
  public void testPrefixFunctionForMultipleValuesWithArray() {
    final XLValue prefix = XLString.of("/");
    final XLArray array = XLArray.of(new XLValue[][] {
      new XLValue[] {XLString.of("ROW_HEADER_1"), XLNumber.of(10)},
      new XLValue[] {XLString.of("ROW_HEADER_2"), XLNumber.of(20)},
      new XLValue[] {XLString.of("ROW_HEADER_3"), XLNumber.of(30)}
    });
    final XLValue result = PROCESSOR.invoke("Prefix", prefix, XLString.of("LABEL_1"), XLNumber.of(1), array);
    assertTrue(result instanceof XLArray);
    final XLValue[][] values = ((XLArray) result).getArray();
    assertEquals(values.length, 1);
    assertEquals(values[0].length, 3);
    assertEquals(((XLString) values[0][0]).getValue(), "/LABEL_1");
    assertEquals(((XLNumber) values[0][1]).getValue(), 1, 1e-15);
    final XLValue[][] resultArray = ((XLArray) values[0][2]).getArray();
    assertEquals(resultArray.length, 3);
    assertEquals(resultArray[0].length, 2);
    assertEquals(((XLString) resultArray[0][0]).getValue(), "/ROW_HEADER_1");
    assertEquals(((XLNumber) resultArray[0][1]).getValue(), 10, 1e-15);
    assertEquals(((XLString) resultArray[1][0]).getValue(), "/ROW_HEADER_2");
    assertEquals(((XLNumber) resultArray[1][1]).getValue(), 20, 1e-15);
    assertEquals(((XLString) resultArray[2][0]).getValue(), "/ROW_HEADER_3");
    assertEquals(((XLNumber) resultArray[2][1]).getValue(), 30, 1e-15);
  }

  /**
   * Tests the "RemoveWhitespace" function.
   */
  @Test
  public void testRemoveWhitespaceFunctionForSingleString() {
    final XLValue result = PROCESSOR.invoke("RemoveWhitespace", XLString.of("LAB \tEL"));
    assertTrue(result instanceof XLArray);
    final XLValue[][] values = ((XLArray) result).getArray();
    assertEquals(values.length, 1);
    assertEquals(values[0].length, 1);
    final XLString s = (XLString) values[0][0];
    assertEquals(s.getValue(), "LABEL");
  }

  /**
   * Tests the "RemoveWhitespace" function.
   */
  @Test
  public void testRemoveWhitespaceFunctionForSingleNumber() {
    final XLValue result = PROCESSOR.invoke("RemoveWhitespace", XLNumber.of(0));
    assertTrue(result instanceof XLArray);
    final XLValue[][] values = ((XLArray) result).getArray();
    assertEquals(values.length, 1);
    assertEquals(values[0].length, 1);
    final XLNumber n = (XLNumber) values[0][0];
    assertEquals(n.getValue(), 0, 1e-15);
  }

  /**
   * Tests the "RemoveWhitespace" function.
   */
  @Test
  public void testRemoveWhitespaceFunctionForMultipleStrings() {
    final XLValue result = PROCESSOR.invoke("RemoveWhitespace", XLString.of(" LABEL_1"), XLString.of("LABEL_2 "),
        XLString.of("\tLABEL_3 "), XLString.of("L\tABEL_4"));
    assertTrue(result instanceof XLArray);
    final XLValue[][] values = ((XLArray) result).getArray();
    assertEquals(values.length, 1);
    assertEquals(values[0].length, 4);
    for (int i = 0; i < 4; i++) {
      final XLString s = (XLString) values[0][i];
      assertEquals(s.getValue(), "LABEL_" + (i + 1));
    }
  }

  /**
   * Tests the "RemoveWhitespace" function.
   */
  @Test
  public void testRemoveWhitespaceFunctionForMultipleValues() {
    final XLValue result = PROCESSOR.invoke("RemoveWhitespace", XLString.of("LA     BEL_1"), XLNumber.of(1), XLNumber.of(2), XLString.of("LABEL_4     "));
    assertTrue(result instanceof XLArray);
    final XLValue[][] values = ((XLArray) result).getArray();
    assertEquals(values.length, 1);
    assertEquals(values[0].length, 4);
    assertEquals(((XLString) values[0][0]).getValue(), "LABEL_1");
    assertEquals(((XLNumber) values[0][1]).getValue(), 1, 1e-15);
    assertEquals(((XLNumber) values[0][2]).getValue(), 2, 1e-15);
    assertEquals(((XLString) values[0][3]).getValue(), "LABEL_4");
  }

  /**
   * Tests the "RemoveWhitespace" function.
   */
  @Test
  public void testRemoveWhitespaceFunctionForMultipleValuesWithArray() {
    final XLArray array = XLArray.of(new XLValue[][] {
      new XLValue[] {XLString.of("RO W_HEADER_1"), XLNumber.of(10)},
      new XLValue[] {XLString.of("ROW_ HEADER_2"), XLNumber.of(20)},
      new XLValue[] {XLString.of("ROW_HEAD ER_3"), XLNumber.of(30)}
    });
    final XLValue result = PROCESSOR.invoke("RemoveWhitespace", XLString.of("\tLABEL_1"), XLNumber.of(1), array);
    assertTrue(result instanceof XLArray);
    final XLValue[][] values = ((XLArray) result).getArray();
    assertEquals(values.length, 1);
    assertEquals(values[0].length, 3);
    assertEquals(((XLString) values[0][0]).getValue(), "LABEL_1");
    assertEquals(((XLNumber) values[0][1]).getValue(), 1, 1e-15);
    final XLValue[][] resultArray = ((XLArray) values[0][2]).getArray();
    assertEquals(resultArray.length, 3);
    assertEquals(resultArray[0].length, 2);
    assertEquals(((XLString) resultArray[0][0]).getValue(), "ROW_HEADER_1");
    assertEquals(((XLNumber) resultArray[0][1]).getValue(), 10, 1e-15);
    assertEquals(((XLString) resultArray[1][0]).getValue(), "ROW_HEADER_2");
    assertEquals(((XLNumber) resultArray[1][1]).getValue(), 20, 1e-15);
    assertEquals(((XLString) resultArray[2][0]).getValue(), "ROW_HEADER_3");
    assertEquals(((XLNumber) resultArray[2][1]).getValue(), 30, 1e-15);
  }
}
