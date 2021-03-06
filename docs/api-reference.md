API Reference
=============
# Table of Contents
 - [Annotations](#annotations)
   - [@XLNamespace](#xlnamespace)
   - [@XLFunction](#xlfunction)
   - [@XLFunctions](#xlfunctions)
   - [@XLParameter](#xlparameter)
   - [@XLConstant](#xlconstant)
 - [The type system](#the-type-system)
   - Core types
     - [XLNumber](#xlnumber)
       - [Dates and times](#dates-and-times)
     - [XLString](#xlstring)
     - [XLBoolean](#xlboolean)
     - [XLArray](#xlarray)
     - [XLError](#xlerror)
     - [XLNil](#xlnil)
     - [XLBigData](#xlbigdata)
     - [XLLocalReference](#xllocalreference)
     - [XLMultiReference](#xlmultireference)
     - [XLMissing](#xlmissing)
   - [Associated types](#associated-types)
     - [XLRange](#xlrange)
     - [XLSheetId](#xlsheetid)
     - [XLObject](#xlobject)
 - [Type converters](#type-converters)
   - [List of default converters](#list-of-default-converters)
 
   
# Annotations
## @XLNamespace
This annotation applies at the class level and specifies a prefix to be prepended to all `@XLFunction` annotated functions within
the specified class.  This allows you to easily add, for example, a company-specific prefix to all your functions, and to easily
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

## @XLFunction
This annotation applies at the method or constructor level, and is used to indicate methods that implement new user-defined functions (UDFs) that should
be registered with Excel.  There are an number of argument to enable different features:

| Argument name | Type | Req? | Default Value | Description |
|---------------|------|-----------|---------------|-------------|
| `name` | `String` | No | The method name will be transformed into Pascal case | The name with which to register the function with Excel, with any `@XLNamespace` prepended. This name, together with the namespace, should typically match the Excel style of Pascal case with an initial uppercase letter. |
| `category` | `String` | No | The containing class name (excluding any package qualification) | The category in which the function sits.  Excel supports a one-level hierarchy with which to group functions, and this is its name.  This hierarchy can be used as a filter when browsing available function in the Insert function dialog. |
| `description` | `String` | No | `""` | The description of the function, as displayed in the Insert function dialog in Excel. |
| `helpTopic` | 'String' | No | `""` | The help topic under which this function should appear in Excel help. |
| `isVolatile` | `boolean` | No | `false` | Notifies Excel as to whether cells containing expressions with this function should be recalculated after *any* calculation.  Use with caution as it can cause many recalculation calls. |
| `isMultiThreadSafe` | `boolean` | No | `true` | Tells Excel this function can safely be called from multiple threads at once.  This means Excel will call in multple threads from its thread pool, but some macro-class API calls may not be available. |
| `isMacroEquivalent` | `boolean` | No | `false` | Tells Excel this function is macro-equivalent.  This means it will only be called from Excel's main thread, but may mean some extra API calls are available. |
| `typeConversionMode` | `TypeConversionMode` | No | `TypeConversionMode` `.SIMPLEST_RESULT` | Indicates to the Java/Excel type  conversion system what type of type conversions are desired.  Options are `SIMPLEST_RESULT`, which converts results into the most  primitive type possible (e.g. an Excel Number `XLNumber` rather than a java.lang.Double object handle); `OBJECT_RESULT`, which forces the type conversion system to return an object handle (possibly boxing the value) and; `PASSTHROUGH`, which is used only by the type conversion system itself when performing conversions recursively (e.g. on the elements on an array) to avoid types being converted more than once. |
| `functionType` | `FunctionType` | No | `FunctionType` `.FUNCTION` | Tells Excel whether this function is a `FUNCTION` or a `COMMAND`.  Commands can be triggered by buttons and other events outside of the context of function calculations and may be able to access API calls not available to functions. |
| `isLongRunning` | `boolean` | No | `false` | Hint to the add-in that this function may take a significant amount of time to execute.  This currently does nothing, but could be used to trigger auto-asynchonous or interruptable execution. |
| `isAutoAsynchronous` | `boolean` | No | `false` | Tell the add-in to register the function as asynchronous, but to handle the blocking callback within the add-in transparently and use the add-ins asynchronous thread pool to execute the function. |
| `isManualAsynchronous` | `boolean` | No | `false` | Register an asynchronous function, but handle the callback manually.  This is not currently supported and is just the same as `isAutoAsynchronous`.  It should not be used. |
| `isCallerRequired` | `boolean` | No | `false` | Tell the add-in to pass the caller information (the cell reference the calculation is taking place in, for example) as the first parameter to the method.  This is not currently supported and should not be used. |

## @XLFunctions
This annotation is similar to `@XLFunction` but applies at the class level.  It is used to indicate that all public methods and constructors
in the class implement Excel user-defined functions using the arguments specified at the class level (the same parameters as for 
`@XLFunction` except `name`, `description` and `helpTopic`) **except** those methods inherited from `Object` (`equals`, `hashCode`, etc.).  The name will be generated from the Constructor or method name by
capitalising the first character, e.g. myMethod becomes MyMethod.  To override the these arguments for specific methods, simply use 
`@XLFunction` on the method or constructor in question.  Note that in this case the `@XLFunction` will not 'inherit' values from the
enclosing `@XLFunctions`, all required parameters must be stated again, even if they're the same.

## @XLParameter
This annotation applies to parameters to the method implementing a user-defined function (which should have been annotated with 
`@XLFunction`) and is used to supply meta-data about each parameter to Excel during function registration.  Below is a list of the
available annotation arguments.

| Argument name | Type | Req? | Default Value | Description |
|---------------|------|-----------|---------------|-------------|
| `name` | `String` | No | param name if avail. else param*x* | The name of the parameter, as it is to appear in the Insert function dialog. | 
| `description` | `String` | No | `""` | The description of the parameter, as it is to appear in the Insert function dialog. |
| `optional` | `boolean` | No | `false` | Whether the argument should be considered optional.  Optional parameters will be passed as `null` if not provided otherwise an Exception will be thrown. |
| `referenceType` | `boolean` | No | `false` | This indicates whether an argument should be registered as being a reference type (e.g. an `XLLocalReference` or `XLMultiReferences` or `XLArray` byref. This will probably only work with commands rather than functions and hasn't been tested. |

## @XLConstant
This annotation can be applied either to fields, or to classes.  If applied to public fields, it will register a user-defined function
of the same name that returns the value of the field.  If applied to a class, it will register user-defined functions for all public
fields of the class.

| Argument name | Type | Req? | Default Value | Description |
|---------------|------|-----------|---------------|-------------|
| `name` | `String` | No | The field name will be transformed into Pascal case | The name with which to register the function with Excel, with any `@XLNamespace` prepended. This name, together with the namespace, should typically match the Excel style of Pascal case with an initial uppercase letter. |
| `category` | `String` | No | The containing class name (excluding any package qualification) | The category in which the function sits.  Excel supports a one-level hierarchy with which to group functions, and this is its name.  This hierarchy can be used as a filter when browsing available function in the Insert function dialog. |
| `description` | `String` | No | `""` | The description of the function, as displayed in the Insert function dialog in Excel. |
| `helpTopic` | 'String' | No | `""` | The help topic under which this function should appear in Excel help. |
| `typeConversionMode` | `TypeConversionMode` | No | `TypeConversionMode` `.SIMPLEST_RESULT` | Indicates to the Java/Excel type  conversion system what type of type conversions are desired.  Options are `SIMPLEST_RESULT`, which converts results into the most  primitive type possible (e.g. an Excel Number `XLNumber` rather than a java.lang.Double object handle); `OBJECT_RESULT`, which forces the type conversion system to return an object handle (possibly boxing the value) and; `PASSTHROUGH`, which is used only by the type conversion system itself when performing conversions recursively (e.g. on the elements on an array) to avoid types being converted more than once. |

# The type system
XL4J includes a set of immutable Java types that directly mirror the types used by Excel natively. These types are mapped to and 
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

## Core types
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
valid day, although it suppresses it - this is why the day count starts on 0th January, to remove the extra day.  This was originally
done for efficiency reasons in Lotus 1-2-3, because it means you can every fourth year is a leap year without needing special logic for 
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
This wraps a boolean, and is implemented as a Java `enum`.  It still implements `XLValue` so remains part of the class hierarchy.
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
from Excel as either an `XLReference` or `XLMultiReference`. In most cases, when you specify an `@XLParameter(referenceType=false)`
(the default), a range is converted by Excel into an array before passing to the function.  Because a range can contain any Excel cells,
an `XLArray` can contain any `XLValue` type in each element.  

When returning array, it's important to understand how Excel *array formulas* work (see the [Introduction to Excel](https://github.com/McLeodMoores/xl4j/blob/master/docs/excel-introduction.md) 
for more information).  In summary, if your function returns an array, you should highlight the area you want to populate with 
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
This type is an enum containing the different errors Excel functions can return.  For Java, currently exception level information is 
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
it remains part of the `XLValue` class hierarchy.
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
its limited usefulness.

```
byte[] myData = new byte[] { 0xDE, 0xAD, 0xBE, 0xEF };
XLBigData xlBigDataBinary = XLBigData.of(myData); // in binary data mode
assert xlBigDataBinary.getBuffer() == myData;
XLBigData xlBigDataBinary2 - XLBigData.of("Hello"); // serialized data of string "Hello" as binary data
xlBigDataBinary2.getValue().equals("Hello"); // deserialize binary data
```

### XLLocalReference
This type represents a single block of cells on the currently selected worksheet.  It directly maps from the `XLOPER12` type
`zltypeSRef`.  In itself, it is probably of limited use, and is really included for completeness.  In most cases this will be
coerced (type converted in the native part of the add-in using the `xlCoerce` API call) into an `XLMultiReference`, which includes
a `sheetId` field that identifies which worksheet the cell(s) are on (and additionally supports multple ranges selected at once).

`XLLocalReference` takes an `XLRange` type, which is an ancillary type that just wraps the selected range information itself and 
is reused in `XLMultiReference`.  Note that `XLLocalReference` will only be passed into a function if a parameter is registered as
`@XLParameter(referenceType=true)`, and even then, Excel will only pass this type if it is registered as a command or possibly a 
macro-equivalent function.

`XLLocalReference` implements the marker interface `XLReference` along with `XLMultiReference`.

```java
// Range covering A1:C3 (0,0) -> (2, 2)
XLRange blockRange = XLRange.of(0, 0, 2, 2);
XLLocalReference xlLocalRef = XLLocalReference.of(blockRange);
assert blockRange == xlLocalRef.getRange();
// Range just covering single cell B2
XLRange singleCell = XLRange.ofCell(1, 1);
XLLocalReference xlLocalRef2 = XLLocalReference.of(singleCell);
assert singleCell == xlLocalRef2.getRange();
```

### XLMultiReference
This type represents one or more ranges of cells selected on a particular worksheet.  It directly maps from the `XLOPER12` type
`zltypeMRef`.  It can be passed into user defined functions from Excel as a way of referring to cells by reference, the alternative
being `XLArray`, which is effectively by value.  The idea is then that you can call back into Excel's API to put/set elements of the 
range.  Because there isn't currently a callback API implemented, this type is currently of limited use, although it should become more
useful in the future.  Additionally, only parameters registered as `@XLParameter(referenceType=true)` can receive this type, and even
then, Excel will only pass it if the user defined function in question is registered as a command or possibly a macro-equivalent 
function.

`XLMultiReference` implements the marker interface `XLReference` along with `XLLocalReference`.

```java
// in reality the sheet ID would be passed in from Excel via an XLMultiReference argument.
XLSheetId sheetId = XLSheetId.of(1); 
// Range covering A1:C3 (0,0) -> (2, 2)
XLRange blockRange = XLRange.of(0, 0, 2, 2);
XLMultiReference xlMultiRef = XLMultiReference.of(sheetId, blockRange);
List<XLRange> ranges = xlMultiRef.getRanges();
assert ranges.contains(blockRange);
assert ranges.size() == 1;
assert sheetId == xlMultiRef.getSheetId();
assert xlMultiRef.isSingleRange();
assert xlMultiRef.getSingleRange() == blockRange;
XLRange[] rangesArray = xlMultiRef.getRangesArray();
assert rangesArray[0] == blockRange;
assert rangesArray.length == 1;

// Range just covering single cell B2
XLRange singleCell = XLRange.ofCell(1, 1);
XLMultiReference xlMultiRef2 = XLMultiReference.of(sheetId, singleCell);

XLMultiReference xlMultiRef3 = XLMultiReference.of(sheetId, blockRange, singleCell);
List<XLRange> ranges2 = xlMultiRef3.getRanges();
assert ranges2.contains(singleCell);
assert ranges2.contains(blockRange);
assert ranges2.size() == 2;
assert ranges2.isSingleRange() == false;
assert ranges2.getSingleRange() == singleCell; // probably should throw exception in this case, but doesn't
```

### XLMissing
This type represents a missing argument in a parameter list.  As the type converter will generally convert this to a `null` if the 
function isn't expecting an `XLValue`, it's of limited use, but will likely be more useful once a callback API is available.  It is
implemented as a singleton `enum` called `INSTANCE`.

```java
XLValue value = XLMissing.INSTANCE;
```

## Associated types
### XLRange
This type represents a contiguous range of cells in Excel.  It uses the R1C1 style of cell reference in that the column and row are
denoted by an index rather than the column being a letter or letters (the A1 style of cell reference).  It simply takes the top left 
and bottom right indexes of the corners of the contiguous rectangular area inclusive.  Utility methods are included for quickly 
checking if the range is for a single cell, single column or single row of cells.

```java
// Range just covering single cell B2
XLRange singleCell = XLRange.ofCell(1, 1);
assert singleCell.isSingleCell() == true;
assert singleCell.isSingleRow() == true; // row of 1 is still a row.
assert singleCell.isSingleColumn() == true; // column of 1 is still a column.

// Range covering column A1:A3 (0, 0) -> (0, 2)
XLRange column = XLRange.of(0, 0, 0, 2);
assert column.isSingleColumn() == true;
assert column.isSingleRow() == false;
assert column.isSingleCell() == false;

// Range covering row A1:C1 (0, 0) -> (2, 0)
XLRange row = XLRange.of(0, 0, 2, 0);
assert.row.isSingleColumn() == false;
assert row.isSingleRow() == true;
assert row.isSingleCell() == false;

// Range covering A1:C3 (0, 0) -> (2, 2)
XLRange blockRange = XLRange.of(0, 0, 2, 2);
assert blockRange.isSingleCell() == false;
assert blockRange.isSingleRow() == false; 
assert blockRange.isSingleColumn() == false;
```

### XLSheetId
This type simply wraps an integer ID of a given worksheet.  It is supplied embedded in an `XLMultiReference` but will become more
useful as an argument once API access is available.

```java
XLSheetId id = XLSheetId.of(1);
assert id.getSheetId() == 1;
```

### XLObject 
This is not a direct analogue of an Excel type, but rather a special case of an `XLString` class that encodes an object handle prefixed
with a special character sequence that is difficult to enter manually, thus minimizing the possibility of invalid handles being
present.  The object is formed of two parts, a class, which can be supplied as a `Class<?>` type or a `String`, and a 64-bit `long` 
handle.  The class is just used as a prefix to the handle to provide a visual indication to the user of what type of object the 
handle represents, which is considerably more helpful than just having a long number.  The handle is used to identify the actual
java object this cell is referring to, which is actually stored in an instance of `Heap`, which is in the 
[`com.mcleodmoores.xl4j.v1.api.core`](https://github.com/McLeodMoores/xl4j/tree/master/xll-core/src/main/java/com/mcleodmoores/xl4j/v1/core) package and is accessible via the `Excel` singleton.  The type conversion system will handle 
object handles transparently, so for the most part no explicit use of `XLObject` is required, but it is used internally and may be 
used explicitly if required.
```
long handle = 1L;
XLObject xlObject = XLObject.of(Map.class, 1);
XLObject xlStrObject = XLObject.of("java.util.Map", 1);
assert xlObject == xlStrObject;

// heap example
Heap heap = new Heap();
JFrame jFrame = new JFrame("Hello World");
XLObject xlObject = XLObject.of(jFrame.getClass(), heap.getHandle(jFrame));
// send to Excel...
JFrame JFrameBack = (JFrame) heap.getObject(xlObject.getHandle());
```

# Type converters
To make it easier to work with normal and custom Java types there is a sophisticated type conversion system in place to marshall data
automatically from Excel types to Java types and back again.  This mostly works transparently as far as the developer and user are
concerned but there are times it will be useful to add your own custom type converters.  For example, you might want an Excel array
(range of cells passed by value) to be automatically converted into a request object for a backend system, or a into a time series
object for analysis.  To do this it's useful to understand the basic structure of the type conversion system and a view of which
type converters are already available.

Type converters are registered in a `TypeConverterRegistry`, the main implementation of which `ScanningTypeConverterRegistry`.  This
registry scans the classpath for Classes implementing `TypeConverter`, so to implement and register your own type converter, all you
need to do is create a class implementing `TypeConverter` (or, more likely, extending the abstract utility class
`AbstractTypeConverter`).

For the most part, which converter is used during a call to your method is statically determined at start-up.  Occasionally, in cases
such as `Object` arrays, conversion will need to be somewhat dynamic and scan the type conversion registry at run-time.  Usally though,
when your `@XLFunction`s are registered, a `MethodInvoker` object is created that has all the type converters pre-computed for each
parameter.  Then, when the native part of the add-in calls into Java to invoke your function, each argument is passed to the
corresponding `TypeConverter`.  Return values are converted in a similar way, but using the methods that convert in the other direction.

Because sometimes we do call the `TypeConverterRegistry` dynamically, we want it to be as fast as possible.  The slow approach
would be to ask each type converter in turn if it can do the conversion, but we short circuit this by using two key classes called
`JavaToExcelTypeMapping` and `ExcelToJavaTypeMapping`.  `CachingTypeConverterRegistry` then uses these to provide fast hash-based
caching layer on the look-up of the appropriate type converters.

Below is an example of the converter for JSR-310 backport `LocalDate` to and from `XLNumber` (exluding the imports and some comments
and annotations).

```java
public final class LocalDateXLNumberTypeConverter extends AbstractTypeConverter {
  private static final int EXCEL_EPOCH_YEAR = 1900;

  public LocalDateXLNumberTypeConverter() {
    super(LocalDate.class, XLNumber.class);
  }

  private static final long DAYS_FROM_EXCEL_EPOCH = ChronoUnit.DAYS.between(LocalDate.of(EXCEL_EPOCH_YEAR, 1, 1), 
                                                                            LocalDate.ofEpochDay(0)) + 1;

  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLNumber.of(((LocalDate) from).toEpochDay() + DAYS_FROM_EXCEL_EPOCH);
  }

  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    final long epochDays = (long) ((XLNumber) from).getValue() - DAYS_FROM_EXCEL_EPOCH;
    return LocalDate.ofEpochDay(epochDays);
  }
}
```

The call to the super-class constructor creates the appropriate `JavaToExcelMapping` and `ExcelToJavaMapping` keys and the super-class
provides these to the registry.  Note how the converter class deals with conversions in both directions.  In both the `toXLValue` and
`toJavaObject` methods, we are passed the source object and an *expected type*.  This is of type `java.lang.Type`, which allows us
to specify primitive types and generic types.  For some converters, it may be useful to delve into the `expectedType` object to 
get more information about generics, which because they are provided from the method signatures of the classes in question, rather
than type-erased run-time objects, can actually contain useful information.

There is also a second super-class constructor not used here that takes a *priority* level.  This is used to determine the search 
order of type converters, with the highest being tried first.  This is useful because it allows more specific converters to be
at the front of the queue, falling back to more generic converters later.  Below is a list of the default type converters and their
priorities.  The number is an ordinal, just used for ordering and the magnitude has no meaning - they are spaced out a bit so 
other converters and more easily be raised and lowered more easily if required.  Determining the appropriate priority level is a bit
of a black art, but as a rule of thumb, use the default level unless you run into issues and then raise it if necessary.  Low prioriy
converters are used for the most generic conversions such as `Object` to `XLObject`.

## List of default converters

| Priority | Converter Class | Excel Class | Java Type |
|----------|-----------------|-----------------|---------------|
| 100 | `XLNumberIdentityConverter` | `XLNumber` | `XLNumber` |
| 100 | `XLBooleanIdentityConverter` | `XLBoolean` | `XLBoolean` |
| 100 | `XLStringIdentityConverter` | `XLString` | `XLString` |
| 100 | `XLObjectIdentityConverter` | `XLObject` | `XLObject` |
| 100 | `XLIntegerIdentityConverter` | `XLInteger` | `XLInteger` |
| 100 | `XLBigDataIdentityConverter` | `XLBigData` | `XLBigData` |
| 100 | `XLArrayIdentityConverter` | `XLArray` | `XLArray`|
| 100 | `XLLocalReferenceIdentityConverter` | `XLLocalReference` | `XLLocalReference` |
| 100 | `XLMultiReferenceIdentityConverter` | ``XLMultiReference`` | `XLMultiReference`| 
| 100 | `XLErrorIdentityConverter` | `XLError` | `XLError`|
| 100 | `XLNilIdentityConverter` | `XLNil` | `XLNil`|
| 100 | `XLMissingIdentityConverter` | `XLMissing` | `XLMissing` |
| 11 | `ObjectArray2DXLArrayTypeConverter` | `XLArray` | `java.lang.Object[][]` |
| 10 | `PrimitiveBooleanXLBooleanTypeConverter` | `XLBoolean` | `boolean` |
| 10 | `PrimitiveByteXLNumberTypeConverter` | `XLNumber` | `byte` |
| 10 | `PrimitiveShortXLNumberTypeConverter` | `XLNumber` | `short` |
| 10 | `PrimitiveIntegerXLNumberTypeConverter` | `XLNumber` | `int` |
| 10 | `PrimitiveLongXLNumberTypeConverter` | `XLNumber` | `long` |
| 10 | `PrimitiveFloatXLNumberTypeConverter` | `XLNumber` | `float` |
| 10 | `PrimitiveDoubleXLNumberTypeConverter` | `XLNumber` | `double` |
| 10 | `BooleanXLBooleanTypeConverter` | `XLBoolean` | `java.lang.Boolean` |
| 10 | `ByteXLNumberTypeConverter` | `XLNumber` | `java.lang.Byte` |
| 10 | `ShortXLNumberTypeConverter` | `XLNumber` | `java.lang.Short` |
| 10 | `IntegerXLNumberTypeConverter` | `XLNumber` | `java.lang.Integer` |
| 10 | `LongXLNumberTypeConverter` | `XLNumber` | `java.lang.Long` |
| 10 | `FloatXLNumberTypeConverter` | `XLNumber` | `java.lang.Float` |
| 10 | `DoubleXLNumberTypeConverter` | `XLNumber` | `java.lang.Double` |
| 10 | `StringXLStringTypeConverter` | `XLString` | `java.lang.String` | 
| 10 | `BigIntegerXLNumberTypeConverter` | `XLNumber` | `java.math.BigInteger` |
| 10 | `BigDecimalXLNumberTypeConverter` | `XLNumber` | `java.math.BigDecimal` |
| 10 | `LocalDateXLNumberTypeConverter` | `XLNumber` | `org.threeten.bp.LocalDate` |
| 10 | `PrimitiveBooleanArrayXLArrayTypeConverter` | `XLArray` | `boolean[]` |
| 10 | `PrimitiveByteArrayXLArrayTypeConverter` | `XLArray` | `byte[]` |
| 10 | `PrimitiveShortArrayXLArrayTypeConverter` | `XLArray` | `short[]` |
| 10 | `PrimitiveIntegerArrayXLArrayTypeConverter` | `XLArray` | `int[]` |
| 10 | `PrimitiveLongArrayXLArrayTypeConverter` | `XLArray` | `long[]` |
| 10 | `PrimitiveFloatArrayXLArrayTypeConverter` | `XLArray` | `float[]` |
| 10 | `PrimitiveDoubleArrayXLArrayTypeConverter` | `XLArray` | `double[]` |
| 10 | `ObjectArrayXLArrayTypeConverter` | `XLArray` | `java.lang.Object[]` |
| 7 | `ObjectArray2DXLArrayTypeConverter2` | `XLArray` | `java.lang.Object[][]` |
| 7 | `XLValueArrayXLValueArrayTypeConverter` | `XLValue` | `XLValue[]` |
| 7 | `EnumXLStringTypeConverter` | `XLString` | `java.lang.Enum` |
| 6 | `ObjectArrayXLArrayTypeConverter2` | `XLArray` | `java.lang.Object[]` |
| 6 | `XLValueXLValueTypeConverter` | `XLValue` | `XLValue` |
| 6 | `Set2XLArrayTypeConverter` | `XLArray` | `java.util.Set` |
| 6 | `List2XLArrayTypeConverter` | `XLArray` | `java.util.List` |
| 6 | `Map2XLArrayTypeConverter` | `XLArray` | `java.util.Map` |
| 5 | `InfNaNXLErrorTypeConverter` | `XLError` | `java.lang.Double` |
| 5 | `ObjectXLObjectTypeConverter` | `XLObject` | `java.lang.Object` |
| -1 | `PrimitiveBooleanXLStringTypeConverter` | `XLString` | `boolean `|
| -1 | `PrimitiveByteXLStringTypeConverter` | `XLString` | `byte` |
| -1 | `PrimitiveCharXLStringTypeConverter` | `XLString` | `char` |
| -1 | `PrimitiveShortXLStringTypeConverter` | `XLString` | `short` |
| -1 | `PrimitiveIntegerXLStringTypeConverter` | `XLString` | `int` |
| -1 | `PrimitiveLongXLStringTypeConverter` | `XLString` | `long` |
| -1 | `PrimitiveFloatXLStringTypeConverter` | `XLString` | `float` |
| -1 | `PrimitiveDoubleXLStringTypeConverter` | `XLString` | `double` |
| -1 | `BooleanXLStringTypeConverter` | `XLString` | `java.lang.Boolean` |
| -1 | `ByteXLStringTypeConverter` | `XLString` | `java.lang.Byte` |
| -1 | `CharacterXLStringTypeConverter` | `XLString` | `java.lang.Character` |
| -1 | `ShortXLStringTypeConverter` | `XLString` | `java.lang.Short` |
| -1 | `IntegerXLStringTypeConverter` | `XLString` | `java.lang.Integer` |
| -1 | `LongXLStringTypeConverter` | `XLString` | `java.lang.Long` |
| -1 | `FloatXLStringTypeConverter` | `XLString` | `java.lang.Float` |
| -1 | `DoubleXLStringTypeConverter` | `XLString` | `java.lang.Double` |
| -7 | `ObjectXLBooleanTypeConverter` | `XLBoolean` | `java.lang.Object` | 
| -7 | `ObjectXLNumberTypeConverter` | `XLNumber` | `java.lang.Object` | 
| -7 | `ObjectXLStringTypeConverter` | `XLString` | `java.lang.Object` |
