/**
 *
 */
package com.mcleodmoores.xl4j.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.ExcelFactory;
import com.mcleodmoores.xl4j.javacode.JConstruct;
import com.mcleodmoores.xl4j.testutil.TestObject;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLError;
import com.mcleodmoores.xl4j.values.XLInteger;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 *
 */
public class JConstructTest {
  private static final XLString CLASS = XLString.of("com.mcleodmoores.xl4j.testutil.TestObject");

  @Test
  public void testNoArgsConstructor() {
    final XLValue constructed = JConstruct.jconstruct(CLASS, new XLValue[0]);
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = new TestObject();
    assertEquals(constructedObject, expectedObject);
  }

  /**
   * No converters for XLInteger.
   */
  @Test
  public void testExpectedFailureIntConstructor() {
    final XLValue constructed = JConstruct.jconstruct(CLASS, XLInteger.of(1));
    assertTrue(constructed instanceof XLError);
  }

  @Test
  public void testIntConstructor() {
    final XLValue constructed = JConstruct.jconstruct(CLASS, XLNumber.of(1));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = new TestObject(1);
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testStringConstructor() {
    final XLValue constructed = JConstruct.jconstruct(CLASS, XLString.of("2"));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = new TestObject("2");
    // could have gone through (Double)
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testObjectConstructor() {
    final XLValue number = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(3));
    final XLValue constructed = JConstruct.jconstruct(CLASS, number);
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = new TestObject(Short.valueOf("3"));
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testIntStringConstructor() {
    final XLValue constructed = JConstruct.jconstruct(CLASS, XLNumber.of(4), XLString.of("40"));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = new TestObject(4, "40");
    // could go through (int, Object)
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testIntDoubleConstructor() {
    final XLValue constructed = JConstruct.jconstruct(CLASS, XLNumber.of(5), XLNumber.of(50));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = new TestObject(5, 50);
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testDoubleConstructor() {
    final XLValue constructed = JConstruct.jconstruct(CLASS, XLNumber.of(6.));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = new TestObject(6.);
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testIntObjectConstructor() {
    final XLValue number = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(70));
    final XLValue constructed = JConstruct.jconstruct(CLASS, XLNumber.of(7), number);
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = new TestObject(7, Short.valueOf("70"));
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testStringsConstructor() {
    final XLValue constructed = JConstruct.jconstruct(CLASS, XLString.of("80"), XLString.of("81"), XLString.of("82"));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = new TestObject("80", "81", "82");
    // could have gone through (Object...) or (Double...)
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testIntStringsConstructor() {
    final XLValue constructed = JConstruct.jconstruct(CLASS, XLNumber.of(9), XLString.of("90"), XLString.of("91"), XLString.of("92"));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = new TestObject(9, new String[] {"90", "91", "92"});
    // could have gone through (Double...) or (int, Object...)
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testDoublesConstructor() {
    final XLValue constructed = JConstruct.jconstruct(CLASS, XLNumber.of(100.), XLNumber.of(101.), XLNumber.of(102.));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = new TestObject(100., 101., 102.);
    // could have gone through (Object...)
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testIntObjectsConstructor() {
    final XLValue number1 = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(110));
    final XLValue number2 = JConstruct.jconstruct(XLString.of("java.lang.Long"), XLNumber.of(111));
    final XLValue number3 = JConstruct.jconstruct(XLString.of("java.lang.Float"), XLNumber.of(112));
    final XLValue constructed = JConstruct.jconstruct(CLASS, XLNumber.of(11), number1, number2, number3);
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = new TestObject(11, new Object[] {(short) 110, 111L, 112F});
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testIntDoublesConstructor() {
    final XLValue constructed = JConstruct.jconstruct(CLASS, XLNumber.of(12), XLNumber.of(120.), XLNumber.of(121.), XLNumber.of(122.));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = new TestObject(12, new Double[] {120., 121., 122.});
    // could have gone through (int, Object...), (Double...) or (int, Double...)
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testObjectsConstructor() {
    final XLValue number1 = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(130));
    final XLValue number2 = JConstruct.jconstruct(XLString.of("java.lang.Long"), XLNumber.of(131));
    final XLValue number3 = JConstruct.jconstruct(XLString.of("java.lang.Float"), XLNumber.of(132));
    final XLValue constructed = JConstruct.jconstruct(CLASS, number1, number2, number3);
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = new TestObject(new Object[] {(short) 130, 131L, 132F});
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testStringArrayStringsConstructor() {
    final XLValue constructed =
        JConstruct.jconstruct(CLASS, XLArray.of(new XLValue[][] {new XLValue[] {XLString.of("14")}}), XLString.of("140"), XLString.of("141"));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = new TestObject(new String[] {"14"}, new String[] {"140", "141"});
    assertEquals(constructedObject, expectedObject);
  }
}
