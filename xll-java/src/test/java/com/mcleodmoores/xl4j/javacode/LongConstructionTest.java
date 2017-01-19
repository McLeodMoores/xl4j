/**
 *
 */
package com.mcleodmoores.xl4j.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.values.XLError;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Tests construction of Longs from the function processor.
 */
public class LongConstructionTest extends TypeConstructionTests {
  /** XLNumber holding a long */
  private static final XLNumber XL_NUMBER_LONG = XLNumber.of(10L);
  /** Long */
  private static final Long LONG = Long.valueOf(10L);
  /** The class name */
  private static final String CLASSNAME = "java.lang.Long";


  /**
   * Attempts to create a Long using a non-existent constructor.
   */
  @Test
  public void testJConstructNoArgs() {
    final XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME));
    assertTrue(xlValue instanceof XLError);
  }

  /**
   * Tests construction using new Long(long).
   */
  @Test
  public void testJConstructLong() {
    final XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XL_NUMBER_LONG);
    assertTrue(xlValue instanceof XLObject);
    final Object longObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(longObject instanceof Long);
    final Long longVal = (Long) longObject;
    assertEquals(longVal.longValue(), LONG.longValue());
  }

  /**
   * Tests construction using new Long(String).
   */
  @Test
  public void testJConstructString() {
    final XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XLString.of("10"));
    assertTrue(xlValue instanceof XLObject);
    final Object longObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(longObject instanceof Long);
    final Long longVal = (Long) longObject;
    assertEquals(longVal.longValue(), LONG.longValue());
  }

  /**
   * Tests creation of Longs using Long.valueOf(long).
   */
  @Test
  public void testJStaticMethodXLong() {
    final XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XL_NUMBER_LONG);
    assertTrue(xlValue instanceof XLObject);
    final Object longObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(longObject instanceof Long);
    final Long longVal = (Long) longObject;
    assertEquals(longVal.longValue(), LONG.longValue());
  }

  /**
   * Test creation of Longs using Long.valueOf(String).
   */
  @Test
  public void testJStaticMethodXString() {
    final XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XLString.of("10"));
    assertTrue(xlValue instanceof XLObject);
    final Object longObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(longObject instanceof Long);
    final Long longVal = (Long) longObject;
    assertEquals(longVal.longValue(), LONG.longValue());
  }

  /**
   * Tests creation of Longs using Long.valueOf(String, int).
   */
  @Test
  public void testJStaticMethodXStringNumber() {
    // 10 in base 8
    final XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XLString.of("12"), XLNumber.of(8));
    assertTrue(xlValue instanceof XLObject);
    final Object longObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(longObject instanceof Long);
    final Long longVal = (Long) longObject;
    assertEquals(longVal.longValue(), Long.valueOf("12", 8).longValue());
  }
}
