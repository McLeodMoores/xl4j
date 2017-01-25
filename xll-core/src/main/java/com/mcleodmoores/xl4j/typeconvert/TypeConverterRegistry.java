/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert;

import java.lang.reflect.Type;

/**
 * Common interface for a type conversion registry.
 */
public interface TypeConverterRegistry {

  /**
   * Find a type converter to perform the required conversion, searching linearly in priority order and returning the first match.
   *
   * @param requiredMapping
   *          the required conversion
   * @return a type converter to perform the conversion
   */
  TypeConverter findConverter(ExcelToJavaTypeMapping requiredMapping);

  /**
   * Find a type converter to perform the required conversion, searching linearly in priority order. This method is used to find a converter
   * from Java back into Excel, when you don't know the target Excel type, and returning the first match.
   *
   * @param requiredJava
   *          the Java type required to convert from.
   * @return a type converter to perform the conversion
   */
  TypeConverter findConverter(Type requiredJava);

}