/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.values;

import java.io.Serializable;
import java.util.Arrays;

import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.util.ArgumentChecker;
import com.mcleodmoores.xl4j.v1.util.HexUtils;
import com.mcleodmoores.xl4j.v1.util.SerializationUtils;

/**
 * Java representation of the xloper type xltypeBigData.
 */
public final class XLBigData implements XLValue {
  private static final int MAX_BYTES = 32;
  private byte[] _valueToExcel;
  private final Excel _excel;
  private final long _handleFromExcel;
  private final long _length;

  private XLBigData(final byte[] valueToExcel) {
    _valueToExcel = valueToExcel;
    _excel = null;
    _handleFromExcel = 0;
    _length = valueToExcel.length; // length embedded in _valueToExcel array in this case.
  }

  private XLBigData(final Excel excel, final long handleFromExcel, final long length) {
    _valueToExcel = null;
    _excel = excel;
    _handleFromExcel = handleFromExcel;
    _length = length;
  }

  /**
   * Create an instance from a byte array. This is used when you're using a specific manual encoding and we're sending data to Excel rather
   * than receiving it.
   * 
   * @param data
   *          the data as a byte array
   * @return an instance of XLBigData
   */
  public static XLBigData of(final byte[] data) {
    ArgumentChecker.notNull(data, "data");
    return new XLBigData(data);
  }

  /**
   * Create an instance from a Serializable Java object. This is used when you want to save an object (or graph of objects) and have it
   * automatically serialized before sending to Excel rather than receiving data from Excel.
   * 
   * @param object
   *          the object to serialize
   * @return an instance of XLBigData
   */
  public static XLBigData of(final Serializable object) {
    ArgumentChecker.notNull(object, "object");
    return new XLBigData(SerializationUtils.serialize(object));
  }

  /**
   * Create an instance from a Windows HANDLE and length. This is used when passing an object from Excel to Java, but only getting the
   * actual data on demand. This allows more efficient communication between Excel and Java by avoid unnecessary data transfers.
   * 
   * @param excel
   *          the excel callback interface, not null. See ExcelFactory.
   * @param handleFromExcel
   *          a Windows HANDLE data type pointing at the data to retrieve. This reduces to (void *) so should fit in a 64-bit signed long.
   * @param length
   *          the length of the data block
   * @return an instance of XLBigData
   */
  public static XLBigData of(final Excel excel, final long handleFromExcel, final long length) {
    ArgumentChecker.notNull(excel, "excel");
    return new XLBigData(excel, handleFromExcel, length);
  }

  /**
   * Return the raw byte buffer. This will either use the embedded byte array or call back into Windows/Excel to copy the buffer into Java.
   * Once this has been done, further requests use the embedded array, so just bear in mind the first call can be expensive if the data is
   * being passed from the XLL side.
   * 
   * @return the raw byte array buffer
   */
  public byte[] getBuffer() {
    if (_valueToExcel == null) { // if no byte buffer, pull it from XLL using handle.
      _valueToExcel = _excel.getExcelCallback().getBinaryName(_handleFromExcel, _length);
    }
    return _valueToExcel;
  }

  /**
   * Return buffer deserialized into an object. This will either use the embedded byte array as a source for the deserialization or call
   * back into Windows/Excel to copy the buffer into Java. Once this has been done, further requests use the embedded array, so just bear in
   * mind the first call can be expensive if the data is being passed from the XLL side. This may throw a ClassNotFoundException embedded
   * inside an XL4JRuntimeException if the class cannot be found.
   * 
   * @return the deserialized object
   */
  public Serializable getValue() {
    return SerializationUtils.deserialize(getBuffer());
  }

  /**
   * Return the Excel handle for this data.
   * 
   * @return the Excel handle for the binary data area
   */
  public long getHandle() {
    return _handleFromExcel;
  }

  /**
   * @return the length of the data
   */
  public long getLength() {
    return _length;
  }

  @Override
  public <E> E accept(final XLValueVisitor<E> visitor) {
    return visitor.visitXLBigData(this);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(getBuffer());
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof XLBigData)) {
      return false;
    }
    final XLBigData other = (XLBigData) obj;
    if (!Arrays.equals(getBuffer(), other.getBuffer())) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final byte[] buffer = getBuffer();
    final String s = HexUtils.bytesToTruncatedPaddedHex(buffer, MAX_BYTES);
    final String elipses = buffer.length > MAX_BYTES ? "..." : "";
    return "XLBigData[len=" + buffer.length + ", buffer=[" + s + elipses + "]]";
  }
}
