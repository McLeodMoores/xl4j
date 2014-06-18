excel4j
=======

# General Notes
## Excel Types
| Excel Type       | Notes/Values |
|------------------|--------------|
| Number           | All floating point, note that it's not IEE-compliant.  In particular NaN -> #NUM!, Inf -> #NUM! and subnormals are truncated to 0.  The range is 10<sup>307</sup> <= abs(x) < 10<sup>308</sup> |
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

1. Evaluate function arguments from most nested outwards.  Cell references and ranges are converted to values, which may then be converted to the expected data types if necessary.  If a name is not identifiable as a function or defined name (named range or cell), then it will be replaced with #NAME? and the evaluation will fail.
2. If the value has changed, any dependent inputs will be recalculated. **WE WILL NEED TO TAKE THIS INTO ACCOUNT WHEN USING OBJECT HANDLES**
3. Circular references are checked and cells may be resized.

# Specification

## Primitive Types
### 
