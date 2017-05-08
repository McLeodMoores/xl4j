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

An alternative is to creat some indexed accessor functions.  With this you return an object handle rather than an array, and then
pass that into another function to pick out a particular element.  An example is `JSONArray.Get` implemented by the `get(JSONArray, int)`
method in [com.mcleodmoores.xl4j.examples.rest.JsonFunctions](https://github.com/McLeodMoores/xl4j/blob/master/xll-examples/src/main/java/com/mcleodmoores/xl4j/examples/rest/JsonFunctions.java).  

|   |            A            |                     B                   | C |
|---|-------------------------|-----------------------------------------|---|
| 1 | =CreateJsonObject()     |                                         |   |
| 2 | *Using hardcoded index* | *Using computed index (easy to copy)*   |   |
| 3 | =JSONArray.Get($A$1, 1) | =JSONArray.Get($A$1, ROW() - ROW($A$2)) |   |
| 4 | =JSONArray.Get($A$1, 2) | =JSONArray.Get($A$1, ROW() - ROW($A$2)) |   |
| 5 | =JSONArray.Get($A$1, 3) | =JSONArray.Get($A$1, ROW() - ROW($A$2)) |   |

The `=CreateJsonObject` isn't a real function, just a placeholder for something that would return a JSON object like `ResponseJSON`.
Also, in this case we're using a 1-based index so the subtract row number (`ROW($A$2)`) is the header rather than the first value, which
is what you'd use for zero-based indices.  Obviously type of accessor function can be done in two or more dimensions as well.

A future intention is to implement functionality that allows you to write function results into the cells below the formula without
either array formulas or accessor functions.

# Minimise the use of volatile functions if any of your functions are doing intensive calculations or I/O
If you have a calculation chain (the chain of dependencies between cells) that contains a function marked 'volatile', the chain will
be constantly re-evaluated.  If any of your functions are even slightly slow, the whole of Excel will slow to a crawl and quite
probably intermittantly lock up.  Often the culprit is an `=TODAY()` or `=NOW()` function in a cell.

# Be aware that Excel often recalculates very aggressively
Excel will often re-compute cells that logically really don't need to be recalculated multiple times.  Long running functions can
therefore end up appearing to take longer than expected as they are actually run several times before appearing.  One way to mitigate
this issue is to cache results when a function is called with the same arguments, essentially memoization.  This obviously requires 
the function is idempotent (that it has no hidden state that affects its results).  For an example of this, using a rather crude 
hand-rolled cache, see the Quandl example.

# Excel being multi-threaded now doesn't necessarily mean what you think it does
You might this that because Excel does multi-threaded recalculation, it can calculate cells in the background without blocking.  This
is not true.  Any of the calculation threads blocking can cause the UI to freeze up.  For the best results, use asynchronous functions
for any I/O or long running calculatoins.

XL4J uses two separate thread pools, one for normal synchronous function calls that has a one to one

# Asynchronous functions get cancelled and recalculated often
Excel often calls the add-in with an asynchronous function call, only to trigger the calculation cancellation event shortly afterwards
(which cancels all outstanding calculations).  What currently happens in this case is:

  1) The current asynchronous thread pool is flagged for shutdown, existing calls continue until finished but then will shut down and
     any threads will terminate.  We might one day put these threads in a java `ThreadGroup` and `interrupt()` the group as this will
     interrupt some operations (but not others).  This isn't implemented currently though.
  2) A new asynchronous thread pool is created running the next wave of asynchronous calls.

This means your asynchronous thread-pool is not held hostage by cancelled functions.  We effectively let them do their thing and shut
them down.  Using a fixed thread pool is important though as Excel can easily create hundreds of concurrent threads if the pool is
unbounded, and will eventually exhaust the machine's resources.
  
