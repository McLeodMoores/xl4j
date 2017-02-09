/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.javacode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.Excel;
import com.mcleodmoores.xl4j.ExcelFactory;
import com.mcleodmoores.xl4j.TypeConversionMode;
import com.mcleodmoores.xl4j.XLFunction;
import com.mcleodmoores.xl4j.XLNamespace;
import com.mcleodmoores.xl4j.XLParameter;
import com.mcleodmoores.xl4j.typeconvert.converters.EnumXLStringTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.converters.ObjectXLObjectTypeConverter;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLError;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

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
