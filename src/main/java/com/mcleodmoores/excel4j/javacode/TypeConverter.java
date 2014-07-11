/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Interface for converting to and from XLValue types to Java types.
 */
public interface TypeConverter {
  /**
   * Test whether this converter can handle a particular conversion.
   * @param excelToJava an excel to java type mapping
   * @return true, if this converter can handle the conversion
   */
  boolean canConvert(ExcelToJavaTypeMapping excelToJava);
  /**
   * Test whether this converter can handle a particular conversion.
   * @param javaToExcel a java to excel type mapping
   * @return true, if this converter can handle the conversion
   */
  boolean canConvert(JavaToExcelTypeMapping javaToExcel);
  
  /**
   * Convert from the supported Java type to the supported Excel type.
   * @param requiredConversion the required conversion
   * @param from  the Java object to convert to an Excel type
   * @return an Excel conversion of a Java type
   */
  XLValue toXLValue(JavaToExcelTypeMapping requiredConversion, Object from);
  
  /**
   * Convert from the supported Excel type to the supported Java type.
   * @param requiredConversion the required conversion
   * @param from  the Excel object to convert into a Java type
   * @return a Java object converted from an Excel object
   */
  Object toJavaObject(ExcelToJavaTypeMapping requiredConversion, XLValue from);
  
  /**
   * Get the priority level of this converter, higher values have higher priority.
   * @return the priority level, higher values being higher priority
   */
  int getPriority();
}
