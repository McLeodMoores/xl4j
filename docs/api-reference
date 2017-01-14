API Reference
=============

## Annotations
### @XLNamespace
This annotation applies at the class level and specifies a prefix to be prepended to all `@XLFunction` annotated functions within
the specificied class.  This allows you to easily add, for example, a company-specific prefix to all your functions, and to easily
change it.  For example, in the xll-java project, there are functions for creating java objects and calling methods, etc.  These use
this annotation to prefix all the functions defined with the letter 'J'.  Below is an abridged version of the source code to demonstate

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
As you can see, the annotations takes a single argument which defines the namespace prefix to be used.
