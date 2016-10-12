package com.mcleodmoores.xl4j;

import java.util.Collection;
import java.util.Collections;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;

/**
 * Unit tests for ArgumentChecker.
 */
public class ArgumentCheckerTests {

  @Test(expectedExceptions = Excel4JRuntimeException.class, expectedExceptionsMessageRegExp = ".*?message2.*")
  public void testNotNegativeInt() {
    ArgumentChecker.notNegative(1, "message");
    ArgumentChecker.notNegative(-1, "message2");
  }

  @Test(expectedExceptions = Excel4JRuntimeException.class, expectedExceptionsMessageRegExp = ".*?message2.*")
  public void testNotNegativeLong() {
    ArgumentChecker.notNegative(1L, "message");
    ArgumentChecker.notNegative(-1L, "message2");
  }

  @Test(expectedExceptions = Excel4JRuntimeException.class, expectedExceptionsMessageRegExp = ".*?message2.*")
  public void testNotNegativeDouble() {
    ArgumentChecker.notNegative(1d, "message");
    ArgumentChecker.notNegative(-1d, "message2");
  }

  private static final Collection<String> NON_EMPTY = Collections.singletonList("Hello");

  @Test(expectedExceptions = Excel4JRuntimeException.class, expectedExceptionsMessageRegExp = ".*?message2.*")
  public void testNotNullOrEmptyCollection() {
    ArgumentChecker.notNullOrEmpty(NON_EMPTY, "message");
    ArgumentChecker.notNullOrEmpty(Collections.emptyList(), "message2");
  }

  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNotNullOrEmptyCollectionNull() {
    ArgumentChecker.notNullOrEmpty((Collection<String>) null, "message2");
  }

  @Test(expectedExceptions = Excel4JRuntimeException.class, expectedExceptionsMessageRegExp = ".*?message2.*")
  public void testNotNullOrEmptyArray() {
    ArgumentChecker.notNullOrEmpty(new String[] { "Hello" }, "message");
    ArgumentChecker.notNullOrEmpty(new String[0], "message2");
  }

  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNotNullOrEmptyArrayNull() {
    ArgumentChecker.notNullOrEmpty((String[]) null, "message2");
  }

  @Test(expectedExceptions = Excel4JRuntimeException.class, expectedExceptionsMessageRegExp = ".*?message2.*")
  public void testNotNullOrEmptyString() {
    ArgumentChecker.notNullOrEmpty("Hello", "message");
    ArgumentChecker.notNullOrEmpty("", "message2");
  }

  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNotNullOrEmptyStringNull() {
    ArgumentChecker.notNullOrEmpty((String) null, "message");
  }

  @Test(expectedExceptions = Excel4JRuntimeException.class, expectedExceptionsMessageRegExp = ".*?message2.*")
  public void testNotNull() {
    ArgumentChecker.notNull("Hello", "message");
    ArgumentChecker.notNull("", "message1.5");
    ArgumentChecker.notNull(null, "message2");
  }

}
