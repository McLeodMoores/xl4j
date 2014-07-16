/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert;

import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Class to represent a Java source and Excel destination type or vice versa.
 */
public final class ExcelToJavaTypeMapping {
  private Class<? extends XLValue> _excelType;
  private Type _javaType;

  /**
   * @param excelType the Excel type
   * @param javaType the Java type
   */
  private ExcelToJavaTypeMapping(final Class<? extends XLValue> excelType, final Type javaType) {
    _javaType = javaType;
    _excelType = excelType;
  }
  
  /**
   * Static factory method.
   * @param excelType the Excel type
   * @param javaType the Java type
   * @return an instance
   */
  public static ExcelToJavaTypeMapping of(final Class<? extends XLValue> excelType, final Type javaType) {
    return new ExcelToJavaTypeMapping(excelType, javaType);
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
  
  /**
   * Checks whether both the excel type and java type are assignable from 
   * the other type (i.e. are the types compatible).
   * @param other  the ExcelToJavaTypeMapping to compare against
   * @return true, if both the excel and java types are assignable from
   */
  public boolean isAssignableFrom(final ExcelToJavaTypeMapping other) {
    if (this == other) {
      return true;
    }
    if (other == null) {
      return false;
    }
    if (!_excelType.isAssignableFrom(other._excelType)) {
      return false;
    }
    if (!_javaType.getClass().isAssignableFrom(other._javaType.getClass())) {
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
    if (!(obj instanceof ExcelToJavaTypeMapping)) {
      return false;
    }
    ExcelToJavaTypeMapping other = (ExcelToJavaTypeMapping) obj;
    if (!_excelType.equals(other._excelType)) {
      return false;
    }
    if (!_javaType.equals(other._javaType)) {
      return false;
    }
    return true;
  }
}
