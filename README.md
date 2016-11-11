XL4J - Native Excel XLL Add-ins in Java
=======================================

# Introduction
XL4J is a Java and native code library that allows you to build native-quality Excel Add-ins using only standard Java tooling (Maven + JDK).  It lets you write high performance custom Excel functions and commands for end users, but also works as a dynamic rapid application development tool in its own right.

In addition to supporting the standard Excel types (numbers, strings, dates, booleans, etc.) it also supports Java objects in the form
of object handles.  This means you can store any complex object in a single Excel sheet cell, allowing much more complex applications.  A background incremental garbage collector prevents discarded and overwritten objects from hanging around.

# Objectives
 - Make no compromises 
   - allowing developers to access any functionality they would be able to through a pure native XLL project written in C++.  This 
     means you don't have to choose between convenience and power.
 - Make it easy
   - Put your data where your users are using it and hugely increase productivity and reduce development cycles by making it really 
     easy to expose data to users without complex and inflexible UI engineering.  
   - super easy to start development - just annotate a method with @XLFunction and watch XL4J do the rest.
   - super easy deployment - just create a Maven project, include a dependency and maven assembly file and build the Add-in directory
     or deploy to a maven repository.
 - Production-grade
   - make consideration of real-world production usage with XCOPY install, access to logs, pre-deployment configuration, etc.
 - Developer friendly licensing
   - Dual license GPL/Commercial means you can get your feet wet without an up-front commitment and use in personal or 
     open source projects without payment.
   - Each commerical license provides perpetual Add-in distribution and source code license for latest version at time of purchase      (like JetBrains).
   - Per developer-seat licensing, with royalty-free end-user licensing (you pay per developer, not per deployment).

# Features
## Writing Excel user-defined functions
 - System will automatically scan your code for @XLFunction annotations and register them with Excel.
    ```java
     @XLFunction(name = "MyAdd")
     public static double myadd(final double one, final double two) {
       return one + two;
     }
  ```

  ![MyAdd](https://github.com/McLeodMoores/xl4j/blob/master/docs/images/my-add.PNG "MyAdd in use")
   
  See the tutorial for more complex examples that return objects and arrays, include documentation and more.
 - Automatic marshalling (conversion) from Java to Excel types and back again.
   - Primitive types (and Boxed equivalents)
   - JSR-310/Java 8 dates
   - 1D/2D Arrays
   - Full object-handling system maintains a garbage-collected heap for objects, necessary for long running sheets
   - Support for varargs
 - All the types of functions and features normally available to Excel XLLs
   - Volatile functions `@XLFunction(volatile=true)` which are always recalculated (e.g. =TODAY()).
   - Macro-equivalent functions `@XLFunction(isMacroEquivalent=true)` run single-threaded but can call more Excel
     APIs such as those that modify other cells (useful for dumping data onto a sheet without using an array formula).
   - Multi-thread safe functions `@XLFunction(isMultiThreadSafe=true)` which Excel can call from multiple calculation threads.
     This is the default.
   - Asynchronous functions `@XLFunction(isAsynchronous=true)` which enable long running operations to run while Excel continues
     and explicitly notify Excel of a result. **CURRENTLY IN DEVELOPMENT**.
 - Call XLL API from different contexts
   - XLL API calls can be made from the caller's Excel calculation thread or from the Excel main thread depending on context required.  
     Excel documentation specifies that many API calls can only be safely made from the main Excel thread. **CURRENTLY IN DEVELOPMENT**
 
## Calling constructors and methods on arbitrary Java objects
The follwing example allows you to create and show a Swing JFrame with no coding at all:

|   | A                                                      |                          B                        |
|---|:------------------------------------------------------:|:-------------------------------------------------:|
| 1 | `=JConstruct("javax.swing.JFrame", "My Window Title")` | `=JConstruct("javax.swing.JButton", "Click me!")` |
| 2 | `=JMethod(A1, "setSize", 300, 200)`                    |                                                   |
| 3 | `=After(A2, JMethod(A1, "add", B1))`                   |                                                   |
| 4 | `=After(A3, JMethod(A1, "setVisible", TRUE))`          |                                                   |

which looks like this in Excel - note the object handles with the >> prefix followed by the type and the handle number:

![JFrame in Excel](https://github.com/McLeodMoores/xl4j/blob/master/docs/images/jframe-example.PNG "How it looks in Excel")

Breaking this example down:
 - The `JConstruct` function calls the named classes constructor with any supplied arguments and returns an object handle.  The first
   constructor that the system is able to convert the argument list for will be chosen.
 - The `JMethod` function calls a method named in the second argument on the object handle passed in as the first argument with
   any subsequently supplied parameters.  The first method that the system is able to convert the argument list for will be chosen.
 - The `After` function is a utility function that allows you to specify that this cell should be evaluated after another one.  In this
   case it allows us to specify that we want the `add` method called after the `setSize` method, and the `setVisible` method after that.
   If we don't do this, we can find that Excel can choose an ordering we didn't want.  Note this is only really an issue when we're 
   side-effecting a java object, which we should generally avoid anyway.
   
Evaluating the sheet results JFrame appearing:

![JFrame](https://github.com/McLeodMoores/xl4j/blob/master/docs/images/jframe.PNG "The Resulting JFrame")

## Configuration and Easy Logging Access
By default there are toolbar icons for opening the settings dialog, and opening the Java and C++ Logs in Notepad (or default 
`.log` file viewer).  These can be disabled for end users if preferred.
 - Support for custom tool icons on the Add-in ribbon via a super-simple extension to the configuration file. **CURRENTLY IN DEVELOPMENT**
 
## Deployment features
 - Zero-install (a.k.a. XCOPY install) works for non-Adminstrator users who lack permission to install software and 
   allow hosting installation files on a network share.
 - No manually run background server.
 - Installation can be custom configured to hide configuration and developer options from end users.
 - White labelling.
 - In-build access to logs without digging around in Temp directories or CLI windows.


