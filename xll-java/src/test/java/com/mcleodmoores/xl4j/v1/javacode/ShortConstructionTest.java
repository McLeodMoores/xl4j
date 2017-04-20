/**
 *
 */
package com.mcleodmoores.xl4j.v1.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;

/**
 * Tests construction of Shorts from the function processor.
 */
public class ShortConstructionTest extends TypeConstructionTests {
  /** XLNumber holding a short. */
  private static final XLNumber XL_NUMBER_SHORT = XLNumber.of((short) 10);
  /** Short. */
  private static final Short SHORT = Short.valueOf((short) 10);
  /** The class name */
  private static final String CLASSNAME = "java.lang.Short";


  /**
   * Tests creation of Shorts using its constructors.
   */
  @Test
  public void testJConstruct() {
    // no no-args constructor for Short
    XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME));
    assertTrue(xlValue instanceof XLError);
    // short constructor
    xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XL_NUMBER_SHORT);
    assertTrue(xlValue instanceof XLObject);
    Object shortObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(shortObject instanceof Short);
    Short shortVal = (Short) shortObject;
    assertEquals(shortVal.shortValue(), SHORT.shortValue());
    // String constructor
    xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XLString.of("10"));
    assertTrue(xlValue instanceof XLObject);
    shortObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(shortObject instanceof Short);
    shortVal = (Short) shortObject;
    assertEquals(shortVal.shortValue(), SHORT.shortValue());
  }

  /**
   * Tests creation of Shorts using Short.valueOf(short).
   */
  @Test
  public void testJStaticMethodXShort() {
    final XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XL_NUMBER_SHORT);
    assertTrue(xlValue instanceof XLObject);
    final Object shortObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(shortObject instanceof Short);
    final Short shortVal = (Short) shortObject;
    assertEquals(shortVal.shortValue(), SHORT.shortValue());
  }

  /**
   * Test creation of Shorts using Short.valueOf(String).
   */
  @Test
  public void testJStaticMethodXString() {
    final XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XLString.of("10"));
    assertTrue(xlValue instanceof XLObject);
    final Object shortObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(shortObject instanceof Short);
    final Short shortVal = (Short) shortObject;
    assertEquals(shortVal.shortValue(), SHORT.shortValue());
  }

  /**
   * Tests creation of Shorts using Short.valueOf(String, int).
   */
  @Test
  public void testJStaticMethodXStringNumber() {
    // 10 in base 8
    final XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XLString.of("12"), XLNumber.of(8));
    assertTrue(xlValue instanceof XLObject);
    final Object shortObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(shortObject instanceof Short);
    final Short shortVal = (Short) shortObject;
    assertEquals(shortVal.shortValue(), Short.valueOf("12", 8).shortValue());
  }
}
