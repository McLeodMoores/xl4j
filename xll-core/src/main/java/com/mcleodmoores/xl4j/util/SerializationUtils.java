package com.mcleodmoores.xl4j.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Static utility methods for serializing and deserializing objects from byte arrays. 
 */
public final class SerializationUtils {
  
  private SerializationUtils() {
  }

  private static final int BYTE_BUFFER_INITIAL_SIZE = 256;

  /**
   * Serialize an object to a byte array.
   * @param object a Serializable object to encode
   * @return a byte array representing the serialized object
   */
  public static byte[] serialize(final Serializable object) {
    ArgumentChecker.notNull(object, "object");
    try (
      ByteArrayOutputStream bos = new ByteArrayOutputStream(BYTE_BUFFER_INITIAL_SIZE);
      ObjectOutput out = new ObjectOutputStream(bos);
    ) {
      out.writeObject(object);
      return bos.toByteArray();
    } catch (IOException e) {
      throw new Excel4JRuntimeException("Could not serialize Serializable", e); // should never happen.
    }
  }
  
  /**
   * Deserialize an object from a byte array.
   * @param data the byte array from which to deserialize the object
   * @return the object, an object that is Serializable
   */
  public static Serializable deserialize(final byte[] data) {
    ArgumentChecker.notNull(data, "data");
    try (
      ByteArrayInputStream bis = new ByteArrayInputStream(data);
      ObjectInput in = new ObjectInputStream(bis);
    ) {
      return (Serializable) in.readObject();
    } catch (IOException e) {
      throw new Excel4JRuntimeException("Could not deserialize data", e);
    } catch (ClassNotFoundException e) { // should we expose this?
      throw new Excel4JRuntimeException("Couldn't deserialize data: Class not found", e);
    }
  }

}
