package com.mcleodmoores.excel4j.typeconvert.converters;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.Excel;
import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.typeconvert.TypeConverter;
import com.mcleodmoores.excel4j.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLArray;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from Doubles to Excel Numbers and back again.
 */
public final class PrimitiveIntegerArrayXLArrayTypeConverter extends AbstractTypeConverter {
  
  private final TypeConverterRegistry _typeConverterRegistry;

  /**
   * Default constructor.
   * @param excel  the excel context object, used to access the type converter registry.
   */
  public PrimitiveIntegerArrayXLArrayTypeConverter(final Excel excel) {
    super(int[].class, XLArray.class);
    _typeConverterRegistry = excel.getTypeConverterRegistry();
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (!from.getClass().isArray()) {
      throw new Excel4JRuntimeException("from parameter must be an array");
    }
    Type componentType = null;
    if (expectedType instanceof Class) {
      Class<?> expectedClass = (Class<?>) from.getClass();
      componentType = expectedClass.getComponentType();
      if (componentType == null) {
        throw new Excel4JRuntimeException("component type of from parameter is null");
      }
    } else {
      throw new Excel4JRuntimeException("expectedType not array or GenericArrayType");
    }
    System.err.println("ComponentType = " + componentType);
    TypeConverter converter = _typeConverterRegistry.findConverter(componentType);
    int[] fromArr = (int[]) from;
    XLValue[][] toArr = new XLValue[1][fromArr.length];
    for (int i = 0; i < fromArr.length; i++) {
      XLValue value = (XLValue) converter.toXLValue(componentType, fromArr[i]);
      toArr[0][i] = value;
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
    TypeConverter lastConverter = null;
    Class<?> lastClass = null;
    if (arr.length == 1) { // array is a single row
      int[] targetArr = new int[arr[0].length];
      for (int i = 0; i < arr[0].length; i++) {
        XLValue val = arr[0][i];
        // This is a rather weak attempt at optimizing converter lookup - other options seemed to have greater overhead.
        if (!val.getClass().equals(lastClass)) {
          lastClass = val.getClass();
          lastConverter = _typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(lastClass, componentType));
        }
        targetArr[i] = (int) lastConverter.toJavaObject(componentType, val); 
      }
      return targetArr;
    } else { // array is single column
      int[] targetArr = new int[arr.length];
      for (int i = 0; i < arr.length; i++) {
        XLValue val = arr[i][0];
        // This is a rather weak attempt at optimizing converter lookup - other options seemed to have greater overhead.
        if (!val.getClass().equals(lastClass)) {
          lastClass = val.getClass();
          lastConverter = _typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(lastClass, componentType));
        }
        targetArr[i] = (int) lastConverter.toJavaObject(componentType, val); 
      }
      return targetArr;
    }
    
  }
}
