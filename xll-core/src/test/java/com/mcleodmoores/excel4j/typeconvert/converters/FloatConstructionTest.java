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
 * Tests construction of Floats from the function processor.
 */
public class FloatConstructionTest extends TypeConstructionTests {
  /** XLNumber holding a float */
  private static final XLNumber XL_NUMBER_FLOAT = XLNumber.of(10F);
  /** Float */
  private static final Float FLOAT = Float.valueOf(10F);
  /** The class name */
  private static final String CLASSNAME = "java.lang.Float";


  /**
   * Attempts to create a Float using a non-existent constructor.
   */
  @Test
  public void testJConstructNoArgs() {
    final XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME));
    assertTrue(xlValue instanceof XLError);
  }

  /**
   * Tests construction using new Float(float).
   */
  @Test
  public void testJConstructFloat() {
    final XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XL_NUMBER_FLOAT);
    assertTrue(xlValue instanceof XLObject);
    final Object floatObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(floatObject instanceof Float);
    final Float floatVal = (Float) floatObject;
    assertEquals(floatVal.floatValue(), FLOAT.floatValue());
  }

  /**
   * Tests construction using new Float(String).
   */
  @Test
  public void testJConstructString() {
    final XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XLString.of("10"));
    assertTrue(xlValue instanceof XLObject);
    final Object floatObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(floatObject instanceof Float);
    final Float floatVal = (Float) floatObject;
    assertEquals(floatVal.floatValue(), FLOAT.floatValue());
  }

  /**
   * Tests creation of Floats using Float.valueOf(float).
   */
  @Test
  public void testJStaticMethodXFloat() {
    final XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XL_NUMBER_FLOAT);
    assertTrue(xlValue instanceof XLObject);
    final Object floatObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(floatObject instanceof Float);
    final Float floatVal = (Float) floatObject;
    assertEquals(floatVal.floatValue(), FLOAT.floatValue());
  }

  /**
   * Test creation of Floats using Float.valueOf(String).
   */
  @Test
  public void testJStaticMethodXString() {
    final XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XLString.of("10"));
    assertTrue(xlValue instanceof XLObject);
    final Object floatObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(floatObject instanceof Float);
    final Float floatVal = (Float) floatObject;
    assertEquals(floatVal.floatValue(), FLOAT.floatValue());
  }

}
