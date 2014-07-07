/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Annotation for method arguments to provide extra meta-data to Excel.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface XLArgument {
  /**
   * The name of the argument, as it is to appear in the function wizard.
   * If not supplied, the library will attempt to:
   *   1. Use the debug symbol if available (the class must have been compiled with debugging info)
   *   2. Use the JavaDoc name if available.
   *   3. Use 'arg<n>' where <n> is 1-based.
   */
  String name();
  /**
   * The description of the argument, as it is to appear in the function wizard.
   * If not supplied, this will default to the JavaDoc description if available.
   */
  String description();
  /**
   * Whether the argument is optional.
   * This defaults to false, i.e. not optional.
   */
  boolean optional() default false;
}
