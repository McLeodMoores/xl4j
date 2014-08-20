/*
 * COM Java wrapper 
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>.
 * Released under the GNU General Public License.
 */
package uk.co.beerdragon.comjvm.ex;

import java.io.Serializable;

/**
 * Representation of the HRESULT data type used for return codes in the COM API.
 * <p>
 * Typically the HRESULT is held as a {@code int} and decoded through the static methods on this
 * class. If necessary an instance of this can be used to "box" the integer result for better
 * clarity than passing a {@link Integer}.
 */
public final class HRESULT implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final int S_MASK = 0x80000000;

  private static final int R_MASK = 0x40000000;

  private static final int C_MASK = 0x20000000;

  private static final int N_MASK = 0x10000000;

  private static final int X_MASK = 0x08000000;

  private static final int FACILITY_MASK = 0x07FF0000;

  private static final int FACILITY_SHIFT = 16;

  private static final int CODE_MASK = 0x0000FFFF;

  /**
   * Operation successful.
   */
  public static final int S_OK = 0x00000000;

  /**
   * FALSE.
   */
  public static final int S_FALSE = 0x00000001;

  /**
   * Operation aborted.
   */
  public static final int E_ABORT = 0x80040004;

  /**
   * General access denied error.
   */
  public static final int E_ACCESSDENIED = 0x80070005;

  /**
   * Unspecified failure.
   */
  public static final int E_FAIL = 0x80004005;

  /**
   * Handle that is not valid.
   */
  public static final int E_HANDLE = 0x80070006;

  /**
   * One or more arguments are not valid.
   */
  public static final int E_INVALIDARG = 0x80070057;

  /**
   * No such interface supported.
   */
  public static final int E_NOINTERFACE = 0x8004002;

  /**
   * Not implemented.
   */
  public static final int E_NOTIMPL = 0x80040001;

  /**
   * Failed to allocate necessary memory.
   */
  public static final int E_OUTOFMEMORY = 0x800700E;

  /**
   * Pointer that is not valid.
   */
  public static final int E_POINTER = 0x80004003;

  /**
   * Unexpected failure.
   */
  public static final int E_UNEXPECTED = 0x8000FFFF;

  private final int _value;

  /**
   * Creates a new instance.
   * 
   * @param value
   *          the 32-bit HRESULT code
   */
  public HRESULT (final int value) {
    _value = value;
  }

  /**
   * Tests if the {@code S}, severity, bit is set.
   * 
   * @param value
   *          {@code HRESULT} to test
   * @return {@code true} if set, {@code false} otherwise
   */
  public static boolean getS (final int value) {
    return (value & S_MASK) != 0;
  }

  /**
   * Tests if the {@code S}, severity, bit is set.
   * 
   * @return {@code true} if set, {@code false} otherwise
   */
  public boolean getS () {
    return getS (_value);
  }

  /**
   * Tests for a success code (the {@code S} bit is clear).
   * 
   * @param value
   *          {@code HRESULT} to test
   * @return {@code true} if successful, {@code false} otherwise
   */
  public static boolean isSuccess (final int value) {
    return !getS (value);
  }

  /**
   * Tests for a success code (the {@code S} bit is clear).
   * 
   * @return {@code true} if successful, {@code false} otherwise
   */
  public boolean isSuccess () {
    return isSuccess (_value);
  }

  /**
   * Tests for a failure code (the {@code S} bit is set).
   * 
   * @param value
   *          {@code HRESULT} to test
   * @return {@code true} if a failure, {@code false} otherwise
   */
  public static boolean isFailure (final int value) {
    return getS (value);
  }

  /**
   * Tests for a failure code (the {@code S} bit is set).
   * 
   * @return {@code true} if a failure, {@code false} otherwise
   */
  public boolean isFailure () {
    return isFailure (_value);
  }

  /**
   * Tests if the {@code R}, reserved, bit is set.
   * 
   * @param value
   *          {@code HRESULT} to test
   * @return {@code true} if set, {@code false} otherwise
   */
  public static boolean getR (final int value) {
    return (value & R_MASK) != 0;
  }

  /**
   * Tests if the {@code R}, reserved, bit is set.
   * 
   * @return {@code true} if set, {@code false} otherwise
   */
  public boolean getR () {
    return getR (_value);
  }

  /**
   * Tests for a severe failure (the {@code R} bit is set when used on some systems).
   * 
   * @param value
   *          {@code HRESULT} to test
   * @return {@code true} if set, {@code false} otherwise
   */
  public static boolean isSevereFailure (final int value) {
    return getR (value);
  }

  /**
   * Tests for a severe failure (the {@code R} bit is set when used on some systems).
   * 
   * @return {@code true} if set, {@code false} otherwise
   */
  public boolean isSevereFailure () {
    return isSevereFailure (_value);
  }

  /**
   * Tests if the {@code C}, customer, bit is set.
   * 
   * @param value
   *          {@code HRESULT} to test
   * @return {@code true} if set, {@code false} otherwise
   */
  public static boolean getC (final int value) {
    return (value & C_MASK) != 0;
  }

  /**
   * Tests if the {@code C}, customer, bit is set.
   * 
   * @return {@code true} if set, {@code false} otherwise
   */
  public boolean getC () {
    return getC (_value);
  }

  /**
   * Tests for a customer defined code (the {@code C} bit is set).
   * 
   * @param value
   *          {@code HRESULT} to test
   * @return {@code true} if customer defined, {@code false} otherwise
   */
  public static boolean isCustomer (final int value) {
    return getC (value);
  }

  /**
   * Tests for a customer defined code (the {@code C} bit is set).
   * 
   * @return {@code true} if customer defined, {@code false otherwise}
   */
  public boolean isCustomer () {
    return isCustomer (_value);
  }

  /**
   * Tests for a Microsoft defined code (the {@code C} bit is clear).
   * 
   * @param value
   *          {@code HRESULT} to test
   * @return {@code true} if Microsoft defined, {@code false} otherwise
   */
  public static boolean isMicrosoft (final int value) {
    return !getC (value);
  }

  /**
   * Tests for a Microsoft defined code (the {@code C} bit is clear).
   * 
   * @return {@code true} if Microsoft defined, {@code false} otherwise
   */
  public boolean isMicrosoft () {
    return isMicrosoft (_value);
  }

  /**
   * Tests if the {@code N}, reserved, bit is set.
   * 
   * @param value
   *          {@code HRESULT} to test
   * @return {@code true} if set, {@code false} otherwise
   */
  public static boolean getN (final int value) {
    return (value & N_MASK) != 0;
  }

  /**
   * Tests if the {@code N}, reserved, bit is set.
   * 
   * @return {@code true} if set, {@code false} otherwise
   */
  public boolean getN () {
    return getN (_value);
  }

  /**
   * Tests for a mapped NT Status code (the {@code N} bit is set when used on some systems).
   * 
   * @param value
   *          {@code HRESULT} to test
   * @return {@code true} if this is a mapped code, {@code false} otherwise
   */
  public static boolean isNTStatus (final int value) {
    return getN (value);
  }

  /**
   * Tests for a mapped NT Status code (the {@code N} bit is set when used on some systems).
   * 
   * @return {@code true} if this is a mapped code, {@code false} otherwise
   */
  public boolean isNTStatus () {
    return isNTStatus (_value);
  }

  /**
   * Tests if the {@code X}, reserved, bit is set.
   * 
   * @param value
   *          {@code HRESULT} to test
   * @return {@code true} if set, {@code false} otherwise
   */
  public static boolean getX (final int value) {
    return (value & X_MASK) != 0;
  }

  /**
   * Tests if the {@code X}, reserved, bit is set.
   * 
   * @return {@code true} if set, {@code false} otherwise
   */
  public boolean getX () {
    return getX (_value);
  }

  /**
   * Tests for a message identifier (the {@code X} bit is set when used on some systems).
   * 
   * @param value
   *          {@code HRESULT} to test
   * @return {@code true} if this is a message identifier, {@code false} otherwise
   */
  public static boolean isMessageID (final int value) {
    return getX (value);
  }

  /**
   * Tests for a message identifier (the {@code X} bit is set when used on some systems).
   * 
   * @return {@code true} if this is a message identifier, {@code false} otherwise
   */
  public boolean isMessageID () {
    return isMessageID (_value);
  }

  /**
   * Extracts the facility code.
   * 
   * @param value
   *          {@code HRESULT} to test
   * @return the facility code
   */
  public static int getFacility (final int value) {
    return (value & FACILITY_MASK) >> FACILITY_SHIFT;
  }

  /**
   * Extracts the facility code.
   * 
   * @return the facility code
   */
  public int getFacility () {
    return getFacility (_value);
  }

  /**
   * Extracts the error/success code.
   * 
   * @param value
   *          {@code HRESULT} to test
   * @return the code
   */
  public static int getCode (final int value) {
    return value & CODE_MASK;
  }

  /**
   * Extracts the error/success code.
   * 
   * @return the code
   */
  public int getCode () {
    return getCode (_value);
  }

  /**
   * Creates a string form for better diagnostic logging. This can sometimes be more useful than the
   * hex encoding.
   * 
   * @param value
   *          {@code HRESULT} to process
   * @return a string representation, never {@code null}
   */
  public static String toString (final int value) {
    final StringBuilder sb = new StringBuilder ();
    sb.append (getS (value) ? 'F' : 'S');
    if (getR (value)) sb.append ('R');
    if (getC (value)) sb.append ('C');
    if (getN (value)) sb.append ('N');
    if (getX (value)) sb.append ('X');
    sb.append ('-');
    sb.append (getFacility (value));
    sb.append ('-');
    sb.append (getCode (value));
    return sb.toString ();
  }

  // Object

  @Override
  public int hashCode () {
    return _value;
  }

  @Override
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof HRESULT)) return false;
    return ((HRESULT)o)._value == _value;
  }

  @Override
  public String toString () {
    return toString (_value);
  }

}