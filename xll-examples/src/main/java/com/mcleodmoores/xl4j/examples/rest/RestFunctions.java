package com.mcleodmoores.xl4j.examples.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.TypeConversionMode;
import com.mcleodmoores.xl4j.XLFunction;
import com.mcleodmoores.xl4j.XLNamespace;
import com.mcleodmoores.xl4j.XLParameter;
import com.mcleodmoores.xl4j.util.ArrayUtils;
import com.mcleodmoores.xl4j.util.ArrayUtils.FixedDimension;
import com.mcleodmoores.xl4j.util.ArrayWrongSizeException;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;

@XLNamespace("Rest")
public class RestFunctions {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestFunctions.class);
  
  @XLFunction(name = "URI",  category = "REST")
  public static WebTarget uri(String uri) {
    Client client = ClientBuilder.newClient();
    return client.target(uri);
  }
  
  @XLFunction(name = "Params",  category = "REST")
  public static WebTarget params(WebTarget target, Object[][] params) {
    if (params != null) {
        try {
          Object[][] normalisedParams = ArrayUtils.transposeIfNeeded(params, 2, FixedDimension.COLUMNS);
          for (int i = 0; i < normalisedParams.length; i++) {
            if (normalisedParams[i][0] instanceof String) {
              target = target.queryParam((String)normalisedParams[i][0], normalisedParams[i][1]);
            } else { 
              LOGGER.warn("query param name " + i + " was not a string, skipping");
            }
          }
          return target;
        } catch (ArrayWrongSizeException e) {
          LOGGER.error("Params need to be passed as a (2 x n) or (n x 2) array.", e);
          throw new Excel4JRuntimeException("Params must be 2 x n or n x 2", e);
        }
    }
    return target;
  }
  
  @XLFunction(name = "Get", category = "REST", isAutoAsynchronous = true)
  public static Response get(WebTarget target, @XLParameter(optional = true) Object[][] responseTypes) {
    List<String> listResponseTypes = new ArrayList<>();
    if (responseTypes != null) {
      try {
        Object[][] normalisedResponseTypes = ArrayUtils.transposeIfNeeded(responseTypes, 1, FixedDimension.ROWS);
        for (int i = 0; i < normalisedResponseTypes[0].length; i++) {
          Object responseType = normalisedResponseTypes[0][i];
          if (responseType instanceof String) {
            listResponseTypes.add((String) responseType);
          }
        }
      } catch (ArrayWrongSizeException e) {
        LOGGER.error("ReponseTypes needs to be a one dimensional range");
      }
    }
    Builder request = target.request(listResponseTypes.toArray(new String[] {}));
    return request.get();
  }

  @XLFunction(name = "Post", category = "REST", isAutoAsynchronous = true)
  public static Response post(
      @XLParameter(name = "target") WebTarget target, 
      @XLParameter(name = "entity") Entity<?> entity, 
      @XLParameter(name = "responseTypes", optional = true) Object[][] responseTypes) {
    List<String> listResponseTypes = new ArrayList<>();
    if (responseTypes != null) {
      try {
        Object[][] normalisedResponseTypes = ArrayUtils.transposeIfNeeded(responseTypes, 1, FixedDimension.ROWS);
        for (int i = 0; i < normalisedResponseTypes[0].length; i++) {
          Object responseType = normalisedResponseTypes[0][i];
          if (responseType instanceof String) {
            listResponseTypes.add((String) responseType);
          }
        }
      } catch (ArrayWrongSizeException e) {
        LOGGER.error("ReponseTypes needs to be a one dimensional range");
      }
    }
    Builder request = target.request(listResponseTypes.toArray(new String[] {}));
    return request.post(entity);
  }
  
  @XLFunction(name = "ReponseStatus", category = "REST")
  public static int reponseStatus(@XLParameter(name = "reponse") Response response) {
    return response.getStatus();
  }
  
  @XLFunction(name = "ReponseLength", category = "REST")
  public static int reponseLength(@XLParameter(name = "reponse") Response response) {
    return response.getLength();
  }

  @XLFunction(name = "ReponseHeaders", category = "REST")
  public static Object[][] reponseHeaders(@XLParameter(name = "reponse") Response response) {
    MultivaluedMap<String, Object> headers = response.getHeaders();
    int maxWidth = 0;
    Set<Entry<String, List<Object>>> entrySet = headers.entrySet();
    for (Entry<String, List<Object>> entry : entrySet) {
      maxWidth = Math.max(entry.getValue() != null ? entry.getValue().size() : 0, maxWidth);
    }
    Object[][] headersArr = new Object[headers.size()][maxWidth];
    int i = 0;
    for (Entry<String, List<Object>> entry : entrySet) {
      headersArr[i][0] = entry.getKey();
      int j = 1;
      for (Object value : entry.getValue()) {
        headersArr[i][j++] = value;
      }
    }
    return headersArr;
  }
  
  @XLFunction(name = "ReponseEntity", category = "REST")//, typeConversionMode = TypeConversionMode.OBJECT_RESULT)
  public static Object reponseString(@XLParameter(name = "reponse") Response response) {
    return response.readEntity(String.class);
  }
  
  @XLFunction(name = "ReponseJSON", category = "REST")//, typeConversionMode = TypeConversionMode.OBJECT_RESULT)
  public static Object reponseJSON(@XLParameter(name = "reponse") Response response) {
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      InputStream inputStream = response.readEntity(InputStream.class);
      // should we be buffering this?
      JSONTokener tokeniser = new JSONTokener(new InputStreamReader(inputStream));
      try {
        Object object = tokeniser.nextValue();
        response.close();
        inputStream.close();
        return object;
      } catch (JSONException jsone) {
        response.close();
        try {
           inputStream.close();
        } catch (IOException ioe) {
        }
        throw new Excel4JRuntimeException("Problem parsing JSON reply", jsone);
      } catch (IOException ex) {
        response.close();
        throw new Excel4JRuntimeException("Problem closing input stream");
      }
    } else {
      String msg = "Response code to " + response.getLocation() + " was " + response.getStatusInfo();
      response.close();
      throw new Excel4JRuntimeException(msg);
    }
  }
}
