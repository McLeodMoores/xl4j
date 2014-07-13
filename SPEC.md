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
5. `@XLFunction.allowReferences` would default to false. (This is whether the function can receive cell references).
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
@XLNamespace("Ex")
package com.mcleodmoores.excel4j.examples;

import com.mcleodmoores.excel4j.XLNamespace;
```

which for the above example would lead to the functions being registered as `ExMyJavaFunction`.  Note the import after the package declaration.  In the case of a class if would be:

``` java
package com.mcleodmoores.examples;

import com.mcleodmoores.excel4j.*;

@XLNamespace("Ex1")
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

### Simplified RTD functions
It would be nice to have a simplified API to allow basic call-back functionality:
``` java
@XLRealTimeDataFunction(name = "MyRTDFunction", category = StandardCategories.Financial, description = "My RTD Function")
public RTDCallback myRtdFunction() {
  final RTDCallback handler = RTDCallback.Builder.of("MyRTDServerProdId").build(); // uses preset server name.
  JButton swingButton = _button1;
  swingButton.addActionListener(new ActionListener() { // obviously we'd add code so we didn't add this all the time.
    public void actionPerformed(ActionEvent e) {
      handler.getCallback().triggerUpdate(_updateField.getText());
    }
  }
  return handler;
}
```
## Explicit types

The aim here is for the developer to have something to fall back on if the automatic marshalling doesn't work.
### XLValue
This broadly mirrors the XLOPER* data structure.  The interesting question is whether this should be an object heirarchy or a type + object structure.  This could be done thus:
  - `XLString`
  - `XLLocalReference` - sref
    - `XLRange'
  - `XLMultiReference` - ref/mref
    - `XLSheetId`
    - `List<XLRange>`
  - `XLBoolean`
  - `XLNumber`
  - `XLValueRange` - mapped to multi
  - `XLError`
    - Enum
    - We could do with a way of getting error messages back
  - `XLNil` - missing array elements
  - `XLInteger`
  - `XLBigData`
  - Assume we don't need Macro flow control 

# Type conversions
## Direct Java Mode
Going from Excel to Java, we have a set of possible target methods that match the method (or constructor name and class). For each one we have a source type (passed by Excel), and a destination type (specified by the signature of the destination method).  We can then attempt to find a converter capable of converting source -> destination type.
 - When there are multiple matches we choose the one with the higest priority converter.
 - We can't tell what the return conversion should be - in this case the converter needs to tell us
   - If we're using a mode that always returns an object, we just return an XLObject.
   - If we're in an aggressive type conversion mode, we find a converter that can convert java -> excel, but without
     knowing the destination type.  The converter must provide that.

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

> We might be able to avoid the common use of these varients if we can automatically queue recalculations of appropriate cells 
> via COM, but I suspect synchronization issues would cause issues if used with e.g. Solver.  Can Solver work with RTD or Volatile 
> UDFs at all?

# Method dispatch
The simplistic idea is that we (ignoring memory management):
 - generate registered UDF handlers 
 - when a UDF is invoked, we make COM client call
 - marshall the XLOPERs
 - the JVM COM Server invokes a method on a Java handler
 - the handler binds to a method (i.e. looks it up, possibly reflectively, cached presumably)
 - the handler invokes that method
 - the handler returns the result object to the JVM COM Server
 - the COM server marshalls the result into a response
 - the client recieves the response
 - the client converts the reposnse into an XLOPER and returns it to Excel.

But this misses the possibility that the UDF might call back into Excel to get/modify the sheet (I'm taking about Excel4 and Excel12, not COM).  In this case the issue is that the call-back is in a different thread to the caller, which won't work at all.  So it will need to be something more like this:
 - generate regsitered UDF handlers
 - when a UDF is invoked, make a COM client call in a loop

``` java
transaction_id = generateId(); // get a transaction id
// call remote end, in BEGIN phase (i.e. initial method invocation
response = invokeUDF(transaction_id, Phase.BEGIN, params.toXLOPERs());
// while the respose we get is ExcelInvoke, that means the remote end wants 
// to call back Excel4/12 in this thread.
while (response.getResponseType() == ReponseType.ExcelInvoke) {
  Object result = invokeExcel4or12(response.getInvokeParams()); // do the callback
  response = invokeUDF(transaction_id, Phase.RESULT, result); // send the results
} // assume response now == Result
freeId(transaction_id);
return response.getResult().toXLOPER();
```

and on the server end, we have something like:

``` java 
Response invokeUDF(ID transaction_id, Phase phase, params...) {
  // dig up the queue so we can pass it on to the invoking thread and also 
  // post results in the case of a callback.
  SynchronousQueue queue = getQueue(transaction_id);
  if (phase == Phase.BEGIN) {
    Method method = findMethod(params); // get the right wrapper method to call.
    method.asyncInvoke(queue, params); // non-blocking invoke, happens in separate thread.
  } else if (phase == Phase.RESULT) {
    queue.put(queue, params); // post results of Excel4/12 blocking until they're taken
  }
  return queue.take(); // this will wait for either be a ReponseType.Result or ReponseType.ExcelInvoke
}
```

then the actual execution thread might have something like (we might be able to avoid this wrapper by putting it in the 
asynchronous invocation mechanics).

``` java 
void udfWrapperMethod(SynchronousQueue queue, params) {
  setResponseQueueForThread(queue); // stores queue in ThreadLocal
  Object result = method(params); // run the 'user' provided method.
  Reponse = Response.ofType(ReponseType.RESULT).withResult(result); // wrap up it's response, and label it a real result.
}
```

and in `method()` (the user's UDF)

``` java
Object method(XLValue params...) {
  // do some calcs
  Object results = _excel.excel4(SOME_METHOD, 0, 1, 2);
  // some more calcs
  return result;
}
```
and in Excel.excel4()
``` java
Object excel4(Excel4MethodId methodId, Object... params) {
  // pull out the queue associated with this thread.
  SynchronousQueue queue = getReponseQueueForThread();
  // send a request for the caller to call back Excel4/12
  queue.put(Reponse.ofType(ReponseType.ExcelInvoke).withInvokeParams(methodId, params));
  // wait for a reponse.
  Object result = queue.take();
  // return control flow to the UDF.
  return result;
}
```
