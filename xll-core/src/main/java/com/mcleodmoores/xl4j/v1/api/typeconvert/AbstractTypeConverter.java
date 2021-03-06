/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.typeconvert;

import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Base class for type converters, removes need for some boilerplate.
 */
public abstract class AbstractTypeConverter implements TypeConverter {
  private static final int DEFAULT_PRIORITY = 10;

  private final ExcelToJavaTypeMapping _excelToJavaTypeMapping;
  private final JavaToExcelTypeMapping _javaToExcelTypeMapping;
  private final int _priority;

  /**
   * Convenience constructor, produces converter with default priority.
   *
   * @param javaType
   *          the Java type, any object type
   * @param excelType
   *          the Excel type
   * @param priority
   *          the priority level, with larger values indicating higher priority
   */
  protected AbstractTypeConverter(final Class<?> javaType, final Class<?> excelType, final int priority) {
    ArgumentChecker.notNull(javaType, "javaType");
    ArgumentChecker.notNull(excelType, "excelType");
    _excelToJavaTypeMapping = ExcelToJavaTypeMapping.of(excelType, javaType);
    _javaToExcelTypeMapping = JavaToExcelTypeMapping.of(javaType, excelType);
    _priority = priority;
  }

  /**
   * Convenience constructor, produces converter with default priority.
   *
   * @param javaType
   *          the Java type, any object type
   * @param excelType
   *          the Excel type
   */
  protected AbstractTypeConverter(final Class<?> javaType, final Class<?> excelType) {
    this(javaType, excelType, DEFAULT_PRIORITY);
  }

  @Override
  public ExcelToJavaTypeMapping getExcelToJavaTypeMapping() {
    return _excelToJavaTypeMapping;
  }

  @Override
  public JavaToExcelTypeMapping getJavaToExcelTypeMapping() {
    return _javaToExcelTypeMapping;
  }

  @Override
  public int getPriority() {
    return _priority;
  }

}
