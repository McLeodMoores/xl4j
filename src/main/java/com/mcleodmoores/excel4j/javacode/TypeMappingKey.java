/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Class to represent a Java source and Excel destination type or vice versa.
 */
public class TypeMappingKey {
  private Class<? extends XLValue> _excelType;
  private Type _javaType;

  /**
   * @param javaType the Java type
   * @param excelType the Excel type
   */
  public TypeMappingKey(final Type javaType, final Class<? extends XLValue> excelType) {
    _javaType = javaType;
    _excelType = excelType;
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
   * Static builder class.
   */
  public static final class Builder {
    private Type _javaType;
    private Class<? extends XLValue> _excelType;
    
    private Builder(final Type javaType) {
      _javaType = javaType;
    }
    
    private Builder(final Class<? extends XLValue> excelType) {
      _excelType = excelType;
    }
    
    /**
     * static factory method to use when creating a key for a Java to Excel conversion. 
     * @param javaType the Java type to convert from
     * @return a Builder, invoke toExcelType() then build()
     */
    public static Builder ofJavaType(final Type javaType) {
      return new Builder(javaType);
    }
    
    /**
     * static factory method to use when creating a key for an Excel to Java conversion.
     * @param excelType the Excel type to convert from
     * @return a Builder, invoke toJavaType() then build()
     */
    public static Builder ofExcelType(final Class<? extends XLValue> excelType) {
      return new Builder(excelType);
    }
    
    /**
     * Specify the Java type to convert to.  Follow this with a call to build().
     * @param javaType the Java type to convert to
     * @return a Builder, invoke build() to get key
     */
    public Builder toJavaType(final Type javaType) {
      _javaType = javaType;
      return this;
    }
    
    /**
     * Specify the Excel type to convert to.  Follow this with a call to build().
     * @param excelType the Excel type to convert to
     * @return a Builder, invoke build to get key
     */
    public Builder toExcelType(final Class<? extends XLValue> excelType) {
      _excelType = excelType;
      return this;
    }
    
    /**
     * Build the key instance.
     * @return the TypeMappingKey instance
     */
    public TypeMappingKey build() {
      return new TypeMappingKey(_javaType, _excelType);
    }
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
    if (!(obj instanceof TypeMappingKey)) {
      return false;
    }
    TypeMappingKey other = (TypeMappingKey) obj;
    if (!_excelType.equals(other._excelType)) {
      return false;
    }
    if (!_javaType.equals(other._javaType)) {
      return false;
    }
    return true;
  }
}
