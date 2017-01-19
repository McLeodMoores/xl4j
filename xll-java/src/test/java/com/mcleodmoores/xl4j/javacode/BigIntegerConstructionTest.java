/**
 *
 */
package com.mcleodmoores.xl4j.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigInteger;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.values.XLError;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Tests construction of BigIntegers from the function processor.
 */
public class BigIntegerConstructionTest extends TypeConstructionTests {
  /** XLNumber holding a double. */
  private static final XLNumber XL_NUMBER_DOUBLE = XLNumber.of(10d);
  /** XLNumber holding a long. */
  private static final XLNumber XL_NUMBER_LONG = XLNumber.of(10L);
  /** XLNumber holding an int. */
  private static final XLNumber XL_NUMBER_INT = XLNumber.of(10);
  /** BigInteger. */
  private static final BigInteger BIG_INTEGER = BigInteger.valueOf(10);
  /** The class name */
  private static final String CLASSNAME = "java.math.BigInteger";

  /**
   * Tests creation of BigIntegers using its constructors.
   */
  @Test
  public void testJConstruct() {
    // no no-args constructor for BigInteger
    XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME));
    assertTrue(xlValue instanceof XLError);
    // new BigInteger(String)
    xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XLString.of("10"));
    assertTrue(xlValue instanceof XLObject);
    Object bigIntegerObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(bigIntegerObject instanceof BigInteger);
    assertEquals(((BigInteger) bigIntegerObject).intValue(), 10);
    // new BigInteger(String, int)
    xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XLString.of("12"), XLNumber.of(8));
    assertTrue(xlValue instanceof XLObject);
    bigIntegerObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(bigIntegerObject instanceof BigInteger);
    assertEquals(((BigInteger) bigIntegerObject).intValue(), 10);
  }

  /**
   * Tests creation of BigIntegers using its static constructors.
   */
  @Test
  public void testJMethod() {
    // no no-args method for BigInteger
    XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"));
    assertTrue(xlValue instanceof XLError);
    // XLString is converted to Double, so BigInteger.valueOf(Double is used)
    xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XLString.of("10"));
    assertTrue(xlValue instanceof XLObject);
    Object bigIntegerObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(bigIntegerObject instanceof BigInteger);
    BigInteger bigInteger = (BigInteger) bigIntegerObject;
    assertEquals(bigInteger.doubleValue(), BIG_INTEGER.doubleValue(), 0);
    // BigInteger.valueOf(double)
    xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XL_NUMBER_DOUBLE);
    assertTrue(xlValue instanceof XLObject);
    bigIntegerObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(bigIntegerObject instanceof BigInteger);
    bigInteger = (BigInteger) bigIntegerObject;
    assertEquals(bigInteger.doubleValue(), BIG_INTEGER.doubleValue(), 0);
    // BigInteger.valueOf(long)
    xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XL_NUMBER_LONG);
    assertTrue(xlValue instanceof XLObject);
    bigIntegerObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(bigIntegerObject instanceof BigInteger);
    bigInteger = (BigInteger) bigIntegerObject;
    assertEquals(bigInteger.longValue(), BIG_INTEGER.longValue());
    // BigInteger.valueOf(long, int)
    xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XL_NUMBER_INT);
    assertTrue(xlValue instanceof XLObject);
    bigIntegerObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(bigIntegerObject instanceof BigInteger);
    bigInteger = (BigInteger) bigIntegerObject;
    assertEquals(bigInteger.intValue(), BIG_INTEGER.intValue());
  }
}
