/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.core.Heap;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.ObjectXLObjectTypeConverter;

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
      @XLParameter(name = "object reference", description = "The object reference") final XLObject objectReference,
      @XLParameter(name = "field name", description = "The field name") final XLString fieldName) {
    try {
      final Excel excel = ExcelFactory.getInstance();
      final Object value = getField(objectReference, fieldName, excel);
      final TypeConverter typeConverter = excel.getTypeConverterRegistry().findConverter(value.getClass());
      return typeConverter.toXLValue(value);
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
      @XLParameter(name = "object reference", description = "The object reference") final XLObject objectReference,
      @XLParameter(name = "field name", description = "The field name") final XLString fieldName) {
    try {
      final Excel excel = ExcelFactory.getInstance();
      final Object value = getField(objectReference, fieldName, excel);
      final TypeConverter typeConverter = new ObjectXLObjectTypeConverter(excel);
      return typeConverter.toXLValue(value);
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
      @XLParameter(name = "class name", description = "The class name, fully qualified or short if registered") final XLString className,
      @XLParameter(name = "field name", description = "The field name") final XLString fieldName) {
    try {
      final Excel excel = ExcelFactory.getInstance();
      final Object value = getField(className, fieldName);
      final TypeConverter typeConverter = excel.getTypeConverterRegistry().findConverter(value.getClass());
      return typeConverter.toXLValue(value);
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
      @XLParameter(name = "class name", description = "The class name, fully qualified or short if registered") final XLString className,
      @XLParameter(name = "field name", description = "The field name") final XLString fieldName) {
    try {
      final Excel excel = ExcelFactory.getInstance();
      final Object value = getField(className, fieldName);
      final TypeConverter typeConverter = new ObjectXLObjectTypeConverter(excel);
      return typeConverter.toXLValue(value);
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
