/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for constructors and methods for classes to be exposed as an Excel user-defined functions (UDF).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XLFunctions {

  /**
   * The prefix for the class name (for constructors) or method name that is used to construct the names
   * of the user-defined function.
   *
   * @return the name
   */
  String prefix() default "";

  /**
   * The category these functions should be registered in. If this is not specified, it defaults to the class name.
   *
   * @return the category
   */
  String category() default "";

  /**
   * The description of this functions.
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
   * Whether the functions should be re-evaluated on any sheet changes. If not specified, it is
   * assumed the functions are not volatile.
   *
   * @return true, if these functions are volatile
   */
  boolean isVolatile() default false;

  /**
   * Whether the functions can be executed from multiple threads safely.
   *
   * @return true, if these functions can be executed concurrently safely
   */
  boolean isMultiThreadSafe() default true;

  /**
   * @return true, if these functions need access to macro-level features. Note this cannot be used in conjunction with
   *         {@link #isMultiThreadSafe}, but is required if access to certain call-backs or range references
   *         ({@link com.mcleodmoores.xl4j.values.XLLocalReference} or {@link com.mcleodmoores.xl4j.values.XLMultiReference})
   *         are needed.
   */
  boolean isMacroEquivalent() default false;

  /**
   * @return the way to handle results
   */
  TypeConversionMode typeConversionMode() default TypeConversionMode.SIMPLEST_RESULT;

  /**
   * The type of function.
   *
   * @return the function type, defaults to FUNCTION
   */
  FunctionType functionType() default FunctionType.FUNCTION;

  /**
   * Indicates whether the function is likely to be slow.
   *
   * @return true if the function is slow, defaults to false
   */
  boolean isLongRunning() default false;

  boolean isAutoAsynchronous() default false;

  boolean isManualAsynchronous() default false;

  boolean isCallerRequired() default false;
}
