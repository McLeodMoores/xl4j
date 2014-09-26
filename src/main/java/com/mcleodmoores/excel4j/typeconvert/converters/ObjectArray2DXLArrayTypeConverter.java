package com.mcleodmoores.excel4j.typeconvert.converters;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.util.Excel4JReflectionUtils;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLArray;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from Doubles to Excel Numbers and back again.
 */
public final class ObjectArray2DXLArrayTypeConverter extends AbstractTypeConverter {
  
  private final TypeConverterRegistry _typeConverterRegistry;

  /**
   * Default constructor.
   * @param excel  the excel context object, used to access the type converter registry.
   */
  public ObjectArray2DXLArrayTypeConverter(final Excel excel) {
    super(Object[][].class, XLArray.class);
    _typeConverterRegistry = excel.getTypeConverterRegistry();
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    
    Type componentType = null;
    if (expectedType instanceof Class) {
      Class<?> expectedClass = (Class<?>) expectedType;
      componentType = expectedClass.getComponentType().getComponentType(); // as it's 2D
    } else if (expectedType instanceof GenericArrayType) {
      GenericArrayType genericArrayType = (GenericArrayType) expectedType;
      componentType = genericArrayType.getGenericComponentType(); // yes it's odd that you don't need to do it twice, see ScratchTests.java
    } else {
      throw new Excel4JRuntimeException("expectedType not array or GenericArrayType");
    }
    TypeConverter converter = _typeConverterRegistry.findConverter(componentType);
    Object[][] fromArr = (Object[][]) from;
    XLValue[][] toArr = new XLValue[fromArr.length][fromArr.length > 0 ? fromArr[0].length : 0];
    for (int i = 0; i < fromArr.length; i++) {
      for (int j = 0; j < fromArr[0].length; j++) {
        XLValue value = (XLValue) converter.toXLValue(componentType, fromArr[i][j]);
        toArr[i][j] = value;
      }
    }
    return XLArray.of(toArr);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    XLArray xlArr = (XLArray) from;
    Type componentType = null;
    if (expectedType instanceof Class) {
      Class<?> expectedClass = (Class<?>) expectedType;
      componentType = expectedClass.getComponentType();
    } else if (expectedType instanceof GenericArrayType) {
      GenericArrayType genericArrayType = (GenericArrayType) expectedType;
      componentType = genericArrayType.getGenericComponentType();
    } else {
      throw new Excel4JRuntimeException("expectedType not array or GenericArrayType");
    }
    XLValue[][] arr = xlArr.getArray();
    Object[][] targetArr = (Object[][]) Array.newInstance(Excel4JReflectionUtils.reduceToClass(componentType), arr.length, arr.length > 0 ? arr[0].length : 0);
    TypeConverter lastConverter = null;
    Class<?> lastClass = null;
    for (int i = 0; i < arr.length; i++) {
      for (int j = 0; j < arr[i].length; j++) {
        XLValue val = arr[i][j];
        // This is a rather weak attempt at optimizing converter lookup - other options seemed to have greater overhead.
        if (!val.getClass().equals(lastClass)) {
          lastClass = val.getClass();
          lastConverter = _typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(lastClass, componentType));
        }
        targetArr[i][j] = lastConverter.toJavaObject(componentType, val); 
      }
    }
    return targetArr;
  }
}
