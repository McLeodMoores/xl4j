package com.mcleodmoores.excel4j;

import java.security.SecureRandom;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
//import com.mcleodmoores.excel4j.util.HexUtils;
import com.mcleodmoores.excel4j.util.SerializationUtils;

/**
 * Unit tests for SerializationUtils.
 * @author jim
 */
public class SerializationUtilsTests {
	// CHECKSTYLE:OFF
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
  	byte[] junk = new byte[1024];
  	new SecureRandom().nextBytes(junk); // don't want to use Java 8 API
  	SerializationUtils.deserialize(junk);
  }
  
  @Test(expectedExceptions = Excel4JRuntimeException.class, expectedExceptionsMessageRegExp = "Couldn't deserialize data: Class not found")
  public void testDeserializeBadClassName() {
  	byte[] data = SerializationUtils.serialize(LocalDate.of(2014, 4, 4));
    // System.out.println(HexUtils.bytesToMultiLineDump(data, 16));
    data[8] = 0x62; // corrupt the class file so the package becomes brg.threeten.bp so the class can't be found.
    // System.out.println(HexUtils.bytesToMultiLineDump(data, 16));
  	SerializationUtils.deserialize(data);
  }
}
