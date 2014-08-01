package com.mcleodmoores.excel4j.util;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for checking arguments.
 */
public final class ArgumentChecker {
  private static Logger s_logger = LoggerFactory.getLogger(ArgumentChecker.class);

  private ArgumentChecker() {
  }

  /**
   * Throws an exception if the argument is negative.
   * @param argument the object to check
   * @param name the name of the parameter
   */
  public static void notNegative(final int argument, final String name) {
    if (argument < 0) {
      s_logger.error("Argument {} was negative", name);
      throw new Excel4JRuntimeException("Value " + name + " was negative");
    }
  }

  /**
   * Throws an exception if the argument is negative.
   * @param argument the object to check
   * @param name the name of the parameter
   */
  public static void notNegative(final long argument, final String name) {
    if (argument < 0L) {
      s_logger.error("Argument {} was negative", name);
      throw new Excel4JRuntimeException("Value " + name + " was negative");
    }
  }

  /**
   * Throws an exception if the argument is negative.
   * @param argument the object to check
   * @param name the name of the parameter
   */
  public static void notNegative(final double argument, final String name) {
    if (argument < 0d) {
      s_logger.error("Argument {} was negative", name);
      throw new Excel4JRuntimeException("Value " + name + " was negative");
    }
  }

  /**
   * Throws an exception if the argument is not null.
   * @param argument the object to check
   * @param name the name of the parameter
   */
  public static void notNull(final Object argument, final String name) {
    if (argument == null) {
      s_logger.error("Argument {} was null", name);
      throw new Excel4JRuntimeException("Value " + name + " was not null");
    }
  }

  /**
   * Throws an exception if the array argument is not null or empty.
   * @param <E> type of array
   * @param argument the object to check
   * @param name the name of the parameter
   */
  public static <E> void notNullOrEmpty(final E[] argument, final String name) {
    if (argument == null) {
      s_logger.error("Argument {} was null", name);
      throw new Excel4JRuntimeException("Value " + name + " was not null");
    } else if (argument.length == 0) {
      s_logger.error("Argument {} was empty array", name);
      throw new Excel4JRuntimeException("Value " + name + " was empty array");
    }
  }

  /**
   * Throws an exception if the collection argument is not null or empty.
   * @param <E> type of array
   * @param argument the object to check
   * @param name the name of the parameter
   */
  public static <E> void notNullOrEmpty(final Collection<E> argument, final String name) {
    if (argument == null) {
      s_logger.error("Argument {} was null", name);
      throw new Excel4JRuntimeException("Value " + name + " was not null");
    } else if (argument.isEmpty()) {
      s_logger.error("Argument {} was empty collection", name);
      throw new Excel4JRuntimeException("Value " + name + " was empty collection");
    }
  }

  /**
   * Throws an exception if the string argument is not null or empty.
   * @param argument the String to check
   * @param name the name of the parameter
   */
  public static void notNullOrEmpty(final String argument, final String name) {
    if (argument == null) {
      s_logger.error("Argument {} was null", name);
      throw new Excel4JRuntimeException("Value " + name + " was not null");
    } else if (argument.length() == 0) {
      s_logger.error("Argument {} was empty string", name);
      throw new Excel4JRuntimeException("Value " + name + " was empty string");
    }
  }
}
