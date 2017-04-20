package com.mcleodmoores.xl4j.examples.rest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;

public class JsonFunctions {
  private static final Logger LOGGER = LoggerFactory.getLogger(JsonFunctions.class);
  
  @XLFunction(name = "JSONObject.Names", category = "JSON")
  public static String[] names(@XLParameter(name = "jsonObject", description = "a JSONObject") JSONObject jsonObject) {
    return JSONObject.getNames(jsonObject);
  }
  
  @XLFunction(name = "JSONObject.Get", category = "JSON")
  public static Object get(@XLParameter(name = "jsonObject", description = "a JSONObject") JSONObject jsonObject, 
                           @XLParameter(name = "fieldName", description = "name of field to get from object") String fieldName) {
    return jsonObject.opt(fieldName);
  }
  
  @XLFunction(name = "JSONObject.Has", category = "JSON")
  public static boolean has(@XLParameter(name = "jsonObject", description = "a JSONObject") JSONObject jsonObject,
                            @XLParameter(name = "fieldName", description = "name of field to check") String fieldName) {
    return jsonObject.has(fieldName);
  }
  
  @XLFunction(name = "JSONObject.Expand", category = "JSON")
  public static Object[][] expand(@XLParameter(name = "jsonObject", description = "a JSONObject") JSONObject jsonObject) {
    Object[][] result = new Object[jsonObject.length()][2];
    String[] names = JSONObject.getNames(jsonObject);
    for (int i = 0; i < names.length; i++) {
      result[i][0] = names[i];
      result[i][1] = jsonObject.opt(names[i]);
    }
    return result;
  }
  
  @XLFunction(name = "JSONArray.Length", category = "JSON")
  public static int length(@XLParameter(name = "jsonArray", description = "a JSONArray") JSONArray jsonArray) {
    return jsonArray.length();
  }
  
  @XLFunction(name = "JSONArray.Get", category = "JSON")
  public static Object get(@XLParameter(name = "jsonArray", description = "a JSONArray") JSONArray jsonArray, 
                           @XLParameter(name = "index", description = "index of element to retrieve") int index) {
    return jsonArray.opt(index);
  }
  
  @XLFunction(name = "JSONArray.Expand", category = "JSON")
  public static Object[] get(@XLParameter(name = "jsonArray", description = "a JSONArray") JSONArray jsonArray) {
    Object[] result = new Object[jsonArray.length()];
    LOGGER.info("Length = " + jsonArray.length());
    for (int i = 0; i < jsonArray.length(); i++) {
      result[i] = jsonArray.opt(i);
      LOGGER.info("[" + i + "] = " + jsonArray.opt(i));
    }
    return result;
  }
}
