/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert;


/**
 * Interface for classes mapping from a Java type to an Excel type.
 * @param <JAVA_TYPE>  the class representing the Java type
 * @param <EXCEL_TYPE>  the class representing the Excel type 
 */
public interface JavaToExcelTypeMapping<JAVA_TYPE, EXCEL_TYPE> {

  /**
   * @return the excel Class in this key
   */
  Class<? extends EXCEL_TYPE> getExcelClass();

  /**
   * @return the java Class in this key
   */
  Class<?> getJavaClass();

  /**
   * @return the java type in this key
   */
  JAVA_TYPE getJavaType();

  /**
   * Checks whether both the java type and excel type are assignable from
   * the other type (i.e. are the types compatible).
   * @param other  the JavaToExcelTypeMapping to compare against
   * @return true, if both the java and excel types are assignable from
   */
  boolean isAssignableFrom(JavaToExcelTypeMapping<?, ?> other);

}