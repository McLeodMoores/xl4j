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
  // CHECKSTYLE:OFF  - this is because of a bug in Checkstyle regarding annotation @return JavaDocs.
  /**
   * The name of the user defined function as used on the worksheet.
   * If this is not specified, the method name is used, with the name processed into
   * leading capital camel case.  E.g. myMethod() becomes MyMethod().
   * @return the name
   */
  String name();
  /**
   * The category this function should be registered in. 
   * If this is not specified, it defaults to the class name.
   * TODO see StandardCategories.
   * @return the category
   */
  String category();
  /**
   * The description of this function.
   * @return the description
   */
  String description();
  /**
   * Whether the function should be re-evaluated on any sheet changes.
   * In other words, is it a volatile function.  If not specified, it is 
   * assumed the function is not volatile.
   * @return true, if this function is volatile
   */
  boolean isVolatile() default false;
  /**
   * Whether to allow LocalReference or MultiReferences to be passed to this
   * function.  This requires different security characteristics on the sheet,
   * so should be avoided where possible.  If not specified, it is assumed
   * the function does not allow references.
   * @return true, if this function is allowed to handle references
   */
  boolean allowReferences() default false;
}
