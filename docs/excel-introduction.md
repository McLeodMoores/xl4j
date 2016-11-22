
# An Excel Primer for Developers
## Document Structure
Excel documents are known as *Workbooks*.  Workbooks are made up of one or more *Sheets* or *Worksheets*, which are the tabs you 
can switch around at the bottom of each window.  Each sheet is made up of a grid of *cells*.  Each cell has a *reference* which can
be used to refer to it.
### Cell References
Cell references come in two flavours
 - A1, where the column is denoted by letters and the row is denoted by a number e.g. D18.
 - R1C1, where the reference refers to row and column by number e.g. R20C18.
Excel uses the R1C1 format internally, and you can choose on the options to view all references in R1C1 format, although most users do
not.

#### Reference behaviour when expanded
Excel has the ability to generalize a cell reference by dragging the corner of a highlighted cell.  This will copy your cell reference 
and modify it.  Typically, the reference will change relative to the source copy cell.  So if we start out with

|   |  A  |  B  |
|---|-----|-----|
| 1 | 123 | =A1 |
| 2 | 456 |     |
| 3 | 789 |     |

and then we pull down the corner of B1 a few rows we'll end up with

|   |  A  |  B  |
|---|-----|-----|
| 1 | 123 | =A1 |
| 2 | 456 | =A2 |
| 3 | 789 | =A3 |

which is great.  Except if we don't want that behaviour.  What if we want the reference to be anchored?  In this case we use a dollar
in the reference to anchor the reference to the cell:

|   |  A  |   B   |
|---|-----|-------|
| 1 | 123 | =$A$1 |
| 2 | 456 |       |
| 3 | 789 |       |

now becomes

|   |  A  |   B   |
|---|-----|-------|
| 1 | 123 | =$A$1 |
| 2 | 456 | =$A$1 |
| 3 | 789 | =$A$1 |

Note that we can anchor one dimension and not the other, so $A1 will always refer to column A if it's expanded horizontally, but it's 
row will change relative to the original.  Similarly, we can anchor the row A$1 will always refer to row 1, but change the column 
relative the original.  A useful keyboard shortcut is that F4 will cycle between the 4 different variants, which can save you a lot of
re-typing.

Interestingly, in R1C1 format, relative references are expressed as R[*&lt;offset&gt;*]C[*&lt;offset&gt;*] where if either the R or C offset 
is optional, so R[-1]C refers to the cell above the current cell.  This can be more readable for complex sheets, so you might want to
use it sometimes and also if you're generating sheets, it can be simpler.

## Excel Cell Types
| Excel Type       | Notes/Values |
|------------------|--------------|
| Number           | All floating point, note that it's not IEEE-compliant.  In particular NaN -> #NUM!, Inf -> #NUM! and subnormals are truncated to 0.  The range is 10<sup>307</sup> <= abs(x) < 10<sup>308</sup> |
| Boolean          | TRUE or FALSE |
| String (Unicode) | 2<sup>15</sup>-1 unicode characters (UTF-16?).  Only displayed if value >= 32. |
| Errors | #NULL!, #DIV/0!, #VALUE!, #REF!, #NAME?, #NUM!, #N/A |
| Arrays | One and two dimensional array of mixed type objects.  Literals are encoded using curly brackets row by row, with commas separating objects, with semi-colons separating rows. |

### Pseudotypes
Dates and times are represented just as numbers, specifically fractions of a day since the epoch (which is not the standard epoch).  Their display is purely a formatting filter.

Percentages too are represented as numbers and their display is just a * 100 formatting filter.  The % symbol is just a unary suffix operator that divides by 100.

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
Highest precendence operator so binds tightly to the operand to its left.  Will try and convert anything to a Number, so can be applied to dates, times and Booleans as well.
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
Scalar operations on arrays are treated as matrix-style operations where each element is operated on separately.  Note here that the formulas in B1:B5 are an array formula rather than multiple single formulas.

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
There are a number of functions available to force type conversions

| Function Name | Return Type | Number | String | Boolean | Error |
|---------------|-------------|--------|--------|---------|-------|
| N()           | Number/Err  | arg    | 0      | 1/0     | arg   |
| T()           | String/Err  | ""     | arg    | ""      | arg   |
| TEXT()        | String/Err  | String rep. of arg | convert to String and back, #VALUE! if fails | "TRUE" or "FALSE" | arg |
| VALUE()       | Number/Err  | arg    | convert to Number, #VALUE! if fails | #VALUE! | arg |
