/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for methods and constructors to be exposed as an Excel user-defined function (UDF).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface XLFunction {

  /**
   * The name of the user-defined function as used on the worksheet. If this is not specified,
   * the method name or class name is used (for methods or constructors, respectively), with the name
   * processed into leading capital camel case, e.g. myMethod() becomes MyMethod().
   *
   * @return the name
   */
  String name() default "";

  /**
   * The category this function should be registered in. If this is not specified, it defaults to the class name.
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
   * Whether the function should be re-evaluated on any sheet changes. In other words, is it a volatile function. If not specified, it is
   * assumed the function is not volatile.
   *
   * @return true, if this function is volatile
   */
  boolean isVolatile() default false;

  /**
   * Whether the function can be executed from multiple threads safely.
   *
   * @return true, if this function can be executed concurrently safely
   */
  boolean isMultiThreadSafe() default true;

  /**
   * @return true, if this function needs access to macro-level features. Note this cannot be used in conjunction with isMultiThreadSafe, but
   *         is required if access to certain call-backs or range references (XLLocalReference or XLMultiReference) are needed.
   */
  boolean isMacroEquivalent() default false;

  /**
   * @return the way to handle results
   */
  TypeConversionMode typeConversionMode() default TypeConversionMode.SIMPLEST_RESULT;

  /**
   * The type of function.
   * @return the function type, defaults to FUNCTION
   */
  FunctionType functionType() default FunctionType.FUNCTION;

  /**
   * Indicates whether the function is likely to be slow.
   * @return true if the function is slow, defaults to false
   */
  boolean isLongRunning() default false;

  /**
   * Indicates that a function should be run in a separate thread, sourced from a special thread-pool
   * and that Excel should be notified asynchronously when the function has finished and returned a 
   * result.  Start of execution may be delayed during periods of high concurrency to avoid excessive
   * thread creation.  Result may be discarded if job is cancelled.
   * @return true if the function should be made automatically asynchronous
   */
  boolean isAutoAsynchronous() default false;

  /**
   * This is not currently implemented.  The intention is that it indicates a function that is able to
   * asynchronously call back into Excel to notify it of a result.  It could therefore be from a 
   * calculation done on a separate thread, the result of a long running I/O operation with a callback 
   * or something similar.  Typically the function would be passed an extra argument containing a handle
   * to include with the callback of type XLBigData.
   * @return true if the function should be passed a handle for asynchronous callback
   */
  boolean isManualAsynchronous() default false;

  /**
   * This is not currently implemented.  The intention is that this indicates that an extra argument
   * should be provided to the function than includes the 'caller' information.  This is usually an
   * XLMultiReference indicating the cell or cells that the results are to be delivered to, which allows
   * all kinds of useful behaviours.
   * @return true if the function should be passed caller information.
   */
  boolean isCallerRequired() default false;
}
