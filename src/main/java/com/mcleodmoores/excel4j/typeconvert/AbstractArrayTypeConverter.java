package com.mcleodmoores.excel4j.typeconvert;

import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Base class for type converters, removes need for some boilerplate.
 */
public abstract class AbstractArrayTypeConverter implements TypeConverter<XLValue[], Object[]> {
  private static final int DEFAULT_PRIORITY = 10;

  private final ExcelToJavaTypeMapping _excelToJavaTypeMapping;
  private final JavaToExcelTypeMappingI _javaToExcelTypeMapping;
  private final int _priority;

  /**
   * Convenience constructor, produces converter with default priority.
   * @param javaType the Java type, any object type
   * @param excelType the Excel type, subclass of XLValue
   * @param priority the priority level, with larger values indicating higher priority
   */
  protected AbstractArrayTypeConverter(final Class<?> javaType, final Class<? extends XLValue> excelType, final int priority) {
    ArgumentChecker.notNull(javaType, "javaType");
    ArgumentChecker.notNull(excelType, "excelType");
    _excelToJavaTypeMapping = ExcelToJavaTypeMapping.of(excelType, javaType);
    _javaToExcelTypeMapping = ScalarJavaToExcelTypeMapping.of(javaType, excelType);
    _priority = priority;
  }

  /**
   * Convenience constructor, produces converter with default priority.
   * @param javaType the Java type, any object type
   * @param excelType the Excel type, subclass of XLValue
   */
  protected AbstractArrayTypeConverter(final Class<?> javaType, final Class<? extends XLValue> excelType) {
    this(javaType, excelType, DEFAULT_PRIORITY);
  }

  @Override
  public ExcelToJavaTypeMapping getExcelToJavaTypeMapping() {
    return _excelToJavaTypeMapping;
  }

  @Override
  public JavaToExcelTypeMappingI getJavaToExcelTypeMapping() {
    return _javaToExcelTypeMapping;
  }

  @Override
  public int getPriority() {
    return _priority;
  }

  // REVIEW emcleod 21-7-2014 hashCode and equals?
}
