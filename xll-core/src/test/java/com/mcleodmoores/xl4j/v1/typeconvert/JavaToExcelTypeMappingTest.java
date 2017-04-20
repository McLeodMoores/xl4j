/**
 *
 */
package com.mcleodmoores.xl4j.v1.typeconvert;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.xl4j.v1.api.values.XLBoolean;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for {@link JavaToExcelTypeMapping}.
 */
@Test
public class JavaToExcelTypeMappingTest {
  private static final int THREE = 3;
  /** The Java class */
  private static final Class<Number> JAVA_CLASS = Number.class;
  /** The XL class */
  private static final Class<XLNumber> EXCEL_CLASS = XLNumber.class;
  /** The mapping */
  private static final JavaToExcelTypeMapping MAPPING = JavaToExcelTypeMapping.of(JAVA_CLASS, EXCEL_CLASS);

  /**
   * Tests the exception thrown if the Java class is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullJavaClass() {
    JavaToExcelTypeMapping.of(null, EXCEL_CLASS);
  }

  /**
   * Tests the exception thrown if the Excel class is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
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
    assertNotEquals(Double.valueOf(THREE), MAPPING);
    final String expectedString = "JavaToExcelTypeMapping[excelType=class com.mcleodmoores.xl4j.v1.api.values.XLNumber, javaType=class java.lang.Number]";
    assertEquals(MAPPING.toString(), expectedString);
    JavaToExcelTypeMapping other = JavaToExcelTypeMapping.of(JAVA_CLASS, EXCEL_CLASS);
    assertEquals(MAPPING, other);
    assertEquals(MAPPING.hashCode(), other.hashCode());
    other = JavaToExcelTypeMapping.of(EXCEL_CLASS, EXCEL_CLASS);
    assertNotEquals(MAPPING, other);
    other = JavaToExcelTypeMapping.of(JAVA_CLASS, XLValue.class);
    assertNotEquals(MAPPING, other);
  }

  /**
   * Tests the assignable from logic.
   */
  @Test
  public void testAssignableFrom() {
    assertTrue(MAPPING.isAssignableFrom(MAPPING));
    assertFalse(MAPPING.isAssignableFrom(null));
    JavaToExcelTypeMapping other = JavaToExcelTypeMapping.of(JAVA_CLASS, EXCEL_CLASS);
    assertTrue(MAPPING.isAssignableFrom(other));
    other = JavaToExcelTypeMapping.of(List.class, EXCEL_CLASS);
    assertFalse(MAPPING.isAssignableFrom(other));
    assertFalse(other.isAssignableFrom(MAPPING));
    other = JavaToExcelTypeMapping.of(JAVA_CLASS, XLBoolean.class);
    assertFalse(MAPPING.isAssignableFrom(other));
    assertFalse(other.isAssignableFrom(MAPPING));
    other = JavaToExcelTypeMapping.of(Double.class, EXCEL_CLASS);
    assertTrue(MAPPING.isAssignableFrom(other));
    assertFalse(other.isAssignableFrom(MAPPING));
    other = JavaToExcelTypeMapping.of(Object.class, EXCEL_CLASS);
    assertFalse(MAPPING.isAssignableFrom(other));
    assertTrue(other.isAssignableFrom(MAPPING));
    other = JavaToExcelTypeMapping.of(JAVA_CLASS, XLValue.class);
    assertFalse(MAPPING.isAssignableFrom(other));
    assertTrue(other.isAssignableFrom(MAPPING));
    other = JavaToExcelTypeMapping.of(Object.class, XLValue.class);
    assertFalse(MAPPING.isAssignableFrom(other));
    assertTrue(other.isAssignableFrom(MAPPING));
  }

}
