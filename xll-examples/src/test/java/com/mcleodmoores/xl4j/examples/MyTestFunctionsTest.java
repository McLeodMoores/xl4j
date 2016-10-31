/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.ExcelFactory;
import com.mcleodmoores.xl4j.simulator.MockFunctionProcessor;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLBoolean;
import com.mcleodmoores.xl4j.values.XLError;
import com.mcleodmoores.xl4j.values.XLLocalReference;
import com.mcleodmoores.xl4j.values.XLMultiReference;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLRange;
import com.mcleodmoores.xl4j.values.XLSheetId;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Unit tests for {@link MyTestFunctions}.
 */
public class MyTestFunctionsTest {
  /** The function processor */
  private MockFunctionProcessor _processor;

  /**
   * Initializes the function processor before the tests are run.
   */
  @BeforeTest
  public void init() {
    _processor = MockFunctionProcessor.getInstance();
  }

  /**
   * Tests String concatenation.
   */
  @Test
  public void testConcatenation() {
    final Object result = _processor.invoke("MyStringCat", XLString.of(" world"), XLString.of("."));
    assertTrue(result instanceof XLString);
    assertEquals(((XLString) result).getValue(), "Hello world.");
  }

  /**
   * Test XOR.
   */
  @Test
  public void testXor() {
    final Object result = _processor.invoke("MyXOR", XLBoolean.FALSE, XLBoolean.TRUE);
    assertTrue(result instanceof XLBoolean);
    assertEquals(result, XLBoolean.TRUE);
  }

  /**
   * Tests the local reference toString() method.
   */
  @Test
  public void testLocalReference() {
    final XLLocalReference ref = XLLocalReference.of(XLRange.of(0, 100, 2, 20));
    final Object result = _processor.invoke("MyLocalReference", ref);
    assertTrue(result instanceof XLString);
    assertEquals(((XLString) result).getValue(), "XLLocalReference[range=XLRange[Range rows=0 to 100, columns=2 to 20]]");
  }

  /**
   * Tests the multi-reference toString() method.
   */
  @Test
  public void testMultiReference() {
    final XLMultiReference ref = XLMultiReference.of(XLSheetId.of(100), XLRange.of(0, 10, 4, 20));
    final Object result = _processor.invoke("MyMultiReference", ref);
    assertTrue(result instanceof XLString);
    assertEquals(((XLString) result).getValue(), "XLMultiReference[sheetId=100, range=XLRange[Range rows=0 to 10, columns=4 to 20]]");
  }

  /**
   * Tests the creation of an array.
   */
  @Test
  public void testArray() {
    final Object result = _processor.invoke("MyArray");
    assertTrue(result instanceof XLArray);
    assertEquals(result,
        XLArray.of(new XLValue[][]{{XLNumber.of(1), XLString.of("Two"), XLNumber.of(3)}, {XLString.of("One"), XLNumber.of(2), XLString.of("3")}}));
  }

  /**
   * Tests list creation. Note that this method returns an object result and so must be retrieved from the heap.
   */
  @Test
  public void testMakeList() {
    final XLArray array =
        XLArray.of(new XLValue[][]{{XLNumber.of(1), XLString.of("Two"), XLNumber.of(3)}, {XLString.of("One"), XLNumber.of(2), XLString.of("3")}});
    final Object result = _processor.invoke("MakeList", array);
    assertTrue(result instanceof XLObject);
    final XLObject listXlObject = (XLObject) result;
    final Object listObject = ExcelFactory.getInstance().getHeap().getObject(listXlObject.getHandle());
    assertTrue(listObject instanceof List);
    assertEquals(listObject, Arrays.asList(XLNumber.of(1), XLString.of("Two"), XLNumber.of(3), XLString.of("One"), XLNumber.of(2), XLString.of("3")));
  }

  /**
   * Tests getting a list element.
   */
  @Test
  public void testListElement() {
    final XLValue result1 = _processor.invoke("MakeList", XLArray.of(new XLValue[][] { { XLString.of("One"), XLString.of("Two") } }));
    assertEquals(result1.getClass(), XLObject.class);
    final XLObject arrayListObj = (XLObject) result1;
    final Object arrayList = ExcelFactory.getInstance().getHeap().getObject(arrayListObj.getHandle());
    assertEquals(arrayList.getClass(), ArrayList.class);
    assertEquals(((List<?>) arrayList).size(), 2);
    final XLValue result2 = _processor.invoke("ListElement",  result1, XLNumber.of(1));
    assertEquals(result2.getClass(), XLString.class);
    assertEquals(((XLString) result2).getValue(), "Two");
    final XLValue result3 = _processor.invoke("ListElement",  result1, XLNumber.of(10));
    assertEquals(result3.getClass(), XLError.class);
    assertEquals(result3, XLError.NA);
  }
}
