package com.mcleodmoores.xl4j;

import java.security.SecureRandom;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.util.HexUtils;
import com.mcleodmoores.xl4j.util.SerializationUtils;

/**
 * Unit tests for SerializationUtils.
 * @author jim
 */
public class SerializationUtilsTests {

  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testSerializeNull() {
    SerializationUtils.serialize(null);
  }

  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testDeserializeNull() {
    SerializationUtils.deserialize(null);
  }

  @Test(expectedExceptions = Excel4JRuntimeException.class, expectedExceptionsMessageRegExp = "Could not deserialize data")
  public void testDeserializeJunk() {
    final byte[] junk = new byte[1024];
    new SecureRandom().nextBytes(junk); // don't want to use Java 8 API
    SerializationUtils.deserialize(junk);
  }

  @Test(expectedExceptions = Excel4JRuntimeException.class, expectedExceptionsMessageRegExp = "Couldn't deserialize data: Class not found")
  public void testDeserializeBadClassName() {
    final byte[] data = SerializationUtils.serialize(LocalDate.of(2014, 4, 4));
    System.out.println(HexUtils.bytesToMultiLineDump(data, 16));
    data[8] = 0x62; // corrupt the class file so the package becomes brg.threeten.bp so the class can't be found.
    System.out.println(HexUtils.bytesToMultiLineDump(data, 16));
    SerializationUtils.deserialize(data);
  }
}
