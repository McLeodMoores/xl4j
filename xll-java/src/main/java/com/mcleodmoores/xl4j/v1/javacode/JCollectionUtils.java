/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.javacode;

import java.util.LinkedHashMap;

import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;

/**
 * Java/Excel collection utilities.
 */
@XLNamespace("J")
public final class JCollectionUtils {
  private JCollectionUtils() { }
  
  //@XLFunction(
  //    name = "LinkedHashMap",
  //    description = "Populate a hashmap from key-value pairs",
  //    category = "Java")
  /**
   * Not complete.
   * @param values  entries for the map
   * @return a linked hash map.
   */
  public static LinkedHashMap<Object, Object> linkedHashMap(
      @XLParameter(name = "Data") final XLValue... values) {
    return null;
  }
}
