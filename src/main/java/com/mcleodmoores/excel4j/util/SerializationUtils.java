package com.mcleodmoores.excel4j.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializationUtils {

  public static byte[] serialize(Serializable object) {
    try (
      ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
      ObjectOutput out = new ObjectOutputStream(bos);
    ) {
      out.writeObject(object);
      return bos.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException(e); // should never happen.
    }
  }
  
  public static Serializable deserialize(byte[] data) {
    try (
      ByteArrayInputStream bis = new ByteArrayInputStream(data);
      ObjectInput in = new ObjectInputStream(bis);
    ) {
      return (Serializable) in.readObject();
    } catch (IOException e) {
      throw new RuntimeException(e); // should never happen.
    } catch (ClassNotFoundException e) { // should we expose this?
      throw new RuntimeException(e);
    }
  }

}
