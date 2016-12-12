/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.javacode;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.Excel;
import com.mcleodmoores.xl4j.ExcelFactory;
import com.mcleodmoores.xl4j.TypeConversionMode;
import com.mcleodmoores.xl4j.XLArgument;
import com.mcleodmoores.xl4j.XLFunction;
import com.mcleodmoores.xl4j.XLNamespace;
import com.mcleodmoores.xl4j.heap.Heap;
import com.mcleodmoores.xl4j.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.ObjectXLObjectTypeConverter;
import com.mcleodmoores.xl4j.values.XLError;
import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLString;

/**
 * Class contain methods to retrieve fields from classes from Excel.
 */
@XLNamespace("J")
public final class JField {
  private static final Logger LOGGER = LoggerFactory.getLogger(JField.class);

  /**
   * Retrieves a member field from an instance, converting the result to an Excel type if possible.
   *
   * @param objectReference
   *          the object reference
   * @param fieldName
   *          the field name
   * @return the field
   */
  @XLFunction(
      name = "Field",
      description = "Return a named Java field",
      category = "Java",
      typeConversionMode = TypeConversionMode.PASSTHROUGH)
  public static Object jField(
      @XLArgument(name = "object reference", description = "The object reference") final XLObject objectReference,
      @XLArgument(name = "field name", description = "The field name") final XLString fieldName) {
    try {
      final Excel excel = ExcelFactory.getInstance();
      final Object value = getField(objectReference, fieldName, excel);
      final TypeConverter typeConverter = excel.getTypeConverterRegistry().findConverter(value.getClass());
      return typeConverter.toXLValue(null, value);
    } catch (final NoSuchFieldException | IllegalAccessException e) {
      LOGGER.error("Problem getting field called {} in {}: {}", fieldName.getValue(), objectReference.getClazz(), e);
      return XLError.Null;
    }
  }

  /**
   * Retrieves a member field from an instance, leaving the result as an object reference.
   *
   * @param objectReference
   *          the object reference
   * @param fieldName
   *          the field name
   * @return the field
   */
  @XLFunction(
      name = "FieldX",
      description = "Return a named Java field",
      category = "Java",
      typeConversionMode = TypeConversionMode.PASSTHROUGH)
  public static Object jFieldX(
      @XLArgument(name = "object reference", description = "The object reference") final XLObject objectReference,
      @XLArgument(name = "field name", description = "The field name") final XLString fieldName) {
    try {
      final Excel excel = ExcelFactory.getInstance();
      final Object value = getField(objectReference, fieldName, excel);
      final TypeConverter typeConverter = new ObjectXLObjectTypeConverter(excel);
      return typeConverter.toXLValue(null, value);
    } catch (final NoSuchFieldException | IllegalAccessException e) {
      LOGGER.error("Problem getting field called {} in {}: {}", fieldName.getValue(), objectReference.getClazz(), e);
      return XLError.Null;
    }
  }

  /**
   * Retrieves a static field from a named class, converting the result to an Excel type if possible.
   *
   * @param className
   *          the class name
   * @param fieldName
   *          the field name
   * @return the field
   */
  @XLFunction(
      name = "StaticField",
      description = "Return a named Java field",
      category = "Java",
      typeConversionMode = TypeConversionMode.PASSTHROUGH)
  public static Object jStaticField(
      @XLArgument(name = "class name", description = "The class name, fully qualified or short if registered") final XLString className,
      @XLArgument(name = "field name", description = "The field name") final XLString fieldName) {
    try {
      final Excel excel = ExcelFactory.getInstance();
      final Object value = getField(className, fieldName);
      final TypeConverter typeConverter = excel.getTypeConverterRegistry().findConverter(value.getClass());
      return typeConverter.toXLValue(null, value);
    } catch (final NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
      LOGGER.error("Problem getting field called {} in {}: {}", fieldName.getValue(), className.getValue(), e);
      return XLError.Null;
    }
  }

  /**
   * Retrieves a static field from an instance, leaving the result as an object reference.
   *
   * @param className
   *          the class name
   * @param fieldName
   *          the field name
   * @return the field
   */
  @XLFunction(
      name = "StaticFieldX",
      description = "Return a named Java field",
      category = "Java",
      typeConversionMode = TypeConversionMode.PASSTHROUGH)
  public static Object jStaticFieldX(
      @XLArgument(name = "class name", description = "The class name, fully qualified or short if registered") final XLString className,
      @XLArgument(name = "field name", description = "The field name") final XLString fieldName) {
    try {
      final Excel excel = ExcelFactory.getInstance();
      final Object value = getField(className, fieldName);
      final TypeConverter typeConverter = new ObjectXLObjectTypeConverter(excel);
      return typeConverter.toXLValue(null, value);
    } catch (final NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
      LOGGER.error("Problem getting field called {} in {}: {}", fieldName.getValue(), className.getValue(), e);
      return XLError.Null;
    }
  }

  private static Object getField(final XLObject objectReference, final XLString fieldName, final Excel excel)
      throws NoSuchFieldException, IllegalAccessException {
    final Heap heap = excel.getHeap();
    final Object object = heap.getObject(objectReference.getHandle());
    final Class<?> clazz = object.getClass();
    final Field field = clazz.getField(fieldName.getValue());
    return field.get(object);
  }

  private static Object getField(final XLString className, final XLString fieldName) throws ClassNotFoundException,
  NoSuchFieldException, IllegalAccessException {
    return resolveClass(className).getField(fieldName.getValue()).get(null);
  }

  private static Class<?> resolveClass(final XLString className) throws ClassNotFoundException {
    return Class.forName(className.getValue());
  }

  private JField() {
  }
}
