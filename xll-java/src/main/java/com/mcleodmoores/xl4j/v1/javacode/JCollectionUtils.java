/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode;

import java.util.LinkedHashMap;

import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;

/**
 *
 */
@XLNamespace("J")
public class JCollectionUtils {

  @XLFunction(
      name = "LinkedHashMap",
      description = "Populate a hashmap from key-value pairs",
      category = "Java")
  public static LinkedHashMap<Object, Object> linkedHashMap(
      @XLParameter(name = "Data") final XLValue... values) {
    return null;
  }
}
