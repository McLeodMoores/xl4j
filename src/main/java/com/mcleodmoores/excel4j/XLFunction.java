/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for method to be exposed as an Excel user-defined function (UDF).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface XLFunction {
  /**
   * The name of the user defined function as used on the worksheet.
   * If this is not specified, the method name is used, with the name processed into
   * leading capital camel case.  E.g. myMethod() becomes MyMethod().
   */
  String name();
  /**
   * The category this function should be registered in. 
   * If this is not specified, it defaults to the class name.
   * @see StandardCategories.
   */
  String category();
  /**
   * The description of this function.
   */
  String description();
  /**
   * Whether the function should be re-evaluated on any sheet changes.
   * In other words, is it a volatile function.  If not specified, it is 
   * assumed the function is not volatile.
   */
  boolean isVolatile() default false;
  /**
   * Whether to allow LocalReference or MultiReferences to be passed to this
   * function.  This requires different security characteristics on the sheet,
   * so should be avoided where possible.  If not specified, it is assumed
   * the function does not allow references.
   */
  boolean allowReferences() default false;
}
