/**
 *
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.values.XLError;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Tests constructions of Integers from the function processor.
 */
public class IntegerConstructionTest extends TypeConstructionTests {
  /** XLNumber holding an int. */
  private static final XLNumber XL_NUMBER_INT = XLNumber.of(10);
  /** Integer. */
  private static final Integer INTEGER = 10;
  /** The class name */
  private static final String CLASSNAME = "java.lang.Integer";

  /**
   * Tests creation of Integer using its constructors.
   */
  @Test
  public void testJConstruct() {
    // no no-args constructor for Integer
    XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME));
    assertTrue(xlValue instanceof XLError);
    // integer constructor
    xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XL_NUMBER_INT);
    assertTrue(xlValue instanceof XLObject);
    Object integerObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(integerObject instanceof Integer);
    Integer integerVal = (Integer) integerObject;
    assertEquals(integerVal.intValue(), INTEGER.intValue());
    // String constructor
    xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XLString.of("10"));
    assertTrue(xlValue instanceof XLObject);
    integerObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(integerObject instanceof Integer);
    integerVal = (Integer) integerObject;
    assertEquals(integerVal.intValue(), INTEGER.intValue());
  }

  /**
   * Tests creation of Integer using its static constructors.
   */
  @Test
  public void testJMethod() {
    // Integer.valueOf(int)
    XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XL_NUMBER_INT);
    assertTrue(xlValue instanceof XLObject);
    Object integerObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(integerObject instanceof Integer);
    Integer integerVal = (Integer) integerObject;
    assertEquals(integerVal.intValue(), INTEGER.intValue());
    // Integer.valueOf(string)
    xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XLString.of("10"));
    assertTrue(xlValue instanceof XLObject);
    integerObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(integerObject instanceof Integer);
    integerVal = (Integer) integerObject;
    assertEquals(integerVal.intValue(), INTEGER.intValue());
    // 10 in base 8
    xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XLString.of("12"), XLNumber.of(8));
    assertTrue(xlValue instanceof XLObject);
    integerObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(integerObject instanceof Integer);
    integerVal = (Integer) integerObject;
    assertEquals(integerVal.intValue(), Integer.valueOf("12", 8).intValue());
  }
}
