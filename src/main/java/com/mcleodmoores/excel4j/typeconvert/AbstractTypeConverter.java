package com.mcleodmoores.excel4j.typeconvert;
import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.values.XLValue;


/**
 * Base class for type converters, removes need for some boilerplate.
 */
public abstract class AbstractTypeConverter implements TypeConverter {

  private static final int DEFAULT_PRIORITY = 10;
  
  private ExcelToJavaTypeMapping _excelToJavaTypeMapping;
  private JavaToExcelTypeMapping _javaToExcelTypeMapping;
  private int _priority;


  /**
   * Convenience constructor, produces converter with default priority.
   * @param javaType the Java type, any object type
   * @param excelType the Excel type, subclass of XLValue
   * @param priority the priority level, with larger values indicating higher priority
   */
  protected AbstractTypeConverter(final Type javaType, final Class<? extends XLValue> excelType, final int priority) {
    _excelToJavaTypeMapping = ExcelToJavaTypeMapping.of(excelType, javaType);
    _javaToExcelTypeMapping = JavaToExcelTypeMapping.of(javaType, excelType);
    _priority = priority;
  }
  
  /**
   * Convenience constructor, produces converter with default priority.
   * @param javaType the Java type, any object type
   * @param excelType the Excel type, subclass of XLValue
   */
  protected AbstractTypeConverter(final Type javaType, final Class<? extends XLValue> excelType) {
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
  public abstract XLValue toXLValue(Class<? extends XLValue> expectedClass, Object from);
  
  @Override
  public abstract Object toJavaObject(Class<?> expectedClass, XLValue from);
  
  @Override
  public int getPriority() {
    return _priority;
  }
}
