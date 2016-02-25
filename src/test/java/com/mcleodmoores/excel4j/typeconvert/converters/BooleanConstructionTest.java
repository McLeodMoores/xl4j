/**
 *
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.values.XLBoolean;
import com.mcleodmoores.excel4j.values.XLError;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Tests constructions of Shorts from the function processor.
 */
public class BooleanConstructionTest extends TypeConstructionTests {
  /** The class name */
  private static final String CLASSNAME = "java.lang.Boolean";


  /**
   * Tests creation of Booleans using its constructors.
   */
  @Test
  public void testJConstruct() {
    // no no-args constructor for Boolean
    XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME));
    assertTrue(xlValue instanceof XLError);
    // Boolean constructor
    xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XLBoolean.FALSE);
    assertTrue(xlValue instanceof XLObject);
    Object booleanObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(booleanObject instanceof Boolean);
    Boolean booleanVal = (Boolean) booleanObject;
    assertEquals(booleanVal, Boolean.FALSE);
    // String constructor
    xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XLString.of("True"));
    assertTrue(xlValue instanceof XLObject);
    booleanObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(booleanObject instanceof Boolean);
    booleanVal = (Boolean) booleanObject;
    assertEquals(booleanVal, Boolean.TRUE);
  }

  /**
   * Tests creation of Booleans using its static constructors.
   */
  @Test
  public void testJMethod() {
    // Boolean.valueOf(boolean)
    XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XLBoolean.FALSE);
    assertTrue(xlValue instanceof XLObject);
    Object booleanObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(booleanObject instanceof Boolean);
    Boolean booleanVal = (Boolean) booleanObject;
    assertEquals(booleanVal, Boolean.FALSE);
    // Boolean.valueOf(String)
    xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XLString.of("true"));
    assertTrue(xlValue instanceof XLObject);
    booleanObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(booleanObject instanceof Boolean);
    booleanVal = (Boolean) booleanObject;
    assertEquals(booleanVal, Boolean.TRUE);
  }
}
