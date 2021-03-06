/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.util;

import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

/**
 * Utility class for checking arguments.
 */
public final class ArgumentChecker {
  private static final Logger LOGGER = LoggerFactory.getLogger(ArgumentChecker.class);

  private ArgumentChecker() {
  }

  /**
   * Throws an exception if the argument is negative.
   *
   * @param argument
   *          the object to check
   * @param name
   *          the name of the parameter
   * @return the argument
   */
  public static int notNegative(final int argument, final String name) {
    if (argument < 0) {
      LOGGER.error("Argument {} was negative", name);
      throw new XL4JRuntimeException("Value for \"" + name + "\" was negative");
    }
    return argument;
  }

  /**
   * Throws an exception if the argument is negative.
   *
   * @param argument
   *          the object to check
   * @param name
   *          the name of the parameter
   * @return the argument
   */
  public static long notNegative(final long argument, final String name) {
    if (argument < 0L) {
      LOGGER.error("Argument {} was negative", name);
      throw new XL4JRuntimeException("Value for \"" + name + "\" was negative");
    }
    return argument;
  }

  /**
   * Throws an exception if the argument is negative.
   *
   * @param argument
   *          the object to check
   * @param name
   *          the name of the parameter
   * @return the argument
   */
  public static double notNegative(final double argument, final String name) {
    if (argument < 0d) {
      LOGGER.error("Argument {} was negative", name);
      throw new XL4JRuntimeException("Value for \"" + name + "\" was negative");
    }
    return argument;
  }

  /**
   * Throws an exception if the argument is null.
   *
   * @param <E>
   *          the type of the argument
   * @param argument
   *          the object to check
   * @param name
   *          the name of the parameter
   * @return the argument
   */
  public static <E> E notNull(final E argument, final String name) {
    if (argument == null) {
      LOGGER.error("Argument {} was null", name);
      throw new XL4JRuntimeException("Value for \"" + name + "\" was null");
    }
    return argument;
  }

  /**
   * Throws an exception if the array argument is null or empty.
   *
   * @param <E>
   *          type of array
   * @param argument
   *          the object to check
   * @param name
   *          the name of the parameter
   * @return the argument
   */
  public static <E> E[] notNullOrEmpty(final E[] argument, final String name) {
    if (argument == null) {
      LOGGER.error("Argument {} was null", name);
      throw new XL4JRuntimeException("Value for \"" + name + "\" was null");
    } else if (argument.length == 0) {
      LOGGER.error("Argument {} was empty array", name);
      throw new XL4JRuntimeException("Value for " + name + " was empty array");
    }
    return argument;
  }

  /**
   * Throws an exception if the collection argument is null or empty.
   *
   * @param <E>
   *          type of array
   * @param argument
   *          the object to check
   * @param name
   *          the name of the parameter
   * @return the argument
   */
  public static <E> Collection<E> notNullOrEmpty(final Collection<E> argument, final String name) {
    if (argument == null) {
      LOGGER.error("Argument {} was null", name);
      throw new XL4JRuntimeException("Value for \"" + name + "\" was null");
    } else if (argument.isEmpty()) {
      LOGGER.error("Argument {} was empty collection", name);
      throw new XL4JRuntimeException("Value " + name + " was empty collection");
    }
    return argument;
  }

  /**
   * Throws an exception if the string argument is null or empty.
   *
   * @param argument
   *          the String to check
   * @param name
   *          the name of the parameter
   * @return the argument
   */
  public static String notNullOrEmpty(final String argument, final String name) {
    if (argument == null) {
      LOGGER.error("Argument {} was null", name);
      throw new XL4JRuntimeException("Value for \"" + name + "\" was null");
    } else if (argument.length() == 0) {
      LOGGER.error("Argument {} was empty string", name);
      throw new XL4JRuntimeException("Value " + name + " was empty string");
    }
    return argument;
  }

  /**
   * Throws an exception if the array argument is null or any of its elements are null.
   *
   * @param <E>
   *          type of array
   * @param argument
   *          the object to check
   * @param name
   *          the name of the parameter
   * @return the argument
   */
  public static <E> E[] notNullArray(final E[] argument, final String name) {
    if (argument == null) {
      LOGGER.error("Argument {} was null", name);
      throw new XL4JRuntimeException("Value for \"" + name + "\" was null");
    }
    for (int i = 0; i < argument.length; i++) {
      if (argument[i] == null) {
        LOGGER.error("Argument {} was has null element at position {}", name, i);
        throw new XL4JRuntimeException("Argument " + name + " has null element at position " + i);
      }
    }
    return argument;
  }

  /**
   * Throws an exception if the array argument is null or any of its elements are null.
   *
   * @param <E>
   *          type of array
   * @param argument
   *          the object to check
   * @param name
   *          the name of the parameter
   * @return the argument
   */
  public static <E> Collection<E> notNullCollection(final Collection<E> argument, final String name) {
    if (argument == null) {
      LOGGER.error("Argument {} was null", name);
      throw new XL4JRuntimeException("Value for \"" + name + "\" was null");
    }
    final Iterator<E> iter = argument.iterator();
    for (int i = 0; i < argument.size(); i++) {
      if (iter.next() == null) {
        LOGGER.error("Argument {} was has null element at position {}", name, i);
        throw new XL4JRuntimeException("Argument " + name + " has null element at position " + i);
      }
    }
    return argument;
  }

  /**
   * Throws an exception if the condition is not true.
   *
   * @param condition
   *          the condition to check
   * @param message
   *          the message if the condition is false
   */
  public static void isTrue(final boolean condition, final String message) {
    if (!condition) {
      LOGGER.error(message);
      throw new XL4JRuntimeException(message);
    }
  }

  /**
   * Throws an exception if the condition is not true.
   *
   * @param condition
   *          the condition to check
   * @param message
   *          the message if the condition is false
   * @param args
   *          the message arguments
   */
  public static void isTrue(final boolean condition, final String message, final Object... args) {
    if (!condition) {
      LOGGER.error(message);
      throw new XL4JRuntimeException(MessageFormatter.arrayFormat(message, args).getMessage());
    }
  }

  /**
   * Throws an exception if the condition is not false.
   *
   * @param condition
   *          the condition to check
   * @param message
   *          the message if the condition is true
   */
  public static void isFalse(final boolean condition, final String message) {
    if (condition) {
      LOGGER.error(message);
      throw new XL4JRuntimeException(message);
    }
  }

  /**
   * Throws an exception if the condition is not false.
   *
   * @param condition
   *          the condition to check
   * @param message
   *          the message if the condition is true
   * @param args
   *          the message arguments
   */
  public static void isFalse(final boolean condition, final String message, final Object... args) {
    if (condition) {
      LOGGER.error(message);
      throw new XL4JRuntimeException(MessageFormatter.arrayFormat(message, args).getMessage());
    }
  }
}
