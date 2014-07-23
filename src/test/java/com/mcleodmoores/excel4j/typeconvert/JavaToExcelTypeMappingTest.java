/**
 *
 */
package com.mcleodmoores.excel4j.typeconvert;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Unit tests for {@link JavaToExcelTypeMapping}.
 */
@Test
public class JavaToExcelTypeMappingTest {
  /** The Java class */
  private static final Class<Double> JAVA_CLASS = Double.class;
  /** The XL class */
  private static final Class<XLNumber> EXCEL_CLASS = XLNumber.class;
  /** The mapping */
  private static final JavaToExcelTypeMapping MAPPING = JavaToExcelTypeMapping.of(JAVA_CLASS, EXCEL_CLASS);

  /**
   * Tests the exception thrown if the Java class is null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullJavaClass() {
    JavaToExcelTypeMapping.of(null, EXCEL_CLASS);
  }

  /**
   * Tests the exception thrown if the Excel class is null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullExcelClass() {
    JavaToExcelTypeMapping.of(JAVA_CLASS, null);
  }

  /**
   * Tests the object (getters, hashCode, equals and toString).
   */
  @Test
  public void testObject() {
    assertEquals(MAPPING.getExcelClass(), EXCEL_CLASS);
    assertEquals(MAPPING.getJavaClass(), JAVA_CLASS);
    assertEquals(MAPPING.getJavaType(), JAVA_CLASS);
    assertEquals(MAPPING, MAPPING);
    assertNotEquals(null, MAPPING);
    assertNotEquals(Double.valueOf(3), MAPPING);
    final String expectedString = "JavaToExcelTypeMapping[excelType=class com.mcleodmoores.excel4j.values.XLNumber, javaType=class java.lang.Double]";
    assertEquals(MAPPING.toString(), expectedString);
    JavaToExcelTypeMapping other = JavaToExcelTypeMapping.of(JAVA_CLASS, EXCEL_CLASS);
    assertEquals(MAPPING, other);
    assertEquals(MAPPING.hashCode(), other.hashCode());
    other = JavaToExcelTypeMapping.of(EXCEL_CLASS, EXCEL_CLASS);
    assertNotEquals(MAPPING, other);
    other = JavaToExcelTypeMapping.of(JAVA_CLASS, XLValue.class);
    assertNotEquals(MAPPING, other);
  }

  //TODO test assignable from

}
