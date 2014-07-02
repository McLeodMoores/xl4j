package com.mcleodmoores.excel4j.util;

/**
 * A runtime exception from the Excel4J Java layer.
 */
public class Excel4JRuntimeException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * Constructor when another exception is being included.
   * @param message a message describing the exception, not null
   * @param cause the cause of the expection if there is one, not null
   */
  public Excel4JRuntimeException(final String message, final Throwable cause) {
    super(message, cause);
  }
  
  /**
   * Constructor when exception is not caused by an underlying exception.
   * @param message a message describing the exception, not null
     */  
  public Excel4JRuntimeException(final String message) {
    super(message);
  }
}

