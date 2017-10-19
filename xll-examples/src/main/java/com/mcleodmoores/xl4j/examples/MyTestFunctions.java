/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples;

import java.util.ArrayList;
import java.util.List;

import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLBoolean;
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLLocalReference;
import com.mcleodmoores.xl4j.v1.api.values.XLMultiReference;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;

/**
 * Functions used in testing.
 */
public final class MyTestFunctions {

  /**
   * Restricted constructor.
   */
  private MyTestFunctions() {
  }

  /**
   * Tests asynchronous functions by sleeping for a given amount of time.
   * @param delay
   *          the delay in seconds
   * @return
   *          the delay or -1 if there is an interrupt
   */
  @XLFunction(name = "AsyncWait",
      description = "Wait for n seconds",
      category = "Mine",
      isAutoRTDAsynchronous = true,
      isMultiThreadSafe = false)
  public static XLNumber myAsyncWait(@XLParameter(name = "delay", description = "delay in seconds") final XLNumber delay) {
    try {
      Thread.sleep((int) delay.getAsDouble() * 1000);
    } catch (final InterruptedException ie) {
      return XLNumber.of(-1);
    }
    return delay;
  }

  /**
   * String concatenation test.
   *
   * @param one
   *          the first string, not null
   * @param two
   *          the second string, not null
   * @return a string
   */
  @XLFunction(name = "MyStringCat",
      description = "Concat 2 strings",
      category = "Mine",
      typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
  public static XLString myStringCat(@XLParameter(name = "string 1", description = "The first string") final XLString one,
      @XLParameter(name = "string 2", description = "The second string") final XLString two) {
    ArgumentChecker.notNull(one, "one");
    ArgumentChecker.notNull(two, "two");
    return XLString.of("Hello" + one.getValue() + two.getValue());
  }

  /**
   * XOR test.
   *
   * @param one
   *          the first value, not null
   * @param two
   *          the second value, not null
   * @return XOR
   */
  @XLFunction(name = "MyXOR",
      description = "XOR 2 booleans",
      category = "Mine",
      typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
  public static XLBoolean myXOR(@XLParameter(name = "boolean 1", description = "The first boolean") final XLBoolean one,
      @XLParameter(name = "boolean 2", description = "The second boolean") final XLBoolean two) {
    ArgumentChecker.notNull(one, "one");
    ArgumentChecker.notNull(two, "two");
    return XLBoolean.from(one.getValue() ^ two.getValue());
  }

  /**
   * Local reference test.
   *
   * @param ref
   *          the reference, not null
   * @return the reference string
   */
  @XLFunction(name = "MyLocalReference",
      description = "Local reference tostring",
      category = "Mine",
      typeConversionMode = TypeConversionMode.SIMPLEST_RESULT,
      isMacroEquivalent = true,
      isMultiThreadSafe = false)
  public static XLString myLocalReference(
      @XLParameter(name = "local reference", description = "The local reference (range)") final XLLocalReference ref) {
    ArgumentChecker.notNull(ref, "ref");
    return XLString.of(ref.toString());
  }

  /**
   * Multi-reference test.
   *
   * @param ref
   *          the reference
   * @return the reference string
   */
  @XLFunction(name = "MyMultiReference",
      description = "Multi reference tostring",
      category = "Mine",
      typeConversionMode = TypeConversionMode.SIMPLEST_RESULT,
      isMacroEquivalent = true,
      isMultiThreadSafe = false)
  public static XLString myMultiReference(
      @XLParameter(name = "multi reference", description = "The multi reference (range)") final XLMultiReference ref) {
    ArgumentChecker.notNull(ref, "ref");
    return XLString.of(ref.toString());
  }

  /**
   * Creates an XLArray.
   *
   * @return the array
   */
  @XLFunction(name = "MyArray",
      description = "Creates an array",
      category = "Mine",
      typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
  public static XLArray myArray() {
    final XLValue[][] arr = { { XLNumber.of(1), XLString.of("Two"), XLNumber.of(3) },
        { XLString.of("One"), XLNumber.of(2), XLString.of("3") } };
    return XLArray.of(arr);
  }

  /**
   * Creates a list from a range.
   *
   * @param arr
   *          the array, not null
   * @return the list
   */
  @XLFunction(name = "MakeList",
      description = "Make a list from a range/array",
      category = "Mine",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT)
  public static List<?> makeList(@XLParameter(name = "entries", description = "The values to put in the list (range)") final XLArray arr) {
    ArgumentChecker.notNull(arr, "arr");
    final ArrayList<XLValue> list = new ArrayList<>();
    final XLValue[][] array = arr.getArray();
    for (final XLValue[] row : array) {
      for (final XLValue element : row) {
        list.add(element);
      }
    }
    return list;
  }

  /**
   * Gets an element from a list. Note the input type - if List were used, the List converter would be used, which
   * is not the behaviour that is being tested.
   *
   * @param object
   *          the list, not null
   * @param index
   *          the index
   * @return the element
   */
  @XLFunction(name = "ListElement",
      description = "Get an element from a list",
      category = "Mine",
      typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
  public static XLValue listElem(final Object object, final int index) {
    ArgumentChecker.notNull(object, "object");
    @SuppressWarnings("unchecked")
    final List<XLValue> list = (List<XLValue>) object;
    if (index >= list.size() || index < 0) {
      return XLError.NA;
    }
    return list.get(index);
  }
}
