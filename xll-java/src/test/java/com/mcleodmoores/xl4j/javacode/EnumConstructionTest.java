/**
 *
 */
package com.mcleodmoores.xl4j.javacode;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.javacode.testutils.TestEnum;
import com.mcleodmoores.xl4j.values.XLError;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Tests construction of enums from the function processor.
 */
public class EnumConstructionTest extends TypeConstructionTests {
  /** The class name */
  private static final String CLASSNAME = "com.mcleodmoores.xl4j.javacode.testutils.TestEnum";

  /**
   * Attempts to create an enum using new TestEnum().
   */
  @Test
  public void testJConstructNoArgs() {
    final XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME));
    assertTrue(xlValue instanceof XLError);
  }

  /**
   * Attempts to create an enum using new TestEnum(String, int).
   */
  @Test
  public void testJConstructNoVisibleConstructor() {
    final XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XLString.of(TestEnum.TEST.name()), XLNumber.of(TestEnum.TEST.ordinal()));
    assertTrue(xlValue instanceof XLError);
  }

  /**
   * Tests creation of an enum constant.
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testJMethod() {
    final XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XLString.of(TestEnum.TEST.name()));
    assertTrue(xlValue instanceof XLObject);
    final Object enumObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(enumObject instanceof Enum);
    final Enum<TestEnum> enumVal = (Enum<TestEnum>) enumObject;
    assertTrue(enumVal == TestEnum.TEST);
  }
}
