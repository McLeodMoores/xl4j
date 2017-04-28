/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1;

import static org.testng.Assert.assertEquals;

import java.util.Collection;
import java.util.Objects;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.annotations.XLConstant;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.api.core.Excel;
import com.mcleodmoores.xl4j.v1.api.core.ExcelCallback;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFunctionCallHandler;
import com.mcleodmoores.xl4j.v1.api.core.FunctionDefinition;
import com.mcleodmoores.xl4j.v1.api.core.Heap;
import com.mcleodmoores.xl4j.v1.api.core.InvokerFactory;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.xl4j.v1.api.values.XLError;
import com.mcleodmoores.xl4j.v1.api.values.XLNumber;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;
import com.mcleodmoores.xl4j.v1.api.values.XLValue;
import com.mcleodmoores.xl4j.v1.core.DefaultExcelCallback;
import com.mcleodmoores.xl4j.v1.core.DefaultExcelFunctionCallHandler;
import com.mcleodmoores.xl4j.v1.invoke.ReflectiveInvokerFactory;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.DoubleXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.IntegerXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveDoubleXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.StringXLStringTypeConverter;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;
import com.mcleodmoores.xl4j.v1.xll.NativeExcelFunctionEntryAccumulator;

/**
 * Unit tests for {@link DefaultExcelFunctionCallHandler}.
 */
public class DefaultExcelFunctionCallHandlerTest {
  private static final Excel EXCEL = ExcelFactory.getInstance();
  private static final Heap HEAP = EXCEL.getHeap();
  private static final TypeConverterRegistry TYPE_CONVERTERS = MockTypeConverterRegistry.builder()
      .with(new DoubleXLNumberTypeConverter())
      .with(new IntegerXLNumberTypeConverter())
      .with(new PrimitiveDoubleXLNumberTypeConverter())
      .with(new StringXLStringTypeConverter())
      .build();

  /**
   * Tests the exception when the function registry is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullFunctionRegistry() {
    new DefaultExcelFunctionCallHandler(null, HEAP);
  }

  /**
   * Tests the exception when the heap is null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullHeap() {
    new DefaultExcelFunctionCallHandler(MockFunctionRegistry.builder().build(), null);
  }

  /**
   * Tests the exception when the arguments are null.
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNullArguments() {
    new DefaultExcelFunctionCallHandler(MockFunctionRegistry.builder().build(), HEAP).invoke(1, (XLValue[]) null);
  }

  /**
   * Tests that passing in an invalid export number is handled correctly.
   */
  @Test
  public void testFunctionDefinitionNotAvailable() {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .build();
    final ExcelFunctionCallHandler handler = new DefaultExcelFunctionCallHandler(registry, HEAP);
    final Object result = handler.invoke(1, XLNumber.of(10));
    assertEquals(result, XLError.Null);
  }

  /**
   * Tests the return value when the object that the method is being called on is not in the heap.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testInvalidObjectHandle1() throws NoSuchMethodException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass.class.getMethod("method", Double.class))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    final ExcelFunctionCallHandler handler = new DefaultExcelFunctionCallHandler(registry, HEAP);
    final ExcelCallback callback = new DefaultExcelCallback(new NativeExcelFunctionEntryAccumulator());
    registry.createAndRegisterFunctions(invokerFactory);
    registry.registerFunctions(callback);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final int exportNumber = definitions.iterator().next().getExportNumber();
    final Object result = handler.invoke(exportNumber, XLObject.of(TestClass.class, -1L), XLNumber.of(10));
    assertEquals(result, XLError.Null);
  }


  /**
   * Tests the return value when the object that contains the field is not on the heap.
   * @throws SecurityException  if the field cannot be found
   * @throws NoSuchFieldException  if the field cannot be found
   */
  @Test
  public void testInvalidObjectHandle2() throws NoSuchFieldException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlConstant(TestClass.class.getField("_field"))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    final ExcelFunctionCallHandler handler = new DefaultExcelFunctionCallHandler(registry, HEAP);
    final ExcelCallback callback = new DefaultExcelCallback(new NativeExcelFunctionEntryAccumulator());
    registry.createAndRegisterFunctions(invokerFactory);
    registry.registerFunctions(callback);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final int exportNumber = definitions.iterator().next().getExportNumber();
    final Object result = handler.invoke(exportNumber, XLObject.of(TestClass.class, -1L));
    assertEquals(result, XLError.Null);
  }

  /**
   * Tests the return value if too many arguments are supplied to a method.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testTooManyArgs1() throws NoSuchMethodException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass.class.getMethod("staticMethod", String.class))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    final ExcelFunctionCallHandler handler = new DefaultExcelFunctionCallHandler(registry, HEAP);
    final ExcelCallback callback = new DefaultExcelCallback(new NativeExcelFunctionEntryAccumulator());
    registry.createAndRegisterFunctions(invokerFactory);
    registry.registerFunctions(callback);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final int exportNumber = definitions.iterator().next().getExportNumber();
    assertEquals(handler.invoke(exportNumber, XLString.of("A"), XLString.of("B"), XLString.of("C")), XLError.Null);
  }

  /**
   * Tests the return value if too few arguments are supplied to a method.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testTooFewArgs1() throws NoSuchMethodException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass.class.getMethod("staticMethod", String.class))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    final ExcelFunctionCallHandler handler = new DefaultExcelFunctionCallHandler(registry, HEAP);
    final ExcelCallback callback = new DefaultExcelCallback(new NativeExcelFunctionEntryAccumulator());
    registry.createAndRegisterFunctions(invokerFactory);
    registry.registerFunctions(callback);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final int exportNumber = definitions.iterator().next().getExportNumber();
    assertEquals(handler.invoke(exportNumber, new XLValue[0]), XLError.Null);
  }

  /**
   * Tests getting an instance field.
   * @throws NoSuchFieldException  if the field cannot be found
   * @throws SecurityException  if the field cannot be found
   */
  @Test
  public void testField() throws NoSuchFieldException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlConstant(TestClass.class.getField("_field"))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    final ExcelFunctionCallHandler handler = new DefaultExcelFunctionCallHandler(registry, HEAP);
    final ExcelCallback callback = new DefaultExcelCallback(new NativeExcelFunctionEntryAccumulator());
    registry.createAndRegisterFunctions(invokerFactory);
    registry.registerFunctions(callback);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final int exportNumber = definitions.iterator().next().getExportNumber();
    final TestClass object = new TestClass(2);
    final XLObject xlObject = XLObject.of(object.getClass(), HEAP.getHandle(object));
    assertEquals(handler.invoke(exportNumber, xlObject), XLString.of("Field"));
    // try with argument as string
    assertEquals(handler.invoke(exportNumber, xlObject.toXLString()), XLString.of("Field"));
  }

  /**
   * Tests getting a static field.
   * @throws SecurityException  if the field cannot be found
   * @throws NoSuchFieldException   if the field cannot be found
   */
  @Test
  public void testStaticField() throws NoSuchFieldException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlConstant(TestClass.class.getField("FIELD"))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    final ExcelFunctionCallHandler handler = new DefaultExcelFunctionCallHandler(registry, HEAP);
    final ExcelCallback callback = new DefaultExcelCallback(new NativeExcelFunctionEntryAccumulator());
    registry.createAndRegisterFunctions(invokerFactory);
    registry.registerFunctions(callback);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final int exportNumber = definitions.iterator().next().getExportNumber();
    assertEquals(handler.invoke(exportNumber, new XLValue[0]), XLString.of("StaticField"));
  }

  /**
   * Test invoking a method.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testMethod() throws NoSuchMethodException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass.class.getMethod("method", Double.class))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    final ExcelFunctionCallHandler handler = new DefaultExcelFunctionCallHandler(registry, HEAP);
    final ExcelCallback callback = new DefaultExcelCallback(new NativeExcelFunctionEntryAccumulator());
    registry.createAndRegisterFunctions(invokerFactory);
    registry.registerFunctions(callback);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final int exportNumber = definitions.iterator().next().getExportNumber();
    final TestClass object = new TestClass(2);
    final XLObject xlObject = XLObject.of(object.getClass(), HEAP.getHandle(object));
    assertEquals(handler.invoke(exportNumber, xlObject, XLNumber.of(5)), XLNumber.of(10));
    // try with argument as string
    assertEquals(handler.invoke(exportNumber, xlObject.toXLString(), XLNumber.of(4)), XLNumber.of(8));
  }

  /**
   * Tests invoking a static method.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testStaticMethod() throws NoSuchMethodException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass.class.getMethod("staticMethod", String.class))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    final ExcelFunctionCallHandler handler = new DefaultExcelFunctionCallHandler(registry, HEAP);
    final ExcelCallback callback = new DefaultExcelCallback(new NativeExcelFunctionEntryAccumulator());
    registry.createAndRegisterFunctions(invokerFactory);
    registry.registerFunctions(callback);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final int exportNumber = definitions.iterator().next().getExportNumber();
    assertEquals(handler.invoke(exportNumber, XLString.of("A")), XLString.of("A####"));
  }

  /**
   * Tests creating a new instance of a class.
   * @throws NoSuchMethodException  if the constructor cannot be found
   * @throws SecurityException  if the constructor cannot be found
   */
  @Test
  public void testConstructor() throws NoSuchMethodException, SecurityException {
    final MockFunctionRegistry registry = MockFunctionRegistry.builder()
        .xlFunction(TestClass.class.getConstructor(Integer.class))
        .build();
    final InvokerFactory invokerFactory = new ReflectiveInvokerFactory(EXCEL, TYPE_CONVERTERS);
    final ExcelFunctionCallHandler handler = new DefaultExcelFunctionCallHandler(registry, HEAP);
    final ExcelCallback callback = new DefaultExcelCallback(new NativeExcelFunctionEntryAccumulator());
    registry.createAndRegisterFunctions(invokerFactory);
    registry.registerFunctions(callback);
    final Collection<FunctionDefinition> definitions = registry.getFunctionDefinitions();
    assertEquals(definitions.size(), 1);
    final int exportNumber = definitions.iterator().next().getExportNumber();
    final XLObject xlObject = (XLObject) handler.invoke(exportNumber, XLNumber.of(23));
    assertEquals(HEAP.getObject(xlObject.getHandle()), new TestClass(23));
  }

  /**
   * A test class.
   */
  // CHECKSTYLE:OFF
  public static class TestClass {

    @XLConstant(name = "Field")
    public final String _field = "Field";

    @XLConstant(name = "StaticField")
    public static final String FIELD = "StaticField";

    private final Integer _i;

    @XLFunction(name = "TestClassConstructor")
    public TestClass(@XLParameter(name = "Integer") final Integer i) {
      _i = i * 3465;
    }

    @XLFunction(name = "TestClassMethod")
    public double method(@XLParameter(name = "Double") final Double d) {
      return d * 2;
    }

    @XLFunction(name = "TestClassStaticMethod")
    public static String staticMethod(@XLParameter(name = "String") final String s) {
      return s + "####";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (_field == null ? 0 : _field.hashCode());
      result = prime * result + (_i == null ? 0 : _i.hashCode());
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof TestClass)) {
        return false;
      }
      final TestClass other = (TestClass) obj;
      return Objects.equals(_field, other._field) && _i.intValue() == other._i.intValue();
    }
  }
}
