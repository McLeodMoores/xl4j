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
  String name() default "";
  /**
   * The category this function should be registered in.
   * If this is not specified, it defaults to the class name.
   * @return the category
   */
  // TODO see StandardCategories.
  String category() default "";
  /**
   * The description of this function.
   * @return the description
   */
  String description() default "";
  /**
   * The help topic
   * @return the help topic
   */
  String helpTopic() default "";
  /**
   * Whether the function should be re-evaluated on any sheet changes.
   * In other words, is it a volatile function.  If not specified, it is
   * assumed the function is not volatile.
   * @return true, if this function is volatile
   */
  boolean isVolatile() default false;
  /**
   * Whether the function can be executed from multiple threads safely.
   * @return true, if this function can be executed concurrently safely
   */
  boolean isMultiThreadSafe() default true;
  /**
   * @return true, if this function needs access to macro-level features
   * Note this cannot be used in conjunction with isMultiThreadSafe, but is required if
   * access to certain call-backs or range references (XLLocalReference or XLMultiReference)
   * are needed.
   */
  boolean isMacroEquivalent() default false;
  /**
   * @return true, if this function executes asynchronously
   * Note this cannot be used in conjunction with volatile or macro equivalent, but is required if
   * access to certain call-backs or range references is needed.
   */
  boolean isAsynchronous() default false;
  /**
   * @return the way to handle results
   */
  TypeConversionMode typeConversionMode() default TypeConversionMode.SIMPLEST_RESULT;
  /**
   * The type of function
   * @return the function type, defaults to FUNCTION
   */
  FunctionType functionType() default FunctionType.FUNCTION;
}
