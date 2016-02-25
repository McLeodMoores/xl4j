/**
 *
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.values.XLError;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Tests constructions of enums from the function processor.
 */
public class EnumConstructionTest extends TypeConstructionTests {
  /** Enum. */
  public static enum TestEnum {
    /** Test value. */
    TEST
  };
  private static final String CLASSNAME = "java.lang.Enum";

  /**
   * Attempts to create an enum using new Enum().
   */
  @Test
  public void testJConstructNoArgs() {
    final XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME));
    assertTrue(xlValue instanceof XLError);
  }

  /**
   * Attempts to create an enum using new Enum(String, int).
   */
  @Test
  public void testJConstructNoVisibleConstructor() {
    final XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XLString.of(TestEnum.TEST.name()), XLNumber.of(TestEnum.TEST.ordinal()));
    assertTrue(xlValue instanceof XLError);
  }

  /**
   * Tests creation of an enum constant.
   */
  @Test
  public void testJMethod() {
    final XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"),
        XLString.of("com.mcleodmoores.excel4j.typeconvert.converters.EnumConstructionTest.TestEnum"), XLString.of(TestEnum.TEST.name()));
    assertTrue(xlValue instanceof XLObject);
    final Object enumObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(enumObject instanceof Enum);
    final Enum<TestEnum> enumVal = (Enum<TestEnum>) enumObject;
    assertTrue(enumVal == TestEnum.TEST);
  }
}
