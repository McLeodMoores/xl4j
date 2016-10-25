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
 * An annotation for constructors to be exposed as an Excel user-defined function.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.CONSTRUCTOR })
public @interface XLConstructor {
  // CHECKSTYLE:OFF - this is because of a bug in Checkstyle regarding annotation @return JavaDocs.
  /**
   * The name of the user-defined function as used on the worksheet. If this is not specified, the class name is used.
   * 
   * @return the name
   */
  String name() default "";

  /**
   * The category this constructor should be registered in. If this is not specified, it defaults to the class name.
   * 
   * @return the category
   */
  String category() default "";

  /**
   * The description of this constructor.
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

}
