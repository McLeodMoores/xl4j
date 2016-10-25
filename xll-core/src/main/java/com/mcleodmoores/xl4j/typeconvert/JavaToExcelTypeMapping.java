/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JReflectionUtils;

/**
 * Class to represent a Java source and Excel destination type or vice versa.
 */
public final class JavaToExcelTypeMapping {
  /** The Excel type destination type */
  private final Class<?> _excelType;
  /** The java source type */
  private final Type _javaType;
  /** The java source class */
  private final Class<?> _javaClass;

  /**
   * @param javaType
   *          the Java type
   * @param excelType
   *          the Excel type
   */
  private JavaToExcelTypeMapping(final Type javaType, final Class<?> excelType) {
    ArgumentChecker.notNull(javaType, "javaType");
    ArgumentChecker.notNull(excelType, "excelType");
    _javaType = javaType;
    _javaClass = Excel4JReflectionUtils.reduceToClass(javaType);
    _excelType = excelType;
  }

  /**
   * Static factory method.
   * 
   * @param javaType
   *          the Java type
   * @param excelType
   *          the Excel type
   * @return an instance
   */
  public static JavaToExcelTypeMapping of(final Type javaType, final Class<?> excelType) {
    return new JavaToExcelTypeMapping(javaType, excelType);
  }

  /**
   * @return the excel Class in this key
   */
  public Class<?> getExcelClass() {
    return _excelType;
  }

  /**
   * @return the java class in this key
   */
  public Class<?> getJavaClass() {
    return _javaClass;
  }

  /**
   * @return the java type in this key
   */
  public Type getJavaType() {
    return _javaType;
  }

  /**
   * Checks whether both the excel type and java type are assignable from the other type (i.e. are the types compatible).
   * 
   * @param other
   *          the ExcelToJavaTypeMapping to compare against
   * @return true, if both the excel and java types are assignable from
   */
  public boolean isAssignableFrom(final JavaToExcelTypeMapping other) {
    if (this == other) {
      return true;
    }
    if (other == null) {
      return false;
    }
    if (!_javaClass.isAssignableFrom(other.getJavaClass())) {
      return false;
    }
    if (!_excelType.isAssignableFrom(other.getExcelClass())) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _excelType.hashCode();
    result = prime * result + _javaType.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof JavaToExcelTypeMapping)) {
      return false;
    }
    final JavaToExcelTypeMapping other = (JavaToExcelTypeMapping) obj;
    if (!_excelType.equals(other.getExcelClass())) {
      return false;
    }
    if (!_javaType.equals(other.getJavaType())) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "JavaToExcelTypeMapping[excelType=" + _excelType + ", javaType=" + _javaType + "]";
  }
}
