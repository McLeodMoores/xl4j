/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.EnumXLStringTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.ObjectXLObjectTypeConverter;

/**
 *
 */
@XLNamespace("J")
public class JEnum {
  private static final Logger LOGGER = LoggerFactory.getLogger(JEnum.class);
  private static final EnumXLStringTypeConverter ENUM_CONVERTER = new EnumXLStringTypeConverter();

  @XLFunction(
      name = "Enum",
      description = "Return all values of an enum as an array",
      category = "Java",
      typeConversionMode = TypeConversionMode.PASSTHROUGH)
  public static Object jEnum(
      @XLParameter(name = "enum name", description = "The fully-qualified enum name") final XLString enumName) {
    try {
      final Class<?> clazz = Class.forName(enumName.getValue());
      final Object[] values = clazz.getEnumConstants();
      final XLValue[][] array = new XLValue[values.length][1];
      for (int i = 0; i < values.length; i++) {
        array[i][0] = (XLValue) ENUM_CONVERTER.toXLValue(values[i]);
      }
      return XLArray.of(array);
    } catch (final ClassNotFoundException e) {
      LOGGER.error("Problem getting enum values for {}: {}", enumName, e);
      return XLError.Null;
    }
  }

  @XLFunction(
      name = "Enum",
      description = "Return all values of an enum as an array",
      category = "Java",
      typeConversionMode = TypeConversionMode.PASSTHROUGH)
  public static Object jEnumX(
      @XLParameter(name = "enum name", description = "The fully-qualified enum name") final XLString enumName) {
    try {
      final Excel excel = ExcelFactory.getInstance();
      final ObjectXLObjectTypeConverter typeConverter = new ObjectXLObjectTypeConverter(excel);
      final Class<?> clazz = Class.forName(enumName.getValue());
      final Object[] values = clazz.getEnumConstants();
      final XLValue[][] array = new XLValue[values.length][1];
      for (int i = 0; i < values.length; i++) {
        array[i][0] = (XLValue) typeConverter.toXLValue(values[i]);
      }
      return XLArray.of(array);
    } catch (final ClassNotFoundException e) {
      LOGGER.error("Problem getting enum values for {}: {}", enumName, e);
      return XLError.Null;
    }
  }
}
