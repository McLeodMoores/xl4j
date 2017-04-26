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
 * Tests construction of Doubles from the function processor.
 */
public class DoubleConstructionTest extends TypeConstructionTests {
  /** XLNumber holding a double. */
  private static final XLNumber XL_NUMBER_DOUBLE = XLNumber.of(10.);
  /** Double. */
  private static final Double DOUBLE = 10.;
  /** The class name */
  private static final String CLASSNAME = "java.lang.Double";

  /**
   * Tests creation of Double using its constructors.
   */
  @Test
  public void testJConstruct() {
    // no no-args constructor for Double
    XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME));
    assertTrue(xlValue instanceof XLError);
    // double constructor
    xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XL_NUMBER_DOUBLE);
    assertTrue(xlValue instanceof XLObject);
    Object doubleObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(doubleObject instanceof Double);
    Double doubleVal = (Double) doubleObject;
    assertEquals(doubleVal.doubleValue(), DOUBLE.doubleValue(), 1e-15);
    // String constructor
    xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XLString.of("10"));
    assertTrue(xlValue instanceof XLObject);
    doubleObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(doubleObject instanceof Double);
    doubleVal = (Double) doubleObject;
    assertEquals(doubleVal.doubleValue(), DOUBLE.doubleValue(), 1e-15);
  }

  /**
   * Tests creation of Double using its static constructors.
   */
  @Test
  public void testJMethod() {
    // Double.valueOf(double)
    XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XL_NUMBER_DOUBLE);
    assertTrue(xlValue instanceof XLObject);
    Object doubleObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(doubleObject instanceof Double);
    Double doubleVal = (Double) doubleObject;
    assertEquals(doubleVal.doubleValue(), DOUBLE.doubleValue(), 1e-15);
    // Double.valueOf(String)
    xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XLString.of("10"));
    assertTrue(xlValue instanceof XLObject);
    doubleObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(doubleObject instanceof Double);
    doubleVal = (Double) doubleObject;
    assertEquals(doubleVal.doubleValue(), DOUBLE.doubleValue(), 1e-15);
  }
}
