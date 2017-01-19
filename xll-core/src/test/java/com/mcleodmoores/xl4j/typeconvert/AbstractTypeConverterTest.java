/**
 *
 */
package com.mcleodmoores.xl4j.typeconvert;

import java.lang.reflect.Type;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Tests a dummy {@link AbstractTypeConverter} implementation.
 */
@Test
public class AbstractTypeConverterTest {

  /**
   * Tests the exception thrown when the java type is null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullJavaType() {
    /**
     * Test converter.
     */
    class NullTypeConverterTest extends AbstractTypeConverter {
      /**
       * No-arg constructor.
       */
      NullTypeConverterTest() {
        super((Class<?>) null, XLNumber.class);
      }

      @Override
      public Object toXLValue(final Type expectedType, final Object from) {
        return null;
      }

      @Override
      public Object toJavaObject(final Type expectedType, final Object from) {
        return null;
      }
    }
    new NullTypeConverterTest();
  }

  /**
   * Tests the exception thrown when the Excel type is null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullXLType() {
    /**
     * Test converter.
     */
    class NullTypeConverterTest extends AbstractTypeConverter {
      /**
       * No-args constructor.
       */
      NullTypeConverterTest() {
        super(Double.class, (Class<? extends XLValue>) null);
      }

      @Override
      public Object toXLValue(final Type expectedType, final Object from) {
        return null;
      }

      @Override
      public Object toJavaObject(final Type expectedType, final Object from) {
        return null;
      }
    }
    new NullTypeConverterTest();
  }
}
