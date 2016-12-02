package com.mcleodmoores.xl4j.examples.quandl;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.Excel;
import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Type converter for maps to {@link XLArray}.
 */
public final class Map2XLArrayTypeConverter extends AbstractTypeConverter {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory
      .getLogger(Map2XLArrayTypeConverter.class);
  /** The priority */
  private static final int PRIORITY = 6;
  /** The Excel context */
  private final Excel _excel;;

  /**
   * Default constructor.
   *
   * @param excel
   *          the excel context object, used to access the type converter
   *          registry, not null
   */
  public Map2XLArrayTypeConverter(final Excel excel) {
    super(Map.class, XLArray.class, PRIORITY);
    ArgumentChecker.notNull(excel, "excel");
    _excel = excel;
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (!from.getClass().isAssignableFrom(Map.class)) {
      throw new Excel4JRuntimeException("\"from\" parameter must be a Map");
    }
    final Map<?, ?> fromMap = (Map<?, ?>) from;
    if (fromMap.size() == 0) { // empty array
      return XLArray.of(new XLValue[1][1]);
    }
    Type keyType = null;
    Type valueType = null;
    if (expectedType instanceof Class) {
      final Class<?> expectedClass = from.getClass();
      final TypeVariable<?>[] typeParameters = expectedClass
          .getTypeParameters();
      final Type[] keyUpperBounds = typeParameters[0].getBounds();
      switch (keyUpperBounds.length) {
        case 0:
          keyType = Object.class;
          break;
        case 1:
          keyType = keyUpperBounds[0];
          break;
        default:
          keyType = keyUpperBounds[0];
          LOGGER.warn(
              "Map key type parameter has multiple upper bounds, only considering first in conversion");
          break;
      }
      keyType = keyUpperBounds[0];
      final Type[] valueUpperBounds = typeParameters[1].getBounds();
      switch (valueUpperBounds.length) {
        case 0:
          valueType = Object.class;
          break;
        case 1:
          valueType = valueUpperBounds[0];
          break;
        default:
          valueType = valueUpperBounds[0];
          LOGGER.warn(
              "Map value parameter has multiple upper bounds, only considering first in conversion");
          break;
      }
      valueType = valueUpperBounds[0];
    } else {
      throw new Excel4JRuntimeException("expectedType not a Class");
    }

    // we know the length is > 0
    final XLValue[][] toArr = new XLValue[fromMap.size()][2];
    // get converters for keys and values
    final TypeConverter keyConverter = _excel.getTypeConverterRegistry()
        .findConverter(keyType);
    final TypeConverter valueConverter = _excel.getTypeConverterRegistry()
        .findConverter(valueType);
    int i = 0;
    // convert each element of the map with the converters
    for (final Map.Entry<?, ?> entry : fromMap.entrySet()) {
      final XLValue key = (XLValue) keyConverter.toXLValue(keyType,
          entry.getKey());
      final XLValue value = (XLValue) valueConverter.toXLValue(valueType,
          entry.getValue());
      toArr[i][0] = key;
      toArr[i][1] = value;
      i++;
    }
    return XLArray.of(toArr);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    final XLArray xlArr = (XLArray) from;
    Type keyType = null;
    Type valueType = null;
    if (expectedType instanceof Class) {

      final Class<?> expectedClass = (Class<?>) expectedType;
      if (!expectedClass.isAssignableFrom(Map.class)) {
        throw new Excel4JRuntimeException("expectedType is not a Map");
      }
      final TypeVariable<?>[] typeParameters = expectedClass
          .getTypeParameters();
      if (typeParameters.length != 2) {
        keyType = Object.class;
        valueType = Object.class;
        LOGGER.warn(
            "No type information available on Map, defaulting to Map<Object, Object>");
      } else {
        final Type[] keyBounds = typeParameters[0].getBounds();
        switch (keyBounds.length) {
          case 0:
            keyType = Object.class;
            break;
          case 1:
            keyType = keyBounds[0];
            break;
          default:
            keyType = keyBounds[0];
            LOGGER.warn(
                "Map value parameter has multiple upper bounds, only considering first in conversion");
            break;
        }
        final Type[] valueUpperBounds = typeParameters[1].getBounds();
        switch (valueUpperBounds.length) {
          case 0:
            valueType = Object.class;
            break;
          case 1:
            valueType = valueUpperBounds[0];
            break;
          default:
            valueType = valueUpperBounds[0];
            LOGGER.warn(
                "Map value parameter has multiple upper bounds, only considering first in conversion");
            break;
        }
        valueType = valueUpperBounds[0];
      }
    } else {
      throw new Excel4JRuntimeException("expectedType not Class");
    }
    final XLValue[][] arr = xlArr.getArray();
    final Map<Object, Object> targetMap = new LinkedHashMap<>();
    TypeConverter lastKeyConverter = null;
    TypeConverter lastValueConverter = null;
    Class<?> lastKeyClass = null;
    Class<?> lastValueClass = null;
    final TypeConverterRegistry typeConverterRegistry = _excel
        .getTypeConverterRegistry();
    for (final XLValue[] element : arr) {
      final XLValue keyValue = element[0];
      final XLValue valueValue = element[1];
      // This is a rather weak attempt at optimizing converter lookup - other
      // options seemed to have greater overhead.
      if (lastKeyConverter == null
          || !keyValue.getClass().equals(lastKeyClass)) {
        lastKeyClass = keyValue.getClass();
        lastKeyConverter = typeConverterRegistry
            .findConverter(ExcelToJavaTypeMapping.of(lastKeyClass, keyType));
        if (lastKeyConverter == null) {
          throw new Excel4JRuntimeException("Could not find type converter for "
              + lastKeyClass + " using component type " + keyType);
        }
      }
      if (lastValueConverter == null
          || !valueValue.getClass().equals(lastValueClass)) {
        lastValueClass = valueValue.getClass();
        lastValueConverter = typeConverterRegistry.findConverter(
            ExcelToJavaTypeMapping.of(lastValueClass, valueType));
        if (lastValueConverter == null) {
          throw new Excel4JRuntimeException("Could not find type converter for "
              + lastValueClass + " using component type " + valueType);
        }
      }
      final Object key = lastKeyConverter.toJavaObject(keyType, keyValue);
      final Object value = lastKeyConverter.toJavaObject(keyType, keyValue);
      targetMap.put(key, value);
    }
    return targetMap;
  }
}
