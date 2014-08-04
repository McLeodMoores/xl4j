/**
 *
 */
package com.mcleodmoores.excel4j.typeconvert;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLBoolean;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Unit tests for {@link ScalarJavaToExcelTypeMapping}.
 */
@Test
public class JavaToExcelTypeMappingTest {
  private static final int THREE = 3;
  /** The Java class */
  private static final Class<Number> JAVA_CLASS = Number.class;
  /** The XL class */
  private static final Class<XLNumber> EXCEL_CLASS = XLNumber.class;
  /** The mapping */
  private static final ScalarJavaToExcelTypeMapping MAPPING = ScalarJavaToExcelTypeMapping.of(JAVA_CLASS, EXCEL_CLASS);

  /**
   * Tests the exception thrown if the Java class is null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullJavaClass() {
    ScalarJavaToExcelTypeMapping.of(null, EXCEL_CLASS);
  }

  /**
   * Tests the exception thrown if the Excel class is null.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullExcelClass() {
    ScalarJavaToExcelTypeMapping.of(JAVA_CLASS, null);
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
    assertNotEquals(Double.valueOf(THREE), MAPPING);
    final String expectedString = "JavaToExcelTypeMapping[excelType=class com.mcleodmoores.excel4j.values.XLNumber, javaType=class java.lang.Number]";
    assertEquals(MAPPING.toString(), expectedString);
    JavaToExcelTypeMappingI other = ScalarJavaToExcelTypeMapping.of(JAVA_CLASS, EXCEL_CLASS);
    assertEquals(MAPPING, other);
    assertEquals(MAPPING.hashCode(), other.hashCode());
    other = ScalarJavaToExcelTypeMapping.of(EXCEL_CLASS, EXCEL_CLASS);
    assertNotEquals(MAPPING, other);
    other = ScalarJavaToExcelTypeMapping.of(JAVA_CLASS, XLValue.class);
    assertNotEquals(MAPPING, other);
  }

  /**
   * Tests the assignable from logic.
   */
  @Test
  public void testAssignableFrom() {
    assertTrue(MAPPING.isAssignableFrom(MAPPING));
    assertFalse(MAPPING.isAssignableFrom(null));
    ScalarJavaToExcelTypeMapping other = ScalarJavaToExcelTypeMapping.of(JAVA_CLASS, EXCEL_CLASS);
    assertTrue(MAPPING.isAssignableFrom(other));
    other = ScalarJavaToExcelTypeMapping.of(List.class, EXCEL_CLASS);
    assertFalse(MAPPING.isAssignableFrom(other));
    assertFalse(other.isAssignableFrom(MAPPING));
    other = ScalarJavaToExcelTypeMapping.of(JAVA_CLASS, XLBoolean.class);
    assertFalse(MAPPING.isAssignableFrom(other));
    assertFalse(other.isAssignableFrom(MAPPING));
    other = ScalarJavaToExcelTypeMapping.of(Double.class, EXCEL_CLASS);
    assertTrue(MAPPING.isAssignableFrom(other));
    assertFalse(other.isAssignableFrom(MAPPING));
    other = ScalarJavaToExcelTypeMapping.of(Object.class, EXCEL_CLASS);
    assertFalse(MAPPING.isAssignableFrom(other));
    assertTrue(other.isAssignableFrom(MAPPING));
    other = ScalarJavaToExcelTypeMapping.of(JAVA_CLASS, XLValue.class);
    assertFalse(MAPPING.isAssignableFrom(other));
    assertTrue(other.isAssignableFrom(MAPPING));
    other = ScalarJavaToExcelTypeMapping.of(Object.class, XLValue.class);
    assertFalse(MAPPING.isAssignableFrom(other));
    assertTrue(other.isAssignableFrom(MAPPING));
  }

}
