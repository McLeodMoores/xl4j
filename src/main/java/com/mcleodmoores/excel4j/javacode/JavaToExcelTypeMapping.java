/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Class to represent a Java source and Excel destination type or vice versa.
 */
public final class JavaToExcelTypeMapping {
  private Class<? extends XLValue> _excelType;
  private Type _javaType;

  /**
   * @param javaType the Java type
   * @param excelType the Excel type
   */
  private JavaToExcelTypeMapping(final Type javaType, final Class<? extends XLValue> excelType) {
    _javaType = javaType;
    _excelType = excelType;
  }
  
  /**
   * Static factory method.
   * @param javaType the Java type
   * @param excelType the Excel type
   * @return an instance
   */
  public static JavaToExcelTypeMapping of(final Type javaType, final Class<? extends XLValue> excelType) {
    return new JavaToExcelTypeMapping(javaType, excelType);
  }

  /**
   * @return the excel type in this key
   */
  public Class<? extends XLValue> getExcelType() {
    return _excelType;
  }
  
  /**
   * @return the java type in this key 
   */
  public Type getJavaType() {
    return _javaType;
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
    JavaToExcelTypeMapping other = (JavaToExcelTypeMapping) obj;
    if (!_excelType.equals(other._excelType)) {
      return false;
    }
    if (!_javaType.equals(other._javaType)) {
      return false;
    }
    return true;
  }
}
