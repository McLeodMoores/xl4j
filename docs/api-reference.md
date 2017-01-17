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
XL4J includes a set of immutable Java types that directly mirror the types used by Excel natively and these types are mapped to and 
from the C union known as `XLOPER12` that is defined by the Excel SDK when calls cross from Java to and from the native code part of 
the add-in.  

Use of these types is mostly optional: the type converter system can convert to and from normal Java types in most cases, but in some
cases you might prefer the explicit control of using these types.  Why might you prefer them?  You will avoid the small overhead of
the type converter system, you may be performing lower level Excel API calls (once available) that require certain types, or you may
want access to reference types containing `XLRange` range references rather than by-value style arrays.

All of these types are immutable, implement `equals` and `hashCode` and have descriptive `toString` implementations suitable for
debugging.  They also all extend the `XLValue` interface, which, beyond acting as a marker interface to collect all the types together,
defines a visitor pattern `accept()` method to make it more efficient to implement functionality that depends on the supplied type 
than a chain of `instanceof` checks.

### XLNumber
This wraps a number type.  This can be any double-precision floating point number, but note that Excel does not support cells containing
`Inf` (infinity) or `NaN` (not-a-number) and sub-normals are truncated to zero.  See `XLError` instances.  It is important to 
understand that Excel represents percentages, integers, accountancy amounts, even dates, as a formatting issue - the underlying
representation of all these as a double-precision floating point value.  You may therefore need to format your data to see the required
format after returning it.  It is intended that future versions of XL4J will add functionality to automatically format results as required.

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
This type represents an Excel array of either one or two dimensions.  Excel has two ways of specifying an array as an input to a
function.  One is explicit, using  curly brackets and comma-separated list syntax `{1, 2, 3}`, which is quite rarely used, or a range 
of the form A1:B2.  A range is not necessarily an array, and if the parameter is registered as a reference type, a range will be passed
from Excel as either an `XLReference` or `XLMultiReference`, but in most cases, when you specify an `@XLParameter(referenceType=false)`
(the default), a range is converted by Excel into an array before passing to the funciton.  Because a range can contain any Excel cells,
an `XLArray` can contain any `XLValue` type in each element.  

When returning array, it's important to understand how Excel *array formulas* work, see the [Introduction to Excel](https://github.com/McLeodMoores/xl4j/blob/master/docs/excel-introduction.md) 
for more information.  In summary though, if your function returns an array, you should highlight the area you want to populate with 
the result, click on the formula bar (or hit F2) and enter your formula (e.g. `=MyArrayFunc()`) and then hit **CTRL-ALT-ENTER**.  If 
you just hit **ENTER** it will not work correctly.  You will then see the forumla replicated in each element of the highlighted range
with array brackets surrounding it.

Presently, `XLArray` is created from a 2D Java array of `XLValue`.  In future, a builder inner class will probably be added for more
convenience.  If you're converting from tabular data provided by another data source (which is likely), you'll probably find it easier
to use an `Object[][]` and let the type converter system handle each conversion itself (possibly adding your own customer type
converters).

```java
XLValue[][] xlValueArr = new XLValue[2][2];
xlValueArr[0][0] = XLString.of("Hello");
xlValueArr[1][0] = XLNil.INSTANCE;
xlValueArr[0][1] = XLNumber.of(42);
xlValueArr[1][1] = XLBoolean.from(false);
XLArray xlArray = XLArray.of(xlValueArr);
assert xlArray.isRow() == false;
assert xlArray.isColumn() == false;
assert xlArray.isArea() == true;
XLValue[][] arr = xlArray.getArray();
assert arr = xlValueArr; // it's not a copy so take 'immutable' with a pinch of salt.
```

### XLError
This type is an enum containing the different errors excel functions can return.  For Java, currently exception level information is 
viewed via the Java log file (see [Logging](https://github.com/McLeodMoores/xl4j/blob/master/docs/logging.md)), although in future
the intention is to allow per-cell Java exceptions to be accessed more easily (via a function and/or and context sensitive inspector 
window).

| Enum value | Excel appearance | Description |
|------------|------------------|-------------|
| Null       | #NULL!           | Errors occur when cell references are separated incorrectly within a formula. A common cause is a space between references rather than an operator or a colon (for ranges).  This is also returned as the indication of a NullPointerException in Java. |
| Div0       | #DIV/0!          | Errors occur when a formula tries to divide a number by zero or an empty cell. |
| Value      | #VALUE!          | Errors occur when a function in a formula has the wrong type of argument. |
| Ref        | #REF!            | Errors occur when a formula contains invalid cell references, often caused by deleted data or cut and pasted cells.
| Name       | #NAME?           | Errors occur when Excel doesn't recognize text in a formula, for example if a Function cannot be found. |
| Num        | #NUM!            | Errors occur when a calculation yields a number that is outside of what Excel can represent. This includes Infinities and NaNs (although sub-normals are truncated to 0 instead).|
| NA         | #N/A             | Errors occur when some data in missing or that inappropriate arguments have been passed to lookup functions (vlookup, etc). |

```java
XLValue retVal;
try {
  return otherMethod(inputs)
} catch (IllegalArgumentException e) {
  return XLError.NA;
}
```

### XLNil
This type represents an empty worksheet cell and is implemented as a Java `enum` with a single value `INSTANCE`.   As with other enums,
it remains part of the `XLValue` class heirarchy.
```java
XLValue value = XLNil.INSTANCE;
```

### XLBigData
This type is a strange beast that presently you can probably safely ignore.  It was introduced into Excel to store binary data on a
worksheet, which sounds really useful.  The problem is that it only works on the current selected worksheet (i.e. where it stores data
is specific to the GUI state) making it much less useful.  Originally `XLBigData` was crafted to store and retrieve serialized objects
from the heap using this mechanism, but this is not presently implemented because of the state problem.  There are alternative ways to
store data in a sheet via COM, so the intention is to use those for persistent object storage in the long term.

In the SDK BigData is a C-union in which one set of fields is used to pass binary data into Excel (pointer + length), and a
different set of fields is used to hold a handle to that data (and a length).  This meant the meaning depended on whether it was an 
input to Excel or an output.

Then, when Excel 2010 introduced asynchronous functions, the BigData data structure was used to hold handles for returning values
once asynchronous calls had completed.  The current implementation of `autoAsynchronous` functions handles all this internally to
the native part of the Add-in, but the plan is to expose `manualAsynchronous` functions in the future, which will require fewer
threads for asynchronous I/O operations that presently.  This implementation is likely to use `XLBigData` but presently no other
API is available that uses it, although the plan is to eventually expose the BigData storage/retrieval API for completeness despite 
it's limited usefulness.

```
byte[] myData = new byte[] { 0xDE, 0xAD, 0xBE, 0xEF };
XLBigData xlBigDataBinary = XLBigData.of(myData); // in binary data mode
assert xlBigDataBinary.getBuffer() == myData;
XLBigData xlBigDataBinary2 - XLBigData.of("Hello"); // serialized data of string "Hello" as binary data
xlBigDataBinary2.getValue().equals("Hello"); // deserialize binary data
```

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
