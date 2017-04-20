/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.core;

import java.lang.reflect.Type;

import com.mcleodmoores.xl4j.v1.api.values.XLValue;

/**
 * Common interface for classes that get field values and convert the results.
 */
public interface FieldGetter {

  /**
   * Get the field, performing the necessary type conversions.
   * @param object  the object from which to obtain the field, can be null if the field is static
   * @return the value to return to Excel
   */
  XLValue get(Object object);

  /**
   * @return the Excel class returned by this field
   */
  Class<?> getExcelReturnType();

  /**
   * Gets the field type.
   * @return the field type
   */
  Type getFieldType();

  /**
   * Returns true if the field is static.
   * @return true if the field is static
   */
  boolean isStatic();

  /**
   * Gets the field name.
   * @return the field name
   */
  String getFieldName();

  /**
   * Gets the name of the declaring class.
   * @return the declaring class
   */
  String getFieldDeclaringClass();

}
