Specification
=============

# Standard UDFs
So an Excel function like `MyJavaFunction(Number1, Number2)`->`Number` would be:
``` java
package com.mcleodmoores.excel4j.example;

import com.mcelodmoores.excel4j.*;

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

Registering the same method twice with the same `@XLFunction.name` could be allowable in some cases to handle overloading.

## Class or Package level prefixes
To allow a degree of namespaceing we need another anotation. In the case of a package it would go in the `package-java.java` file in the package you want:

``` java
@XLNamespace(name = "Ex")
package com.mcleodmoores.excel4j.examples;

import com.mcleodmoores.excel4j.XLNamespace;
```

which for the above example would lead to the functions being registered as `ExMyJavaFunction`.  Note the import after the package declaration.  In the case of a class if would be:

``` java
package com.mcleodmoores.examples;

import com.mcleodmoores.excel4j.*;

@XLNamespace(name = "Ex1")
public class Example1 {
  @XLFunction
  ...
}
```

and in this case would lead to initial example function being registered as `Ex1MyJavaFunction`.

## Commands 
Commands would be handled by setting the command flag if the return type is `void`.

## Explcit types
### XLRange
Wrapped array of mixed types


