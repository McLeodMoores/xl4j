/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert;


/**
 * Interface for classes storing mappings between Excel and Java types.
 * Generic so we can have scalar and array versions.
 * @param <EXCEL_TYPE>  the Excel base class to handle
 * @param <JAVA_TYPE>  the Java class representing the java type
 */
public interface ExcelToJavaTypeMapping<EXCEL_TYPE, JAVA_TYPE> {
  /**
   * @return the excel Class in this key
   */
  Class<? extends EXCEL_TYPE> getExcelClass();
  
  /**
   * @return the java type in this key
   */
  JAVA_TYPE getJavaType();

  /**
   * @return the java type in this key
   */
  Class<?> getJavaClass();

  /**
   * Checks whether both the excel type and java type are assignable from
   * the other type (i.e. are the types compatible).
   * @param other  the ExcelToJavaTypeMapping to compare against
   * @return true, if both the excel and java types are assignable from
   */
  boolean isAssignableFrom(final ExcelToJavaTypeMapping<?, ?> other);
}
