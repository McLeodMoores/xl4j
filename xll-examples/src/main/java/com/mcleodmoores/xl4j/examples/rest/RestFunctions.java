/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
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
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.util.ArrayUtils;
import com.mcleodmoores.xl4j.v1.util.ArrayUtils.FixedDimension;
import com.mcleodmoores.xl4j.v1.util.ArrayWrongSizeException;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;

/**
 * Excel functions that create REST requests and responses.
 */
@XLNamespace("Rest")
public final class RestFunctions {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestFunctions.class);

  /**
   * Creates a web target.
   *
   * @param uri
   *          the URI
   * @return
   *          the web target
   */
  @XLFunction(name = "URI", category = "REST")
  public static WebTarget uri(final String uri) {
    final Client client = ClientBuilder.newClient();
    return client.target(uri);
  }

  /**
   * Creates a new target by configuring a query parameter on the URI of the input instance.
   *
   * @param target
   *          the target
   * @param params
   *          the parameters as a (n x 2) or (2 x n) array
   * @return
   *          the target
   */
  @XLFunction(name = "Params", category = "REST")
  public static WebTarget params(final WebTarget target, final Object[][] params) {
    WebTarget result = target;
    if (params != null) {
      try {
        final Object[][] normalisedParams = ArrayUtils.transposeIfNeeded(params, 2, FixedDimension.COLUMNS);
        for (int i = 0; i < normalisedParams.length; i++) {
          if (normalisedParams[i][0] instanceof String) {
            result = target.queryParam((String) normalisedParams[i][0], normalisedParams[i][1]);
          } else {
            LOGGER.warn("query param name " + i + " was not a string, skipping");
          }
        }
        return result;
      } catch (final ArrayWrongSizeException e) {
        LOGGER.error("Params need to be passed as a (2 x n) or (n x 2) array.", e);
        throw new XL4JRuntimeException("Params must be 2 x n or n x 2", e);
      }
    }
    return result;
  }

  /**
   * Invokes HTTP GET for the request.
   *
   * @param target
   *          the target
   * @param responseTypes
   *          the response types as a (n x 1) or (1 x n) array
   * @return
   *          the response
   */
  @XLFunction(name = "Get", category = "REST", isAutoRTDAsynchronous = true, isMultiThreadSafe = false)
  public static Response get(final WebTarget target, @XLParameter(optional = true) final Object[][] responseTypes) {
    final List<String> listResponseTypes = new ArrayList<>();
    if (responseTypes != null) {
      try {
        final Object[][] normalisedResponseTypes = ArrayUtils.transposeIfNeeded(responseTypes, 1, FixedDimension.ROWS);
        for (int i = 0; i < normalisedResponseTypes[0].length; i++) {
          final Object responseType = normalisedResponseTypes[0][i];
          if (responseType instanceof String) {
            listResponseTypes.add((String) responseType);
          }
        }
      } catch (final ArrayWrongSizeException e) {
        LOGGER.error("ReponseTypes needs to be a one dimensional range");
      }
    }
    final Builder request = target.request(listResponseTypes.toArray(new String[] {}));
    return request.get();
  }

  /**
   * Invokes HTTP POST for the request.
   *
   * @param target
   *          the target
   * @param entity
   *          the message entity
   * @param responseTypes
   *          the response types as a (n x 1) or (1 x n) array
   * @return
   *          the response
   */
  @XLFunction(name = "Post", category = "REST", isAutoRTDAsynchronous = true, isMultiThreadSafe = false)
  public static Response post(
      @XLParameter(name = "target") final WebTarget target,
      @XLParameter(name = "entity") final Entity<?> entity,
      @XLParameter(name = "responseTypes", optional = true) final Object[][] responseTypes) {
    final List<String> listResponseTypes = new ArrayList<>();
    if (responseTypes != null) {
      try {
        final Object[][] normalisedResponseTypes = ArrayUtils.transposeIfNeeded(responseTypes, 1, FixedDimension.ROWS);
        for (int i = 0; i < normalisedResponseTypes[0].length; i++) {
          final Object responseType = normalisedResponseTypes[0][i];
          if (responseType instanceof String) {
            listResponseTypes.add((String) responseType);
          }
        }
      } catch (final ArrayWrongSizeException e) {
        LOGGER.error("ReponseTypes needs to be a one dimensional range");
      }
    }
    final Builder request = target.request(listResponseTypes.toArray(new String[] {}));
    return request.post(entity);
  }

  /**
   * Gets the status code of a response.
   *
   * @param response
   *        the response
   * @return
   *        the status code
   */
  @XLFunction(name = "ResponseStatus", category = "REST")
  public static int responseStatus(@XLParameter(name = "response") final Response response) {
    return response.getStatus();
  }

  /**
   * Gets the length of a response.
   *
   * @param response
   *          the response
   * @return
   *          the length of the response
   */
  @XLFunction(name = "ResponseLength", category = "REST")
  public static int responseLength(@XLParameter(name = "response") final Response response) {
    return response.getLength();
  }

  /**
   * Gets the response headers as an array.
   *
   * @param response
   *          the response
   * @return
   *          the headers as a (n headers x max width) array
   */
  @XLFunction(name = "ResponseHeaders", category = "REST")
  public static Object[][] responseHeaders(@XLParameter(name = "response") final Response response) {
    final MultivaluedMap<String, Object> headers = response.getHeaders();
    int maxWidth = 0;
    final Set<Entry<String, List<Object>>> entrySet = headers.entrySet();
    for (final Entry<String, List<Object>> entry : entrySet) {
      maxWidth = Math.max(entry.getValue() == null ? 0 : entry.getValue().size(), maxWidth);
    }
    final Object[][] headersArr = new Object[headers.size()][maxWidth];
    final int i = 0;
    for (final Entry<String, List<Object>> entry : entrySet) {
      headersArr[i][0] = entry.getKey();
      int j = 1;
      for (final Object value : entry.getValue()) {
        headersArr[i][j++] = value;
      }
    }
    return headersArr;
  }

  /**
   * Gets the response entity as a <code>String</code>.
   * @param response
   *          the response
   * @return
   *          the entity as a string
   */
  @XLFunction(name = "ResponseEntity", category = "REST")
  public static Object reponseString(@XLParameter(name = "response") final Response response) {
    return response.readEntity(String.class);
  }

  /**
   * If the response status is <code>OK</code>, returns the response as a JSON object.
   * @param response
   *          the response
   * @return
   *          a JSON object
   */
  @XLFunction(name = "ResponseJSON", category = "REST")
  public static Object responseJSON(@XLParameter(name = "response") final Response response) {
    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      try (InputStream inputStream = response.readEntity(InputStream.class)) {
        // should we be buffering this?
        final JSONTokener tokeniser = new JSONTokener(new InputStreamReader(inputStream));
        try {
          final Object object = tokeniser.nextValue();
          response.close();
          inputStream.close();
          return object;
        } catch (final JSONException jsone) {
          response.close();
          try {
            inputStream.close();
          } catch (final IOException ioe) {
          }
          throw new XL4JRuntimeException("Problem parsing JSON reply", jsone);
        } catch (final IOException ex) {
          response.close();
          throw new XL4JRuntimeException("Problem closing input stream");
        }
      } catch (final IOException e) {
        throw new XL4JRuntimeException("", e);
      }
    }
    final String msg = "Response code to " + response.getLocation() + " was " + response.getStatusInfo();
    response.close();
    throw new XL4JRuntimeException(msg);
  }

  private RestFunctions() {
  }
}
