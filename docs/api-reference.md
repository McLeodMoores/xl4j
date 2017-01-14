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

| Argument name | Type | Required? | Default Value | Description |
| `name` | `String` | No | The method name will be transformed into Pascal case | The name with which to register the function with Excel, with 
any `@XLNamespace` prepended. This name, together with the namespace, should typically match the Excel style of Pascal case with an initial uppercase letter. |
| `category` | `String` | No | the containing class name (excluding any package qualification) | The category in which the function
sits.  Excel supports a one-level heirarchy with which to group functions, and thisis it's name.  This heirarchy can be used as a 
filter when browsing available function in the Insert function dialog. |
| `description` | `String` | No | `""` | The description of the function, as displayed in the Insert function dialog in Excel. |
| `helpTopic` | 'String' | No | `""` | The help topic under which this function should appear in Excel help. |
| `isVolatile` | `boolean` | No | `false` | Notifies Excel as to whether cells containing expressions with this function should be
recalculated after *any* calculation.  Use with caution as it can cause many recalculation calls. |
| `isMultiThreadSafe` | `boolean` | No | `true` | Tells Excel this function can safely be called from multiple threads at once.  This
means Excel will call in multple threads from it's thread pool, but some macro-class API calls may not be available. |
| `isMacroEquivalent` | `boolean` | No | `false` | Tells Excel this function is macro-equivalent.  This means it will only be called
from Excel's main thread, but may mean some extra API calls are available. |
| `typeConversionMode` | enum `TypeConversionMode` | No | `TypeConversionMode.SIMPLEST_RESULT` | Indicates to the Java/Excel type 
conversion system what type of type conversions are desired.  Options are `SIMPLEST_RESULT`, which converts results into the most 
primitive type possible (e.g. an Excel Number `XLNumber` rather than a java.lang.Double object handle); `OBJECT_RESULT`, which forces
the type conversion system to return an object handle (possibly boxing the value) and; `PASSTHROUGH`, which is used only by the 
type conversion system itself when performing conversions recursively (e.g. on the elements on an array) to avoid types being converted
more than once. |
