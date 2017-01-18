/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotations for constants (fields, values from enums) to be exposed to Excel.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface XLConstant {

  /**
   * The name of the function that returns the value to the worksheet. If this is not specified, the field name is used.
   *
   * @return the name
   */
  String name() default "";

  /**
   * The category that the function should be registered in. If this is not specified, it defaults to the class name.
   *
   * @return the category
   */
  String category() default "";

  /**
   * The description of this function.
   *
   * @return the description
   */
  String description() default "";

  /**
   * The help topic.
   *
   * @return the help topic
   */
  String helpTopic() default "";

  /**
   * The conversion mode to be used on the value returned to the sheet.
   *
   * @return the conversion mode
   */
  TypeConversionMode typeConversionMode() default TypeConversionMode.SIMPLEST_RESULT;
}
