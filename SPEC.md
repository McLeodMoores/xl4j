Specification
=============

# Standard UDFs
So an Excel function like `MyJavaFunction(Number1, Number2)`->`Number` would be:
``` java
package com.mcleodmoores.excel4j.example;

import com.mcelodmoores.excel4j.*;

class MyFunctions {
  @XLFunction(name = "MyJavaFunction", category = StandardCategories.Financial, 
              description = "My Java Function", volatile = "true", allowReferences = "false")
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
5. `@XLFunction.allowReferences' would default to false.
6. `@XLArgument.name` would default to:
  - debug symbol name if available (paranamer) 
  - the JavaDoc name if available (or perhaps this should be first?)
  - 'arg<x>', starting at the nth argument (1-based, so if the first arg has a name but the second doesn't it becomes arg2).  I'm prepared to hear that this should be zero-based but my gut says not.
7. `@XLArgument.description` would default to:
  - the JavaDoc description if available.
8. `@XLArgument.optional` would default to false.

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

# RTD Functions/Servers
One possibility is that we expose and call the COM API directly to forward to an RTD function.
``` java
Excel excel = Excel.getInstance();
XLValue value = excel.getWorksheetFunction().rtd("MyRTDServerProdId", "ServerName", "Topic1", "Topic2").getValue();
return value;
```
This might in practice need to be via the non-COM API though because of threading issues.
A more user-friendly approach might be to add a different class of Excel function:
``` java
@XLRealTimeDataFunction(name = "MyRTDFunction", category = StandardCategories.Financial, description = "My RTD Function")
public RTDArgs myRtdFunction(final String topic1, final String topic2) {
  return RTDArgs.Builder.of("MyRTDServerProdId", "ServerName").withTopic(topic1).withTopic(topic2).build();
}
```
and we'd then generate the RTD function call back into Excel outside of the Java code.  Note that `@XLRealTimeDataFunction` would have the same attributes as `@XLFunction` except for the volatile attribute, which is not applicable to RTD functions.

This of course doesn't actually deal with the RTD server itself.  In this case the Java code should simply closely mirror the COM IRtdServer interface:
``` java
public interface XLRealTimeDataServer {
  XLConnectDataReponse connectData(int topicId, String[] topics);
  void disconnectData(int topicId);
  int heartBeat();
  List<XLRefreshDataResponse> refreshData();
  int serverStart(XLRealTimeDataUpdateEvent handler);
  void serverTerminate();
}

public interface XLRealTimeDataUpdateEvent {
  void disconnect(); // stop servicing this event handler.
  void updateNotify(); // data ready to pull with refreshData();
  int getHeartbeatInterval();
  void setHeartbeatInterval(); // might want to hide setter from client by excluding from interface.
}

public interface XLConnectDataResponse {
  XLValue getValue();
  boolean isUpdated(); // are new values required
}

public interface XLRefreshDataResponse {
  String[] getTopics();
  XLValue getValue();
}
```

## Explcit types

The aim here is for the developer to have something to fall back on if the automatic marshalling doesn't work.
### XLValue
This broadly mirrors the XLOPER* data structure.  The interesting question is whether this should be an object heirarchy or a type + object structure.  This could be done thus:
  - `XLString`
  - `XLReference`? `XLRange`? - can we automatically manage the difference between local references and other worksheets (xltypeRef vs xltypeSRef)?
  - `XLBoolean`
  - `XLNumber`
  - `XLArray` - mapped to multi
  - `XLError` - do we need multiple types here?
  - `XLNil` - missing array elements
  - do we need the more obscure stuff?
    - `XLShort`
    - `XLBigData`
    - Macro flow control crap

### Type conversions

# Direct Java Calls
Direct Java Calls refers to the abililty to call into unmodified Java classes and methods directly from Excel.  

 - `JConstruct(<class-name>, { <argument-x> }[0..n] })` - call a constructor, returning an object handle.
 - `JMethod(<object-handle> | <class-name>, <method-name>, { <argument-x> }[0..n] })` calls a method, returning an object handle or primitive.  The default uses standard Excel dependency tracking so may become stale (?)
   - Reduce type variants, these reduce results to Excel Number, Boolean or String wherever possible:
     - `JMethodV(...)` volatile function that is evaluated every time anything on the sheet changes.  
     - `JMethodR(...)` monitors the object for changes and updates the cell with a re-evaluated result via RTD server.
   - Explicit type variants, these keep all results as Java object handles.
     - `JMethodVX(...)` volatile, explicit typed results.
     - `JMethodRX(...)` real-time (RTD), explciti typed results
 - `JReadMember(<object-handle> | <class-name>, <member-name>) - read a member, returning an object handle or primitive.
   - Reduce type variants, these reduce results to Excel Number, Boolean or String wherever possible:
     - `JReadMemberV(...)` volatile function that is evaluated every time anything on the sheet changes.  
     - `JReadMemberR(...)` monitors the object for changes and updates the cell with a re-evaluated result via RTD server.
   - Explicit type variants, these keep all results as Java object handles.
     - `JMethodVX(...)` volatile, explicit typed results.
     - `JMethodRX(...)` real-time (RTD), explciti typed results
 - `JWriteMember(<object-handle> | <class-name>, <member-name>, <value-or-object-handle>)` - write a member, returning an empty cell.


fdksjafkldsj
fdaslkjflksdj
fasdlkjfalksdjf
fdlksajflkajsdlkfj
ldsakjflajdslajfladskjf
afkljdslfjasldjfldajs
dskfjalksdjfkjadslkfj
fdalskjflajsdlfja;ds


Padding because dillinger doesn't scroll to bottom properly




