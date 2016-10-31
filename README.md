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
   - Each commerical license provides perpetual Add-in distribution and source code license for latest version at time of purchase 
     (like JetBrains).
   - Per developer-seat licensing, with royalty-free end-user licensing (you pay per developer, not per deployment).

# Features
## Writing Excel user-defined functions
 - System will automatically scan your code for @XLFunction annotations and register them with Excel.
 - Automatic marshalling (conversion) from Java to Excel types and back again.
   - Primitive types (and Boxed equivalents)
   - JSR-310/Java 8 dates
   - 1D/2D Arrays
   - Full object handling system maintains a garbage collected heap for objects, necessary for long running sheets
   - Support for varargs
 - Ability to create and call methods on arbitrary java objects from Excel with no code changes:
 
   |   |                             A                          |     B      |
   |---| ------------------------------------------------------ |:----------:|
   | 1 | `=JConstruct("javax.swing.JFrame", "My Window Title")` |            |
   | 2 | `=JMethod(A1, "setSize", 400, 300)`                    |            |
   | 3 | `=JMethod(A1, "setVisible", TRUE)`                     |            |

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

# General Notes

## Excel Types
| Excel Type       | Notes/Values |
|------------------|--------------|
| Number           | All floating point, note that it's not IEEE-compliant.  In particular NaN -> #NUM!, Inf -> #NUM! and subnormals are truncated to 0.  The range is 10<sup>307</sup> <= abs(x) < 10<sup>308</sup> |
| Boolean          | TRUE or FALSE |
| String (Unicode) | 2<sup>15</sup>-1 unicode characters (UTF-16?).  Only displayed if value >= 32. |
| Errors | #NULL!, #DIV/0!, #VALUE!, #REF!, #NAME?, #NUM!, #N/A |
| Arrays | One and two dimensional array of mixed type objects.  Literals are encoded using curly brackets row by row, with commas separating objects, with semi-colons separating rows. |

### Pseudo types
Dates and times are represented just as numbers, specifically fractions of a day since the epoch (which is not the standard epoch).  Their display is purely a formatting filter.

Percentages to are represented as numbers and their display is just a * 100 formatting filter.  The % symbol is just a unary suffix operator that divides by 100.

## Input evaluation
Chains of evaluation

1. If string prefix (single quote) => String
2. If prefixed with plus, minus or equals => Formula
3. See if the value looks like a date, time, currency amount, percentage or number
 
For formulas, the process is then

1. Evaluate function arguments from most nested outwards.  Cell references and ranges are converted to values (unless the function in question expects a reference), which may then be converted to the expected data types if necessary.  If a name is not identifiable as a function or defined name (named range or cell), then it will be replaced with #NAME? and the evaluation will fail.
2. If the _value has changed_, any dependent inputs will be recalculated. **WE WILL NEED TO TAKE THIS INTO ACCOUNT WHEN USING OBJECT HANDLES**
3. Circular references are checked and cells may be resized.
 
## Type conversion at the Excel level
Conversions take place as operators are applied to values:
### The equals operator
Will convert any cell references into _values_ before invoking functions and will only return one of the basic Excel types listed above
### Unary minus
Will convert a string representation of a number to a negated number representation, so double negation converts from String to Number.  Booleans convert to -1 or 0, so an easy Boolean to Number conversion is achieved with double negation. Note that the unary plus operator does not have the same effect.
### Binary arithmetic operators (`+`, `-`, `*`, `/`, `^`)
Will try to convert any values to Numbers.  This includes strings in any recognised format, dates and times and percentages.
### Percentage operators (`%`)
Higest precendence operator so binds tightly to the operand to it's left.  Will try and convert anything to a Number, so can be applied to dates, times and Booleans as well.
### String contatenation operator (`&`)
Convert numbers to strings in a default number format unrelated to display format.
### Binary Boolean comparison operators (`=`, `<`, `>`, `<=`, `>=`, `<>`)
Acting on String the comparisons are *case insensitive*.  Internally everything is converted to lower case before comparison. No other conversions are done for these operators.  This means you can't compare string and number representations and expect equality or reasonable comparisons.
### Binding to functions
Conversions also take place when binding to function parameters.  Excel will try to convert to the expected type.

## Ranges and Arrays
### Ranges
Ranges are treated quite differently from arrays and can have some odd properties when evaluated in a scalar context.

| . |  A |     B      | 
|---|----|------------|
| 1 |  2 | =$A$2:$A$5 |
| 2 |  4 | =$A$2:$A$5 |
| 3 |  8 | =$A$2:$A$5 |
| 4 | 16 | =$A$2:$A$5 |
| 5 | 32 | =$A$2:$A$5 |

yields the values 

|   |  A |     B    | 
|---|----|----------|
| 1 |  2 |  #VALUE! |
| 2 |  4 |        4 |
| 3 |  8 |        8 |
| 4 | 16 |       16 |
| 5 | 32 |       32 |

where each the range is converted into a scalar in a different way for each context using the *current* row/column relative to the range.  Note that if the range does not overlap the current column, the result will be `#VALUE!`

### Arrays
Scalar operations on arrays are treated as matrix style operations where each element is operated on separately.  Note here that the formulas in B1:B5 are an array formula rather than multiple single formulas.

|   |  A |        B       | 
|---|----|----------------|
| 1 |  2 | \{=$A$2:$A$5\} |
| 2 |  4 | \{=$A$2:$A$5\} |
| 3 |  8 | \{=$A$2:$A$5\} |
| 4 | 16 | \{=$A$2:$A$5\} |
| 5 | 32 | \{=$A$2:$A$5\} |

yields

|   |  A |    B    | 
|---|----|---------|
| 1 |  2 |       4 |
| 2 |  4 |       8 |
| 3 |  8 |      16 |
| 4 | 16 |      32 |
| 5 | 32 | #VALUE! |

### Explicit type conversions
There are a number of funcitons available to force type conversions

| Function Name | Return Type | Number | String | Boolean | Error |
|---------------|-------------|--------|--------|---------|-------|
| N()           | Number/Err  | arg    | 0      | 1/0     | arg   |
| T()           | String/Err  | ""     | arg    | ""      | arg   |
| TEXT()        | String/Err  | String rep. of arg | convert to String and back, #VALUE! if fails | "TRUE" or "FALSE" | arg |
| VALUE()       | Number/Err  | arg    | convert to Number, #VALUE! if fails | #VALUE! | arg |

# Specification

## Primitive Types
### 
