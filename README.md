XL4J - Excel Add-ins for Java
=============================

# Introduction
XL4J is a Java and native code library that allows you to build native-quality Excel Add-ins using only standard Java tooling (Maven + JDK).  It lets you write might performance custom Excel functions and commands for end users, but also to work as a dynamic rapid application development tool in it's own right.

In addition to supporting the standard Excel types (numbers, stings, dates, booleans, etc) it also supports Java objects in the form
of object handles.  This means you can store any complex object in a single Excel sheet cell, allowing much more complex applications.  A background incremental garbage collector prevents discarded and overwritten objects from hanging around.

# Objectives
 - Make no comprimises 
   - allowing developers to access any functionality they would be able to through a pure native XLL project written in C++.  This 
     means you don't have to choose between convenience and power.
 - Make it easy
   - Put your data where you users are using it and hugely increase productivity and reduce development cycles by making it really 
     easy to expose data to users without complex and inflexible UI engineering.  
   - super easy to start development - just annotate a method with @XLFunction and watch XL4J do the rest.
   - super easy deployment - just create a Maven project, include a dependency and maven assembly file and build the Add-in directory
     or deploy to a maven repository.
 - Production-grade
   - make consideration of real-world production usage with XCOPY install, access to logs, pre-deployment configuration, etc.
 - Developer friendly licensing

- Dual license GPL/Commerical means you can get you feet wet without an up-front commitment and use in personal or 
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
   
 - Automatic marshalling (conversion) from Java to Excel types and back again.
   - Primitive types (and Boxed equivalents)
   - JSR-310/Java 8 dates
   - 1D/2D Arrays
   - Full object handling system maintains a garbage collected heap for objects, necessary for long running sheets
   - Support for varargs
 - Ability to create and call methods on arbitrary java objects from Excel with no code changes:
 
   |   | A                                                      |                          B                        |
   |---|:------------------------------------------------------:|:-------------------------------------------------:|
   | 1 | `=JConstruct("javax.swing.JFrame", "My Window Title")` | `=JConstruct("javax.swing.JButton", "Click me!")` |
   | 2 | `=JMethod(A1, "setSize", 300, 200)`                    |                                                   |
   | 3 | `=After(A2, JMethod(A1, "add", B1))`                   |                                                   |
   | 4 | `=After(A3, JMethod(A1, "setVisible", TRUE))`          |                                                   |
   
   results in a JFrame appearing:
   
   ![JFrame](https://github.com/McLeodMoores/xl4j/blob/master/docs/images/jframe.PNG "The Resulting JFrame")

## Deployment features
 - Zero-install (a.k.a. XCOPY install) works for non-Adminstrator users who lack permission to install software and 
   allow hosting installation files on a network share.
 - No manually run background server.
 - Installation can be custom configured to hide configuration and developer options from end users.
 - White labelling.
 - In-build access to logs without digging around in Temp directories or CLI windows.
   

# Two distinct modes of use

1. Interacting with existing Java code and libraries directly from Excel
  - Creating object by calling constructors, factory methods, etc.
  - Invoking methods
  - Marshall results too and from Excel types where possible, but always have a fallback to explcitily typed objects
2. Writing and exposing functions specifically designed for Excel.  These would share the same type system but have
   more refined argument lists, accept ranges, arrays and so on as necessary.
  - Support for basic non-blocking, fast UDF invocation/calculation.
  - Support for slower calculations by using
    - Asynchronous UDFs
    - Other tricks?  Timeouts, retries, RTD functions?
  - Support for RTD servers.


