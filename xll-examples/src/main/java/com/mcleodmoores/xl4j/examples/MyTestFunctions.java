/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples;

import java.util.ArrayList;
import java.util.List;

import com.mcleodmoores.xl4j.TypeConversionMode;
import com.mcleodmoores.xl4j.XLArgument;
import com.mcleodmoores.xl4j.XLFunction;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLBoolean;
import com.mcleodmoores.xl4j.values.XLError;
import com.mcleodmoores.xl4j.values.XLLocalReference;
import com.mcleodmoores.xl4j.values.XLMultiReference;
import com.mcleodmoores.xl4j.values.XLNumber;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

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
  public static XLString myStringCat(@XLArgument(name = "string 1", description = "The first string") final XLString one,
      @XLArgument(name = "string 2", description = "The second string") final XLString two) {
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
  public static XLBoolean myXOR(@XLArgument(name = "boolean 1", description = "The first boolean") final XLBoolean one,
      @XLArgument(name = "boolean 2", description = "The second boolean") final XLBoolean two) {
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
      @XLArgument(name = "local reference", description = "The local reference (range)") final XLLocalReference ref) {
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
      @XLArgument(name = "multi reference", description = "The multi reference (range)") final XLMultiReference ref) {
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
  public static List<?> makeList(@XLArgument(name = "entries", description = "The values to put in the list (range)") final XLArray arr) {
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
   * Gets an element from a list.
   *
   * @param list
   *          the list, not null
   * @param index
   *          the index
   * @return the element
   */
  @XLFunction(name = "ListElement",
              description = "Get an element from a list",
              category = "Mine",
              typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
  public static XLValue listElem(final List<XLValue> list, final int index) {
    ArgumentChecker.notNull(list, "list");
    if (index >= list.size() || index < 0) {
      return XLError.NA;
    }
    return list.get(index);
  }
}
