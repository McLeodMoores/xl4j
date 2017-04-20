/**
 *
 */
package com.mcleodmoores.xl4j.v1.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;

/**
 * Tests construction of BigDecimals from the function processor.
 */
public class BigDecimalConstructionTest extends TypeConstructionTests {
  /** XLNumber holding a double */
  private static final XLNumber XL_NUMBER_DOUBLE = XLNumber.of(10d);
  /** XLNumber holding a long */
  private static final XLNumber XL_NUMBER_LONG = XLNumber.of(10L);
  /** XLNumber holding an int */
  private static final XLNumber XL_NUMBER_INT = XLNumber.of(10);
  /** BigDecimal */
  private static final BigDecimal BIG_DECIMAL = BigDecimal.valueOf(10d);
  /** The class name */
  private static final String CLASSNAME = "java.math.BigDecimal";

  /**
   * Tests creation of BigDecimals using its constructors.
   */
  @Test
  public void testJConstruct() {
    // no no-args constructor for BigDecimal
    XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME));
    assertTrue(xlValue instanceof XLError);
    // double constructor
    xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XL_NUMBER_DOUBLE);
    assertTrue(xlValue instanceof XLObject);
    Object bigDecimalObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(bigDecimalObject instanceof BigDecimal);
    BigDecimal bigDecimal = (BigDecimal) bigDecimalObject;
    assertEquals(bigDecimal.doubleValue(), BIG_DECIMAL.doubleValue(), 0);
    // long constructor
    xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XL_NUMBER_LONG);
    assertTrue(xlValue instanceof XLObject);
    bigDecimalObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(bigDecimalObject instanceof BigDecimal);
    bigDecimal = (BigDecimal) bigDecimalObject;
    assertEquals(bigDecimal.longValue(), BIG_DECIMAL.longValue());
    // int constructor
    xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XL_NUMBER_INT);
    assertTrue(xlValue instanceof XLObject);
    bigDecimalObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(bigDecimalObject instanceof BigDecimal);
    bigDecimal = (BigDecimal) bigDecimalObject;
    assertEquals(bigDecimal.intValue(), BIG_DECIMAL.intValue());
  }

  /**
   * Tests creation of BigDecimals using its static constructors.
   */
  @Test
  public void testJMethod() {
    // no no-args method for BigDecimal
    XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"));
    assertTrue(xlValue instanceof XLError);
    // XLString is converted to Double, so BigDecimal.valueOf(Double is used)
    xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XLString.of("10"));
    assertTrue(xlValue instanceof XLObject);
    Object bigDecimalObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(bigDecimalObject instanceof BigDecimal);
    BigDecimal bigDecimal = (BigDecimal) bigDecimalObject;
    assertEquals(bigDecimal.doubleValue(), BIG_DECIMAL.doubleValue(), 0);
    // BigDecimal.valueOf(double)
    xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XL_NUMBER_DOUBLE);
    assertTrue(xlValue instanceof XLObject);
    bigDecimalObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(bigDecimalObject instanceof BigDecimal);
    bigDecimal = (BigDecimal) bigDecimalObject;
    assertEquals(bigDecimal.doubleValue(), BIG_DECIMAL.doubleValue(), 0);
    // BigDecimal.valueOf(long)
    xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XL_NUMBER_LONG);
    assertTrue(xlValue instanceof XLObject);
    bigDecimalObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(bigDecimalObject instanceof BigDecimal);
    bigDecimal = (BigDecimal) bigDecimalObject;
    assertEquals(bigDecimal.longValue(), BIG_DECIMAL.longValue());
    // BigDecimal.valueOf(long, int)
    xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XL_NUMBER_INT);
    assertTrue(xlValue instanceof XLObject);
    bigDecimalObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(bigDecimalObject instanceof BigDecimal);
    bigDecimal = (BigDecimal) bigDecimalObject;
    assertEquals(bigDecimal.intValue(), BIG_DECIMAL.intValue());
  }
}
