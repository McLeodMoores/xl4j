Debugging Functions
===================
As anyone who as spent time in Excel knows, it can sometimes be difficult to work out why formulas returns errors. This problem can be magnified if an add-in is used. Fortunately, both the nature of Java and the XL4J add-in itself have features that can make debugging easier. This document attempts to outline the most common problems and suggests ways to track down any issues and resolve them.

# Examples used in this document
To show some trouble-shooting methods, we start with some functions that are extensions of the [Schedule](https://github.com/McLeodMoores/xl4j/blob/master/xll-examples/src/main/java/com/mcleodmoores/xl4j/examples/timeseries/Schedule.java) class that is available in the `xll-examples` project.

We have added a simple marker interface:

``` java
/**
 * Marker interface for functions used in the debugging documentation.
 */
@XLNamespace("Schedule.")
@XLFunctions(
    typeConversionMode = TypeConversionMode.OBJECT_RESULT,
    category = "Schedule")
public interface ScheduleFunctionV1 extends BiFunction<LocalDate, LocalDate, Schedule> {

}
```
and added forward and reverse quarterly schedule generation implementations:
``` java
/**
 * Generates a quarterly schedule from the start date to end date inclusive.
 */
public class ForwardQuarterlyScheduleFunction implements ScheduleFunctionV1 {

  /**
   * Restricted constructor for registration example.
   */
  ForwardQuarterlyScheduleFunction() {
  }

  @Override
  public Schedule apply(final LocalDate start, final LocalDate endInclusive) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(endInclusive, "endInclusive");
    LocalDate date = start;
    final Schedule result = new Schedule();
    int i = 0;
    while (!date.isAfter(endInclusive)) {
      result.add(date);
      // offset from start to avoid end-of-month effects e.g. don't want 31 Nov -> 28 Feb -> 28 May
      i += 3;
      date = start.plusMonths(i);
    }
    return result;
  }
}
```

``` java
/**
 * Generates a quarterly schedule from the end date to start date inclusive. The series is returned
 * with increasing dates.
 */
public class ReverseQuarterlyScheduleFunction implements ScheduleFunctionV1 {

  @Override
  public Schedule apply(final LocalDate start, final LocalDate endInclusive) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(endInclusive, "endInclusive");
    LocalDate date = endInclusive;
    final Schedule result = new Schedule();
    int i = 0;
    while (!date.isBefore(start)) {
      result.add(date);
      i += 3;
      // offset from start to avoid end-of-month effects e.g. don't want 30 Nov <- 28 Feb <- 31 May
      date = endInclusive.minusMonths(i);
    }
    Collections.reverse(result);
    return result;
  }

```
These classes can be found on the `examples-for-docs` branch in the `xll-examples` project.

# The function is missing
After the plugin has been built with these new classes, the next step is to try to use them. However, we try to use the reverse quarterly function in Excel, the function name doesn't appear in the suggestions:
![First image](https://github.com/McLeodMoores/xl4j/blob/master/docs/images/missing-function-1.png)
The first thing to check is that the add-in has installed successfully by looking at the tabs at the top **TODO what are these called**:
![Second image](https://github.com/McLeodMoores/xl4j/blob/master/docs/images/missing-function-2.png)
There's no `Add-ins` tab, which means that the add-in hasn't been added. Once we've fixed this, we try again and see:
![Third image](https://github.com/McLeodMoores/xl4j/blob/master/docs/images/missing-function-3.png)

Although other schedule functions have been registered, we still can't see either of our functions. The first thing to do is to check that the functions were registered on the Java side. We go to the `Add-ins` tab and open the Java log
![Fourth image](https://github.com/McLeodMoores/xl4j/blob/master/docs/images/missing-function-4.png)
![Fifth image](https://github.com/McLeodMoores/xl4j/blob/master/docs/images/missing-function-5.png)
Note that the logging level is `INFO` - if the logging level is set to be higher than this, then the settings must be changed and Excel restarted **TODO link to settings doc**.

## No annotation
## Function is not visible

# The function cannot be called successfully
## Calling an instance method
## Wrong number of arguments

# The function does not complete as expected
## Logging output
## Attaching a debugger (Eclipse)
## Attaching a debugger (IntelliJ)


