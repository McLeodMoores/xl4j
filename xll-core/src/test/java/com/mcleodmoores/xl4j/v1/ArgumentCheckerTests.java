package com.mcleodmoores.xl4j.v1;

import java.util.Collection;
import java.util.Collections;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Unit tests for ArgumentChecker.
 */
public class ArgumentCheckerTests {

  /**
   * Tests notNegative().
   */
  @Test(expectedExceptions = XL4JRuntimeException.class, expectedExceptionsMessageRegExp = ".*?message2.*")
  public void testNotNegativeInt() {
    ArgumentChecker.notNegative(1, "message");
    ArgumentChecker.notNegative(-1, "message2");
  }

  /**
   * Tests notNegative().
   */
  @Test(expectedExceptions = XL4JRuntimeException.class, expectedExceptionsMessageRegExp = ".*?message2.*")
  public void testNotNegativeLong() {
    ArgumentChecker.notNegative(1L, "message");
    ArgumentChecker.notNegative(-1L, "message2");
  }

  /**
   * Tests notNegative().
   */
  @Test(expectedExceptions = XL4JRuntimeException.class, expectedExceptionsMessageRegExp = ".*?message2.*")
  public void testNotNegativeDouble() {
    ArgumentChecker.notNegative(1d, "message");
    ArgumentChecker.notNegative(-1d, "message2");
  }

  private static final Collection<String> NON_EMPTY = Collections.singletonList("Hello");

  /**
   * Tests notNullOrEmpty().
   */
  @Test(expectedExceptions = XL4JRuntimeException.class, expectedExceptionsMessageRegExp = ".*?message2.*")
  public void testNotNullOrEmptyCollection() {
    ArgumentChecker.notNullOrEmpty(NON_EMPTY, "message");
    ArgumentChecker.notNullOrEmpty(Collections.emptyList(), "message2");
  }

  /**
   * Tests notNullOrEmpty().
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNotNullOrEmptyCollectionNull() {
    ArgumentChecker.notNullOrEmpty((Collection<String>) null, "message2");
  }

  /**
   * Tests notNullOrEmpty().
   */
  @Test(expectedExceptions = XL4JRuntimeException.class, expectedExceptionsMessageRegExp = ".*?message2.*")
  public void testNotNullOrEmptyArray() {
    ArgumentChecker.notNullOrEmpty(new String[] { "Hello" }, "message");
    ArgumentChecker.notNullOrEmpty(new String[0], "message2");
  }

  /**
   * Tests notNullOrEmpty().
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNotNullOrEmptyArrayNull() {
    ArgumentChecker.notNullOrEmpty((String[]) null, "message2");
  }

  /**
   * Tests notNullOrEmpty().
   */
  @Test(expectedExceptions = XL4JRuntimeException.class, expectedExceptionsMessageRegExp = ".*?message2.*")
  public void testNotNullOrEmptyString() {
    ArgumentChecker.notNullOrEmpty("Hello", "message");
    ArgumentChecker.notNullOrEmpty("", "message2");
  }

  /**
   * Tests notNullOrEmpty().
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNotNullOrEmptyStringNull() {
    ArgumentChecker.notNullOrEmpty((String) null, "message");
  }

  /**
   * Tests notNull().
   */
  @Test(expectedExceptions = XL4JRuntimeException.class, expectedExceptionsMessageRegExp = ".*?message2.*")
  public void testNotNull() {
    ArgumentChecker.notNull("Hello", "message");
    ArgumentChecker.notNull("", "message1.5");
    ArgumentChecker.notNull(null, "message2");
  }

}
