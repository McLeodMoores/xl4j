/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.rest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;

/**
 * Excel functions that extract information from <code>JSONObject</code>s.
 */
public final class JsonFunctions {
  private static final Logger LOGGER = LoggerFactory.getLogger(JsonFunctions.class);

  /**
   * Gets the names of the fields.
   *
   * @param jsonObject
   *          the object
   * @return
   *          the fields as array
   */
  @XLFunction(name = "JSONObject.Names", category = "JSON")
  public static String[] names(@XLParameter(name = "jsonObject", description = "a JSONObject") final JSONObject jsonObject) {
    return JSONObject.getNames(jsonObject);
  }

  /**
   * Gets the value associated with the field.
   *
   * @param jsonObject
   *          the object
   * @param fieldName
   *          the field name
   * @return
   *          the object or null if there is no value for this field
   */
  @XLFunction(name = "JSONObject.Get", category = "JSON")
  public static Object get(@XLParameter(name = "jsonObject", description = "a JSONObject") final JSONObject jsonObject,
      @XLParameter(name = "fieldName", description = "name of field to get from object") final String fieldName) {
    return jsonObject.opt(fieldName);
  }

  /**
   * Returns true if there is a value for this field in the object.
   *
   * @param jsonObject
   *          the object
   * @param fieldName
   *          the field name
   * @return
   *          true if there is a value for this field
   */
  @XLFunction(name = "JSONObject.Has", category = "JSON")
  public static boolean has(@XLParameter(name = "jsonObject", description = "a JSONObject") final JSONObject jsonObject,
      @XLParameter(name = "fieldName", description = "name of field to check") final String fieldName) {
    return jsonObject.has(fieldName);
  }

  /**
   * Expands a JSON object into a (number of fields x 2) array containing the key and value in each row.
   *
   * @param jsonObject
   *          the object
   * @return
   *          the values as an array
   */
  @XLFunction(name = "JSONObject.Expand", category = "JSON")
  public static Object[][] expand(@XLParameter(name = "jsonObject", description = "a JSONObject") final JSONObject jsonObject) {
    final Object[][] result = new Object[jsonObject.length()][2];
    final String[] names = JSONObject.getNames(jsonObject);
    for (int i = 0; i < names.length; i++) {
      result[i][0] = names[i];
      result[i][1] = jsonObject.opt(names[i]);
    }
    return result;
  }

  /**
   * Gets the length of the array.
   *
   * @param jsonArray
   *          the array
   * @return
   *          the length
   */
  @XLFunction(name = "JSONArray.Length", category = "JSON")
  public static int length(@XLParameter(name = "jsonArray", description = "a JSONArray") final JSONArray jsonArray) {
    return jsonArray.length();
  }

  /**
   * Gets the value at the index.
   *
   * @param jsonArray
   *          the array
   * @param index
   *          the index
   * @return
   *          the value
   */
  @XLFunction(name = "JSONArray.Get", category = "JSON")
  public static Object get(@XLParameter(name = "jsonArray", description = "a JSONArray") final JSONArray jsonArray,
      @XLParameter(name = "index", description = "index of element to retrieve") final int index) {
    return jsonArray.opt(index);
  }

  /**
   * Expands a JSON array into an array.
   *
   * @param jsonArray
   *          the array
   * @return
   *          the values as an array
   */
  @XLFunction(name = "JSONArray.Expand", category = "JSON")
  public static Object[] get(@XLParameter(name = "jsonArray", description = "a JSONArray") final JSONArray jsonArray) {
    final Object[] result = new Object[jsonArray.length()];
    LOGGER.info("Length = " + jsonArray.length());
    for (int i = 0; i < jsonArray.length(); i++) {
      result[i] = jsonArray.opt(i);
      LOGGER.info("[" + i + "] = " + jsonArray.opt(i));
    }
    return result;
  }

  private JsonFunctions() {
  }
}
