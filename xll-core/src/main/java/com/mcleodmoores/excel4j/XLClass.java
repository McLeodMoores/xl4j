/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that allows all constructors and methods of a class to be exposed to Excel. By default, methods from
 * Object (e.g. equals, hashCode) are not exposed.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface XLClass {
  // CHECKSTYLE:OFF  - this is because of a bug in Checkstyle regarding annotation @return JavaDocs.

  /**
   * The name of the user-defined function as used on the worksheet. If this is not specified, the class name
   * is used.
   * @return  the name
   */
  String name() default "";

  /**
   * The category that the constructors and methods should be registered in.
   * If this is not specified, it defaults to the class name.
   * @return the category
   */
  String category() default "";

  /**
   * The description of this class.
   * @return the description
   */
  String description() default "";

  /**
   * The help topic.
   * @return the help topic
   */
  String helpTopic() default "";

  /**
   * Set to true if methods from Object (clone, equals, finalize, getClass, hashCode, notify, notifyAll, toString, wait)
   * should be exposed to Excel.
   * @return  true or false
   */
  boolean includeObjectMethods() default false;

  String[] excludedMethods() default {};
}
