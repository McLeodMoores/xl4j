Specification
=============

# Standard UDFs
So an Excel function like `MyJavaFunction(Number1, Number2)`->`Number` would be:
```
class MyFunctions {

  @XLFunction(name = "MyJavaFunction", category = StandardCategories.Financial, description = "My Java Function", volatile = "true")
  public double myJavaFunction(@XLArgument(name = "arg1", description = "first argument", optional = "false") double arg1, 
                               @XLArgument(name = "arg2", description = "second argument", optional = "false") double param2) {
    return param1 + param2;
  }
}
```

In this example most of these annotations would be optional

1. `@XLFunction.name` would default to the method name by doing a conversion to camel case.
2. `@XLFunction.category` would default to the Class name.
3. `@XLFunction.description` would lift the first line of any JavaDoc comment or otherwise be the fully qualified class name plus themethod name.
4. `@XLFunction.volatile` would default to false.
5. `@XLArgument.name` would default to:
  - debug symbol name if available (paranamer) 
  - the JavaDoc name if available (or perhaps this should be first?)
  - 'arg<x>', starting at the nth argument (1-based, so if the first arg has a name but the second doesn't it becomes arg2).  I'm prepared to hear that this should be zero-based but my gut says not.
6. `@XLArgument.description` would default to:
  - the JavaDoc description if available.
7. `@XLArgument.optional` would default to false.
