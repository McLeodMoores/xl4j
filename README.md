excel4j
=======

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
|   |  A |     B      | 
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

|   |  A |       B      | 
|---|----|--------------|
| 1 |  2 | {=$A$2:$A$5} |
| 2 |  4 | {=$A$2:$A$5} |
| 3 |  8 | {=$A$2:$A$5} |
| 4 | 16 | {=$A$2:$A$5} |
| 5 | 32 | {=$A$2:$A$5} |

yields

|   |  A |    B    | 
|---|----|---------|
| 1 |  2 |       4 |
| 2 |  4 |       8 |
| 3 |  8 |      16 |
| 4 | 16 |      32 |
| 5 | 32 | #VALUE! |

# Specification

## Primitive Types
### 
