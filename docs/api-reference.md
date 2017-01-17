API Reference
=============

## Annotations
### @XLNamespace
This annotation applies at the class level and specifies a prefix to be prepended to all `@XLFunction` annotated functions within
the specificied class.  This allows you to easily add, for example, a company-specific prefix to all your functions, and to easily
change it.  For example, in the xll-java project, there are functions for creating java objects and calling methods, etc.  These use
this annotation to prefix all the functions defined with the letter 'J'.  Below is an abridged version of the source code to demonstrate:

```java
@XLNamespace("J")
public final class JConstruct {
  @XLFunction(name = "Construct", ... )
  public static XLValue jconstruct(
      @XLArgument(name = "class name", ...) final XLString className,
      @XLArgument(name = "args", ...) final XLValue... args) {
      ...
  }
  ...
}
```
As you can see, the annotations takes a single argument which defines the namespace prefix to be used.  In this case, the resulting
new user defined function is called using `JConstruct`.

### @XLFunction
This annotation applies at the method level, and is used to indicate methods that implement new user-defined functions (UDFs) that should
be registered with Excel.  There are an number of argument to enable different features:

| Argument name | Type | Req? | Default Value | Description |
|---------------|------|-----------|---------------|-------------|
| `name` | `String` | No | The method name will be transformed into Pascal case | The name with which to register the function with Excel, with any `@XLNamespace` prepended. This name, together with the namespace, should typically match the Excel style of Pascal case with an initial uppercase letter. |
| `category` | `String` | No | The containing class name (excluding any package qualification) | The category in which the function sits.  Excel supports a one-level heirarchy with which to group functions, and thisis it's name.  This heirarchy can be used as a filter when browsing available function in the Insert function dialog. |
| `description` | `String` | No | `""` | The description of the function, as displayed in the Insert function dialog in Excel. |
| `helpTopic` | 'String' | No | `""` | The help topic under which this function should appear in Excel help. |
| `isVolatile` | `boolean` | No | `false` | Notifies Excel as to whether cells containing expressions with this function should be recalculated after *any* calculation.  Use with caution as it can cause many recalculation calls. |
| `isMultiThreadSafe` | `boolean` | No | `true` | Tells Excel this function can safely be called from multiple threads at once.  This means Excel will call in multple threads from it's thread pool, but some macro-class API calls may not be available. |
| `isMacroEquivalent` | `boolean` | No | `false` | Tells Excel this function is macro-equivalent.  This means it will only be called from Excel's main thread, but may mean some extra API calls are available. |
| `typeConversionMode` | `TypeConversionMode` | No | `TypeConversionMode` `.SIMPLEST_RESULT` | Indicates to the Java/Excel type  conversion system what type of type conversions are desired.  Options are `SIMPLEST_RESULT`, which converts results into the most  primitive type possible (e.g. an Excel Number `XLNumber` rather than a java.lang.Double object handle); `OBJECT_RESULT`, which forces the type conversion system to return an object handle (possibly boxing the value) and; `PASSTHROUGH`, which is used only by the type conversion system itself when performing conversions recursively (e.g. on the elements on an array) to avoid types being converted more than once. |
| `functionType` | `FunctionType` | No | `FunctionType` `.FUNCTION` | Tells Excel whether this function is a `FUNCTION` or a `COMMAND`.  Commands can be triggered by buttons and other events outside of the context of function calculations and may be able to access API calls not available to functions. |
| `isLongRunning` | `boolean` | No | `false` | Hint to the add-in that this function may take a significant amount of time to execute.  This currently does nothing, but could be used to trigger auto-asynchonous or interruptable execution. |
| `isAutoAsynchronous` | `boolean` | No | `false` | Tell the add-in to register the function as asynchronous, but to handle the blocking callback within the add-in transparently and use the add-ins asynchronous thread pool to execute the function. |
| `isManualAsynchronous` | `boolean` | No | `false` | Register an asynchronous function, but handle the callback manually.  This is not currently supported and is just the same as `isAutoAsynchronous`.  It should not currently be used. |
| `isCallerRequired` | `boolean` | No | `false` | Tell the add-in to pass the caller information (the cell reference the calculation is taking place in, for example) as the first parameter to the method.  This is not currently supported and should not be used. |


### @XLParameter
This annotation applies to parameters to the method implementing a user-defined function (which should have been annotated with 
`@XLFunction`) and is used to supply meta-data about each parameter to Excel during function registration.  Below is a list of the
available annotation arguments.

| Argument name | Type | Req? | Default Value | Description |
|---------------|------|-----------|---------------|-------------|
| `name` | `String` | No | param name if avail. else param*x* | The name of the parameter, as it is to appear in the Insert function dialog. | 
| `description` | `String` | No | `""` | The description of the parameter, as it is to appear in the Insert function dialog. |
| `optional` | `boolean` | No | `false` | Whether the argument should be considered optional.  Optional parameters will be passed as `null` if not provided otherwise an Exception will be thrown. |
| `referenceType` | `boolean` | No | `false` | This indicates whether an argument should be registered as being a reference type (e.g.
an `XLLocalReference` or `XLMultiReferences` or `XLArray` byref. This will probably only work with commands rather than functions and
hasn't been tested. |

### @XLConstant
This annotation can be applied either to fields, or to classes.  If applied to public fields, it will register a user-defined function
of the same name that returns the value of the field.  If applied to a class, it will register user-defined functions for all public
fields of the class.

| Argument name | Type | Req? | Default Value | Description |
|---------------|------|-----------|---------------|-------------|
| `name` | `String` | No | The field name will be transformed into Pascal case | The name with which to register the function with Excel, with any `@XLNamespace` prepended. This name, together with the namespace, should typically match the Excel style of Pascal case with an initial uppercase letter. |
| `category` | `String` | No | The containing class name (excluding any package qualification) | The category in which the function sits.  Excel supports a one-level heirarchy with which to group functions, and thisis it's name.  This heirarchy can be used as a filter when browsing available function in the Insert function dialog. |
| `description` | `String` | No | `""` | The description of the function, as displayed in the Insert function dialog in Excel. |
| `helpTopic` | 'String' | No | `""` | The help topic under which this function should appear in Excel help. |
| `typeConversionMode` | `TypeConversionMode` | No | `TypeConversionMode` `.SIMPLEST_RESULT` | Indicates to the Java/Excel type  conversion system what type of type conversions are desired.  Options are `SIMPLEST_RESULT`, which converts results into the most  primitive type possible (e.g. an Excel Number `XLNumber` rather than a java.lang.Double object handle); `OBJECT_RESULT`, which forces the type conversion system to return an object handle (possibly boxing the value) and; `PASSTHROUGH`, which is used only by the type conversion system itself when performing conversions recursively (e.g. on the elements on an array) to avoid types being converted more than once. |

## The type system
XL4J includes Java types that directly mirror the types used by Excel natively and these types are mapped to and from the C union 
known as `XLOPER12` that is defined by the Excel SDK when calls cross from Java to and from the native code part of the add-in.  
Use of these types is mostly optional: the type converter system can convert to and from normal Java types in most cases, but in some
cases you might prefer the explicit control of using these types.  Why might you prefer these?  You will avoid the small overhead of
the type converter system, you may be performing lower level Excel API calls (once available) that require certain types, or you may
want access to reference types containing `XLRange` range references rather than by-value style arrays.

All of these types implement `equals` and `hashCode` and have descriptive `toString` implementations suitable for debugging.  They also
all extend the `XLValue` interface, which, beyond acting as a marker interface to collect all the types together, defines a visitor
pattern `accept()` method to make it more efficient to implement functionality that depends on the supplied type than a chain of `instanceof`
checks.

### XLNumber
This wraps a number type.  This can be any double-precision floating point number, but note that Excel does not support cells containing
`Inf` (infinity) or `NaN` (not-a-number).  These are handled as `XLError` instances.  It is important to understand that Excel
represents percentages, integers, accountancy amounts, even dates, as a formatting issue - the underlying representation of all these 
as a double-precision floating point value.  You may therefore need to format your data to see the required format after returning it.
It is intended that future versions of XL4J will add functionality to automatically format results as required.

```java
XLNumber xlNumber = XLNumber.of(3.4d);
double number = xlNumber.getValue();
int i = 12345;
XLNumber xlNumberFromInt = XLNumber.of(i);
i = xlNumberFromInt.getAsInt();
long l = 123345
XLNumber xlNumberFromLong = XLNumber.of(l);
l = xlNumberFromLong.getAsLong();
short s = xlNumber.getAsShort();
float f = xlNumber.getAsFloat();
double d = xlNumber.getAsDouble(); // same as getValue()
```

#### Dates and times
**Dates** and **times** are actually represented using `XLNumber` - the number represents the number of days since either 0th January
1900 (yes, the day before 1st January 1900, there is a reason of sorts!) or 0th January 1904, depending on whether the worksheet is in
1900 or 1904 mode.  

1904 mode is used in the Mac version of Excel for historical reasons.  Which mode is used for a given workbook is configured from the
options section in Excel.  For Windows-created sheets it will always be 1900-mode unless specifically set up otherwise.  There is 
another twist though.  Normally, years that start centuries that aren't a multiple of 100 skip a leap year (unless the year is also
a multiple of 400 years).  This means the year 1900 should have skipped a leap year.  However, Excel counts the 29th February as a
valid day, although it supresses it - this is why the day count starts on 0th January, to remove the extra day.  This was originally
done for efficiency reasons in Lotus 1-2-3, because it means you can every forth year is a leap year without needing special logic for 
1900, which is faster.

See the section on type converters for details on conversion of `Date` and Java 8/JSR-310 types `LocalDate` and `LocalDateTime`.

### XLString
This wraps a string type.  This can be a unicode string up to 32K characters long.  Some typical uses:

```java
XLString xlString = XLString.of("Hello Excel!")
String string = xlString.getValue();
if (xlString.isObject()) { // String has object handle prefix
  XLObject xlObject = xlString.toXLObject(); // See XLObject for details.
}
System.out.println(xlString.toString());
```

### XLBoolean
This wraps a boolean, and is implemented as a Java `enum`.  It still implements `XLValue` so remains part of the class heirarchy.
```java
XLBoolean xlBooleanT = XLBoolean.TRUE;
XLBoolean xlBooleanF = XLBoolean.FALSE;
// use of enum in switch
switch (xlBooleanT) {
  case XLBoolean.TRUE:
    // it was true
    break;
  case XLBoolean.FALSE:
    // it was false
    break;
}
// use of enum in if
if (xlBooleanT == XLBoolean.TRUE) {
  // yes
}
// show conversion to and from boolean
boolean b = true;
XLBoolean converted = XLBoolean.from(b);
if (converted.getValue()) {
  // yes
}
```

### XLArray
### XLError
### XLNil
This type represents an empty worksheet cell and is implemented as a Java `enum` with a single value `INSTANCE`.   As with other enums,
it remains part of the `XLValue` class heirarchy.

### XLBigData
### XLLocalReference
### XLMultiReference
### XLMissing

Associated types
### XLRange
### XLSheetId
### XLObject 
This is not a direct analogue of an Excel type, but rather a special case of an XLString class that encodes an object handle prefixed
with a special character sequence that it is difficult to enter manually, thus minimizing the possibility of invalid handles being
present.
