Hints and tips on writing add-ins
=================================

# Learn how to use array formulas
Some of the most useful functions returns structured data that should be spread over a grid of cells.  Array formulas refers to
user-defined functions that return either one or two dimensional arrays (or `XLArray` directly).  To use array formulas you highlight
the area into which you want to write the results, click in the formula bar (or press F2), type your formula e.g. 
`=ExpandTabularResult(A1)` and rather than pressing ENTER, press CTRL-SHIFT-ENTER.  ENTER can be RETURN rather than the numpad ENTER 
key.  If you press ENTER by mistake or want to change a scalar formula into an array formula, you can just highlight the area again, hit
F2, which will edit the formula in the top left cell and hit CTRL-SHIFT-ENTER.

## Increasing the area covered by an array formula
In this case, you simply highlight the new area (which must completely contain the existing area), hit F2 or click in the formula bar 
to edit the formula, and hit CTRL-SHIFT-ENTER.

## Decreasing the area covered by an array forumla
This is more tricky.  If you do the obvious thing and try and delete part of the area, or try select a reduced area and hit 
CTRL-SHIFT-ENTER you will encounter the annoying error: "You cannot change part of an array".  What you need to do is highlight the
existing area and hit CTRL-ENTER.  This converts the elements to normal formulas.  You can then highlight the new area (which can be
smaller) and hit CTRL-SHIFT-ENTER.

## Alternatives to array formulas
Before using array formulas with abandon though, you should consider whether your users are familiar with them.  Some users find
using array formulas very annoying, or, more likely, are completely unfamiliar with them.  You should either plan to survey or 
train users before moving forward.

An alternative is to use indexed accessor functions.  These pass 
