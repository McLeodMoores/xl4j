/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode;

import java.util.ArrayList;
import java.util.List;

import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.api.values.XLArray;
import com.mcleodmoores.xl4j.v1.api.values.XLMissing;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;

/**
 * Contains functions that are of general use in Excel.
 */
public final class ExcelUtils {

  /**
   * Creates an array by adding all arguments. Any inputs that are arrays will be flattened in row / column order.
   * @param inputs
   *          the inputs
   * @return the inputs as an array
   */
  @XLFunction(
      name = "Array",
      category = "Utils",
      description = "Create an array from a list of inputs")
  public static XLValue[] array(
      @XLParameter(name = "inputs", description = "The inputs to put into an array") final XLValue... inputs) {
    final List<Object> arrayList = new ArrayList<>();
    for (final XLValue value : inputs) {
      if (value instanceof XLArray) {
        final XLValue[][] xlArray = ((XLArray) value).getArray();
        for (final XLValue[] row : xlArray) {
          for (final XLValue entry : row) {
            arrayList.add(entry);
          }
        }
      } else {
        arrayList.add(value);
      }
    }
    return arrayList.toArray(new XLValue[arrayList.size()]);
  }

  /**
   * Provides null-like functionality. Useful for inputs to methods that can take arrays containing null
   * values, for example.
   * @return XLMissing
   */
  @XLFunction(
      name = "Null",
      category = "Utils",
      description = "Create a null value")
  public static XLValue excelNull() {
    return XLMissing.INSTANCE;
  }

  /**
   * Prepends the prefix to any strings found in the inputs, or leaves them unchanged. If the input is
   * an array, each element of the array that is a string is altered.
   * @param prefix
   *          the prefix
   * @param inputs
   *          the inputs
   * @return the inputs with any strings prepended with the value
   */
  @XLFunction(
      name = "Prefix",
      category = "Utils",
      description = "Append a prefix to a range of values if they are strings, or leaves them unchanged otherwise")
  public static XLValue[] prefix(
      @XLParameter(name = "prefix", description = "The prefix") final String prefix,
      @XLParameter(name = "inputs", description = "The inputs") final XLValue... inputs) {
    final List<Object> arrayList = new ArrayList<>();
    for (final XLValue value : inputs) {
      if (value instanceof XLArray) {
        final XLValue[][] xlArray = ((XLArray) value).getArray();
        final XLValue[][] resultArray = new XLValue[xlArray.length][xlArray[0].length];
        for (int i = 0; i < xlArray.length; i++) {
          for (int j = 0; j < xlArray[i].length; j++) {
            final XLValue entry = xlArray[i][j];
            resultArray[i][j] = entry instanceof XLString ? XLString.of(prefix + ((XLString) entry).getValue()) : entry;
          }
          arrayList.add(resultArray);
        }
      } else {
        arrayList.add(value instanceof XLString ? XLString.of(prefix + ((XLString) value).getValue()) : value);
      }
    }
    return arrayList.toArray(new XLValue[arrayList.size()]);
  }

  /**
   * Removes whitespace from any strings found in the inputs, or leaves them unchanged. If the input is
   * an array, each element of the array that is a string is altered.
   * @param inputs
   *          the inputs
   * @return the inputs with whitespace removed from any strings
   */
  @XLFunction(
      name = "RemoveWhitespace",
      category = "Utils",
      description = "Remove any whitespace in a string, or leaves the value unchanged otherwise")
  public static XLValue[] removeWhitespace(
      @XLParameter(name = "inputs", description = "The inputs") final XLValue... inputs) {
    final List<Object> arrayList = new ArrayList<>();
    for (final XLValue value : inputs) {
      if (value instanceof XLArray) {
        final XLValue[][] xlArray = ((XLArray) value).getArray();
        final XLValue[][] resultArray = new XLValue[xlArray.length][xlArray[0].length];
        for (int i = 0; i < xlArray.length; i++) {
          for (int j = 0; j < xlArray[i].length; j++) {
            final XLValue entry = xlArray[i][j];
            resultArray[i][j] = entry instanceof XLString ? XLString.of(((XLString) value).getValue().replaceAll("\\s", "")) : entry;
          }
          arrayList.add(resultArray);
        }
      } else {
        arrayList.add(value instanceof XLString ? XLString.of(((XLString) value).getValue().replaceAll("\\s", "")) : value);
      }
    }
    return arrayList.toArray(new XLValue[arrayList.size()]);
  }

  private ExcelUtils() {
  }
}
