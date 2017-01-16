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
| `category` | `String` | No | the containing class name (excluding any package qualification) | The category in which the function sits.  Excel supports a one-level heirarchy with which to group functions, and thisis it's name.  This heirarchy can be used as a filter when browsing available function in the Insert function dialog. |
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
