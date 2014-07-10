/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Multimap;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Interface for converting to and from XLValue types to Java types.
 */
public interface TypeConverter {
  /**
   * Get the multimap of supported conversions from an Excel type to a Java type.
   * @return a map of supported Excel to Java conversions
   */
  Set<TypeMappingKey> getExcelToJavaMap();
  
  /**
   * Get the multimap of supported conversions from a Java type to an Excel type.
   * @return a map of supported Java to Excel conversions
   */
  Set<TypeMappingKey> getJavaToExcelMap();
  
  /**
   * Convert from the supported Java type to the supported Excel type.
   * @param from  the Java object to convert to an Excel type
   * @return an Excel conversion of a Java type
   */
  XLValue toXLValue(Object from);
  
  /**
   * Convert from the supoprted Excel type to the supported Java type.
   * @param from  the Excel object to convert into a Java type
   * @return a Java object converted from an Excel object
   */
  Object toJavaObject(XLValue from);
  
  /**
   * Get the priority level of this converter, higher values have higher priority.
   * @return the priority level, higher values being higher priority
   */
  int getPriority();
}
