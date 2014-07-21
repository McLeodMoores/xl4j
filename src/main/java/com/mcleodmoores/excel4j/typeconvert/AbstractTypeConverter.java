package com.mcleodmoores.excel4j.typeconvert;

import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Base class for type converters, removes need for some boilerplate.
 */
public abstract class AbstractTypeConverter implements TypeConverter {
  // REVIEW emcleod 21-7-2014 No input checks - NullPointerExceptions are
  // wanted?
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
   *          the Excel type, subclass of XLValue
   * @param priority
   *          the priority level, with larger values indicating higher priority
   */
  protected AbstractTypeConverter(final Class<?> javaType, final Class<? extends XLValue> excelType, final int priority) {
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
   *          the Excel type, subclass of XLValue
   */
  protected AbstractTypeConverter(final Class<?> javaType, final Class<? extends XLValue> excelType) {
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

  // REVIEW emcleod 21-7-2014 - expecting ClassCastException?
  // expectedClass doesn't appear to be tested in the majority
  // of the converters. Would it be safest to do so?
  @Override
  public abstract XLValue toXLValue(Class<? extends XLValue> expectedClass, Object from);

  // REVIEW emcleod 21-7-2014 - expecting ClassCastException?
  // expectedClass doesn't appear to be tested in the majority
  // of the converters. Would it be safest to do so?
  @Override
  public abstract Object toJavaObject(Class<?> expectedClass, XLValue from);

  @Override
  public int getPriority() {
    return _priority;
  }

  // REVIEW emcleod 21-7-2014 hashCode and equals?
}
