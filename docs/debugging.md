Debugging Functions
===================
As anyone who as spent time in Excel knows, it can sometimes be difficult to work out why a formula returns an error. This problem can be magnified if an add-in is used. Fortunately, both the nature of Java and the XL4J add-in itself have features that can make debugging easier. This document attempts to outline the most common problems and suggests ways to track down any issues and resolve them.

## Examples used in this document
To show some trouble-shooting methods, we start with some functions that are extensions of the [Schedule](https://github.com/McLeodMoores/xl4j/blob/master/xll-examples/src/main/java/com/mcleodmoores/xl4j/examples/timeseries/Schedule.java) class that is available in the `xll-examples` project.

We have added a simple marker interface:

``` java
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
}
```
These classes can be found on the `examples-for-docs` branch in the `xll-examples` project.

### General note about errors
It can sometimes be hard to work out if an error is coming from Excel or the add-in itself. It's worth noting that errors returned from XL4J are the `#NULL!` error type. Excel only returns this type of error "when you refer to an intersection of two ranges that do not intersect". As a general rule of thumb, therefore, any errors other than `#NULL!` are because there's an error in the formula, division by zero, etc., and `#NULL!` is an error from XL4J if there are no ranges referenced in the formula.


## \#NULL!

### The function is missing
After the plugin has been built with these new classes, the next step is to try to use them. However, when we try to use the reverse quarterly function in Excel, the function name doesn't appear in the suggestions:

![Missing function 1](images/missing-function-1.png)

The first thing to check is that the add-in has installed successfully by looking at the tabs:

![Missing function 2](images/missing-function-2.png)

There's no `Add-ins` tab, which means that the add-in hasn't been registered. Once we've fixed this, we try again and see:

![Missing function 3](images/missing-function-3.png)

Although other schedule functions have been registered, we still can't see either of our functions. The first thing to do is to check that the functions were registered on the Java side. We go to the `Add-ins` tab and open the Java log (note that the logging level is `INFO` - if the logging level is set to be higher than this, then the [settings must be changed](settings.md) and Excel restarted. There is a section that shows the names of the functions that have been registered (shown with some registrations removed):

![Missing function 4](images/missing-function-4.png)

As expected, there is no entry for either of our functions. Looking further in the log, there is an error message warning us that we cannot mark the interface with an `@XLFunctions` annotation:

![Missing function 5](images/missing-function-5.png)

To fix this problem, we remove the annotation on `ScheduleFunctionV1` and move it to both functions:

``` java
public interface ScheduleFunctionV2 extends BiFunction<LocalDate, LocalDate, Schedule> {
}
```
``` java
@XLNamespace("Schedule.")
@XLFunctions(
    prefix = "ForwardQuarterly",
    typeConversionMode = TypeConversionMode.OBJECT_RESULT,
    description = "Generates a quarterly schedule from the start to end date",
    category = "Schedule")
public class ForwardQuarterlyScheduleFunctionV2 implements ScheduleFunctionV2 {

  /**
   * Default constructor.
   */
  ForwardQuarterlyScheduleFunctionV2() {
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
@XLNamespace("Schedule.")
@XLFunctions(
    prefix = "ReverseQuarterly",
    typeConversionMode = TypeConversionMode.OBJECT_RESULT,
    description = "Generates a quarterly schedule from the end to start date",
    category = "Schedule")
public class ReverseQuarterlyScheduleFunctionV2 implements ScheduleFunctionV2 {

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
}
```
After rebuilding and reinstalling, we can create the reverse quarterly schedule calculator:

![Fixed function 1](images/fixed-function-1.png)

However, the forward calculator is still not available:

![Missing function 6](images/missing-function-6.png)

Going back to the log, we see that there are entries for the `apply()` and `andThen()` methods, but not the constructor.

![Missing function 7](images/missing-function-7.png)

Only `public` classes, constructors or methods can be registered as functions. Once the constructor visibility is changed, the forward quarterly calculator can also be created in Excel.

### The function name seems wrong

We've added another function that generates a forward schedule with a user-supplied month interval:

``` java
@XLNamespace("Schedule.")
@XLFunctions(
    prefix = "ForwardNMonths",
    typeConversionMode = TypeConversionMode.OBJECT_RESULT,
    description = "Generates a schedule from the start to end date",
    category = "Schedule")
public class ForwardNMonthsScheduleFunctionV1 implements ScheduleFunctionV2 {
  private final int _n;

  public ForwardNMonthsScheduleFunctionV1(final int n) {
    _n = n;
  }

  @XLFunction(
      name = "GenerateForwardNMonthsSchedule",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT,
      description = "Generates a schedule from the start to end date",
      category = "Schedule")
  @Override
  public Schedule apply(final LocalDate start, final LocalDate endInclusive) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(endInclusive, "endInclusive");
    ArgumentChecker.isTrue(_n > 0, "The interval must be greater than zero: have {}", _n);
    LocalDate date = start;
    final Schedule result = new Schedule();
    int i = 0;
    while (!date.isAfter(endInclusive)) {
      result.add(date);
      // offset from start to avoid end-of-month effects e.g. don't want 31 Nov -> 28 Feb -> 28 May
      i += _n;
      date = start.plusMonths(i);
    }
    return result;
  }
}
```
However, when we try to call the `Schedule.ForwardNMonths.apply` function, it does not appear in the list of available functions.

![Missing function 8](images/missing-function-8.png)

This is because the `@XLFunction` annotation takes precedence over `@XLFunctions`, which prevents the same method from being registered twice. The `apply` method has been registered as `Schedule.GenerateForwardNMonthsSchedule`. Using this function name, the schedule can be generated.

![Fixed function 2](images/fixed-function-2.png)

![Successful call 1](images/successful-call-1.png)

As we don't intend to call the `andThen` method for this calculator, we've removed the class-level annotation and added an annotation for the constructor (very important: the object can't be constructed otherwise):

``` java
@XLNamespace("Schedule.")
public class ForwardNMonthsScheduleFunctionV2 implements ScheduleFunctionV2 {
  private final int _n;

  @XLFunction(
      name = "ForwardNMonths",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT,
      description = "Generates a schedule from the start to end date",
      category = "Schedule")
  public ForwardNMonthsScheduleFunctionV2(
      @XLParameter(name = "Number of months") final int n) {
    _n = n;
  }

  @XLFunction(
      name = "GenerateForwardNMonthsSchedule",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT,
      description = "Generates a schedule from the start to end date",
      category = "Schedule")
  @Override
  public Schedule apply(final LocalDate start, final LocalDate endInclusive) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(endInclusive, "endInclusive");
    ArgumentChecker.isTrue(_n > 0, "The interval must be greater than zero: have {}", _n);
    LocalDate date = start;
    final Schedule result = new Schedule();
    int i = 0;
    while (!date.isAfter(endInclusive)) {
      result.add(date);
      // offset from start to avoid end-of-month effects e.g. don't want 31 Nov -> 28 Feb -> 28 May
      i += _n;
      date = start.plusMonths(i);
    }
    return result;
  }
}
```
![Successful call 2](images/successful-call-2.png)

### The function cannot be called successfully

This section uses a class that performs simple adjustments on a `Schedule` that contains a static factory method:

``` java
@XLNamespace(value = "Schedule.")
public final class ScheduleAdjusterV1 {

  @XLFunction(
      name = "ScheduleAdjuster",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT)
  public static ScheduleAdjuster of(
      @XLParameter(name = "schedule") final Schedule schedule) {
    return new ScheduleAdjuster(schedule);
  }

  private final Schedule _schedule;

  private ScheduleAdjuster(final Schedule schedule) {
    _schedule = ArgumentChecker.notNull(schedule, "schedule");
  }
  
    @XLFunction(
      name = "WithDayOffset",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT)
  public Schedule withDayOffset(
      @XLParameter(name = "days") final int days) {
    final Schedule adjusted = new Schedule();
    for (final LocalDate date : _schedule) {
      adjusted.add(date.plusDays(days));
    }
    return adjusted;
  }

  @XLFunction(
      name = "WithWeekOffset")
  public Schedule withWeekOffset(
      @XLParameter(name = "weeks") final int weeks) {
    final Schedule adjusted = new Schedule();
    for (final LocalDate date : _schedule) {
      adjusted.add(date.plusDays(weeks * 7));
    }
    return adjusted;
  }

  @XLFunction(
      name = "WithMonthOffset",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT)
  public Schedule withMonthOffset(
      @XLParameter(name = "months") final int months,
      @XLParameter(name = "daysPerMonth", optional = true) final Integer daysPerMonth) {
    ArgumentChecker.notNegative(daysPerMonth, "The number of days per month cannot be negative");
    final Schedule adjusted = new Schedule();
    if (daysPerMonth != null) {
      for (final LocalDate date : _schedule) {
        adjusted.add(date.plusDays(daysPerMonth * months));
      }
    } else {
      for (final LocalDate date : _schedule) {
        adjusted.add(date.plusMonths(months));
      }
    }
    return adjusted;
  }

  @XLFunction(
      name = "IntersectTimeSeries",
      typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
  public TimeSeries intersectTimeSeries(
      @XLParameter(name = "ts") final TimeSeries ts) {
    final TimeSeries result = TimeSeries.newTimeSeries();
    for (final LocalDate date : _schedule) {
      final double value = ts.get(date);
      result.put(date, value);
    }
    return result;
  }

```
and has mutator methods that offset the dates in the schedule by a fixed amount, and a method that performs an intersection with a time series.

### Calling an instance method

The adjuster is created:

![Static call 1](images/static-call-1.png)

and we unsuccessfully try to adjust the schedule by a 4 day offset:

![Failed instance call 1](images/failed-instance-call-1.png)

The last error message in the log does not give many clues:

![Failed instance call 2](images/failed-instance-call-2.png)

but the lines above show what the problem is. The arguments to the function are `XLNumber` and `XLMissing`. For instance methods, the first argument to the Excel function must be the object that the method is being called on. The call handler is expecting an `XLObject` (the schedule adjuster) and `XLNumber` (the number of days to adjust by). Once the schedule adjuster is passed into the function as the first argument, the function completes successfully:

![Successful instance call 1](images/successful-instance-call-1.png)

### Too few arguments

We have already seen the effect of inadvertently supplying too few arguments when calling an instance function:

![Too few arguments 1](images/too-few-arguments-1.png)

and there is a message in the log showing the arguments that were supplied and the number that were expected:

![Too few arguments 2](images/too-few-arguments-2.png)

Unless some or all of the arguments to a method or constructor are `Optional` (** TODO link to section**), an argument of `XLMissing` shows that the wrong number of arguments were provided.

### Wrong argument type

The ![argument converters](https://github.com/McLeodMoores/xl4j/blob/master/xll-core/src/main/java/com/mcleodmoores/xl4j/v1/typeconvert/converters/) available in the `xll-core` project can handle most of the expected types to and from Excel types (e.g. `XLNumber`, `XLString`, etc.). However, especially for functions with many arguments, it can be easy to get the arguments in the wrong order or provide the wrong type.

In an extremely artifical example, we supply a string where an integer (`XLString` -> `int` instead of `XLNumber` -> `int`) is expected:

![Wrong argument type 1](images/wrong-argument-type-1.png)

In the majority of cases, an exception will be thrown in the type converter that was registered for the function:

![Wrong argument type 2](images/wrong-argument-type-2.png)

The solution to this is to either fix the function call by supplying an argument of the expected type, or changing the function and rebuilding.

### Optional arguments

Optional arguments to XL4J functions are provided in the same way as for other Excel functions. If the optional argument is in the middle of a list of arguments, the value can be left empty (e.g. `=FUNC(A1,A2,,,A3)`). If the last argument is optional, then either `=FUNC(A1,A2,)` or `=FUNC(A1,A2)` will work.

The `withMonthOffset` function in our schedule adjuster takes an optional argument (the number of days per month). This function works if we provide a value for this field:

![Successful optional argument 1](images/successful-optional-argument-1.png)

but not if it's left out:

![Failed optional argument 1](images/failed-optional-argument-1.png)

Looking in the log, we can see the issue. 

![Failed optional argument 2](images/failed-optional-argument-2.png)

Missing values are passed into Java methods / constructors as `null`. However, this value is dereferenced before any check for null. If we change the method to check the value of `daysPerMonth` only after a `null` check:

``` java
  @XLFunction(
      name = "WithMonthOffset",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT)
  public Schedule withMonthOffset(
      @XLParameter(name = "months") final int months,
      @XLParameter(name = "daysPerMonth", optional = true) final Integer daysPerMonth) {
    final Schedule adjusted = new Schedule();
    if (daysPerMonth != null) {
      ArgumentChecker.notNegative(daysPerMonth, "The number of days per month cannot be negative");
      for (final LocalDate date : _schedule) {
        adjusted.add(date.plusDays(daysPerMonth * months));
      }
    } else {
      for (final LocalDate date : _schedule) {
        adjusted.add(date.plusMonths(months));
      }
    }
    return adjusted;
  }
```
then the function returns a schedule:

![Successful optional argument 2](images/successful-optional-argument-2.png)

In this case, `null` was allowed as an input to the method because the type was `Integer`. If this is replaced by `int`, an exception is thrown before the method is called:

![Failed optional argument 3](images/failed-optional-argument-3.png)


## \#VALUE!

### Too many arguments

If a function is called with too many arguments:

![Too many arguments 1](images/too-many-arguments-1.png)

then Excel itself will return a `#VALUE!` error:

![Too many arguments 2](images/too-many-arguments-2.png)

According to the Excel help page, this error means "There's something wrong with the way your formula is typed." Unfortunately, there is no way to get a more detailed error for these type of problems.


## Unexpected value returned to Excel

# The function does not complete as expected
## Logging output
## Attaching a debugger (Eclipse)
## Attaching a debugger (IntelliJ)


Debugging Functions
===================
As anyone who as spent time in Excel knows, it can sometimes be difficult to work out why a formula returns an error. This problem can be magnified if an add-in is used. Fortunately, both the nature of Java and the XL4J add-in itself have features that can make debugging easier. This document attempts to outline the most common problems and suggests ways to track down any issues and resolve them.

## Examples used in this document
To show some trouble-shooting methods, we start with some functions that are extensions of the [Schedule](https://github.com/McLeodMoores/xl4j/blob/master/xll-examples/src/main/java/com/mcleodmoores/xl4j/examples/timeseries/Schedule.java) class that is available in the `xll-examples` project.

We have added a simple marker interface:

``` java
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
}
```
These classes can be found on the `examples-for-docs` branch in the `xll-examples` project.

### General note about errors
It can sometimes be hard to work out if an error is coming from Excel or the add-in itself. It's worth noting that errors returned from XL4J are the `#NULL!` error type. Excel only returns this type of error "when you refer to an intersection of two ranges that do not intersect". As a general rule of thumb, therefore, any errors other than `#NULL!` are because there's an error in the formula, division by zero, etc., and `#NULL!` is an error from XL4J if there are no ranges referenced in the formula.


## \#NULL!

### The function is missing
After the plugin has been built with these new classes, the next step is to try to use them. However, when we try to use the reverse quarterly function in Excel, the function name doesn't appear in the suggestions:

![Missing function 1](images/missing-function-1.png)

The first thing to check is that the add-in has installed successfully by looking at the tabs: **TODO what are these called**:

![Missing function 2](images/missing-function-2.png)

There's no `Add-ins` tab, which means that the add-in hasn't been registered. Once we've fixed this, we try again and see:

![Missing function 3](images/missing-function-3.png)

Although other schedule functions have been registered, we still can't see either of our functions. The first thing to do is to check that the functions were registered on the Java side. We go to the `Add-ins` tab and open the Java log (note that the logging level is `INFO` - if the logging level is set to be higher than this, then the ![settings](settings.md) must be changed and Excel restarted). There is a section that shows the names of the functions that have been registered (shown with some registrations removed):

![Missing function 4](images/missing-function-4.png)

As expected, there is no entry for either of our functions. Looking further in the log, there is an error message warning us that we cannot mark the interface with an `@XLFunctions` annotation:

![Missing function 5](images/missing-function-5.png)

To fix this problem, we remove the annotation on `ScheduleFunctionV1` and move it to both functions:

``` java
public interface ScheduleFunctionV2 extends BiFunction<LocalDate, LocalDate, Schedule> {
}
```
``` java
@XLNamespace("Schedule.")
@XLFunctions(
    prefix = "ForwardQuarterly",
    typeConversionMode = TypeConversionMode.OBJECT_RESULT,
    description = "Generates a quarterly schedule from the start to end date",
    category = "Schedule")
public class ForwardQuarterlyScheduleFunctionV2 implements ScheduleFunctionV2 {

  /**
   * Default constructor.
   */
  ForwardQuarterlyScheduleFunctionV2() {
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
@XLNamespace("Schedule.")
@XLFunctions(
    prefix = "ReverseQuarterly",
    typeConversionMode = TypeConversionMode.OBJECT_RESULT,
    description = "Generates a quarterly schedule from the end to start date",
    category = "Schedule")
public class ReverseQuarterlyScheduleFunctionV2 implements ScheduleFunctionV2 {

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
}
```
After rebuilding and reinstalling, we can create the reverse quarterly schedule calculator:

![Fixed function 1](images/fixed-function-1.png)

However, the forward calculator is still not available:

![Missing function 6](images/missing-function-6.png)

Going back to the log, we see that there are entries for the `apply()` and `andThen()` methods, but not the constructor.

![Missing function 7](images/missing-function-7.png)

Only `public` classes, constructors or methods can be registered as functions. Once the constructor visibility is changed, the forward quarterly calculator can also be created in Excel.

### The function name seems wrong

We've added another function that generates a forward schedule with a user-supplied month interval:

``` java
@XLNamespace("Schedule.")
@XLFunctions(
    prefix = "ForwardNMonths",
    typeConversionMode = TypeConversionMode.OBJECT_RESULT,
    description = "Generates a schedule from the start to end date",
    category = "Schedule")
public class ForwardNMonthsScheduleFunctionV1 implements ScheduleFunctionV2 {
  private final int _n;

  public ForwardNMonthsScheduleFunctionV1(final int n) {
    _n = n;
  }

  @XLFunction(
      name = "GenerateForwardNMonthsSchedule",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT,
      description = "Generates a schedule from the start to end date",
      category = "Schedule")
  @Override
  public Schedule apply(final LocalDate start, final LocalDate endInclusive) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(endInclusive, "endInclusive");
    ArgumentChecker.isTrue(_n > 0, "The interval must be greater than zero: have {}", _n);
    LocalDate date = start;
    final Schedule result = new Schedule();
    int i = 0;
    while (!date.isAfter(endInclusive)) {
      result.add(date);
      // offset from start to avoid end-of-month effects e.g. don't want 31 Nov -> 28 Feb -> 28 May
      i += _n;
      date = start.plusMonths(i);
    }
    return result;
  }
}
```
However, when we try to call the `Schedule.ForwardNMonths.apply` function, it does not appear in the list of available functions.

![Missing function 8](images/missing-function-8.png)

This is because the `@XLFunction` annotation takes precedence over `@XLFunctions`, which prevents the same method from being registered twice. The `apply` method has been registered as `Schedule.GenerateForwardNMonthsSchedule`. Using this function name, the schedule can be generated.

![Fixed function 2](images/fixed-function-2.png)

![Successful call 1](images/successful-call-1.png)

As we don't intend to call the `andThen` method for this calculator, we've removed the class-level annotation and added an annotation for the constructor (very important: the object can't be constructed otherwise):

``` java
@XLNamespace("Schedule.")
public class ForwardNMonthsScheduleFunctionV2 implements ScheduleFunctionV2 {
  private final int _n;

  @XLFunction(
      name = "ForwardNMonths",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT,
      description = "Generates a schedule from the start to end date",
      category = "Schedule")
  public ForwardNMonthsScheduleFunctionV2(
      @XLParameter(name = "Number of months") final int n) {
    _n = n;
  }

  @XLFunction(
      name = "GenerateForwardNMonthsSchedule",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT,
      description = "Generates a schedule from the start to end date",
      category = "Schedule")
  @Override
  public Schedule apply(final LocalDate start, final LocalDate endInclusive) {
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(endInclusive, "endInclusive");
    ArgumentChecker.isTrue(_n > 0, "The interval must be greater than zero: have {}", _n);
    LocalDate date = start;
    final Schedule result = new Schedule();
    int i = 0;
    while (!date.isAfter(endInclusive)) {
      result.add(date);
      // offset from start to avoid end-of-month effects e.g. don't want 31 Nov -> 28 Feb -> 28 May
      i += _n;
      date = start.plusMonths(i);
    }
    return result;
  }
}
```
![Successful call 2](images/successful-call-2.png)

### The function cannot be called successfully

This section uses a class that performs simple adjustments on a `Schedule` that contains a static factory method:

``` java
@XLNamespace(value = "Schedule.")
public final class ScheduleAdjusterV1 {

  @XLFunction(
      name = "ScheduleAdjuster",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT)
  public static ScheduleAdjuster of(
      @XLParameter(name = "schedule") final Schedule schedule) {
    return new ScheduleAdjuster(schedule);
  }

  private final Schedule _schedule;

  private ScheduleAdjuster(final Schedule schedule) {
    _schedule = ArgumentChecker.notNull(schedule, "schedule");
  }
  
    @XLFunction(
      name = "WithDayOffset",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT)
  public Schedule withDayOffset(
      @XLParameter(name = "days") final int days) {
    final Schedule adjusted = new Schedule();
    for (final LocalDate date : _schedule) {
      adjusted.add(date.plusDays(days));
    }
    return adjusted;
  }

  @XLFunction(
      name = "WithWeekOffset")
  public Schedule withWeekOffset(
      @XLParameter(name = "weeks") final int weeks) {
    final Schedule adjusted = new Schedule();
    for (final LocalDate date : _schedule) {
      adjusted.add(date.plusDays(weeks * 7));
    }
    return adjusted;
  }

  @XLFunction(
      name = "WithMonthOffset",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT)
  public Schedule withMonthOffset(
      @XLParameter(name = "months") final int months,
      @XLParameter(name = "daysPerMonth", optional = true) final Integer daysPerMonth) {
    ArgumentChecker.notNegative(daysPerMonth, "The number of days per month cannot be negative");
    final Schedule adjusted = new Schedule();
    if (daysPerMonth != null) {
      for (final LocalDate date : _schedule) {
        adjusted.add(date.plusDays(daysPerMonth * months));
      }
    } else {
      for (final LocalDate date : _schedule) {
        adjusted.add(date.plusMonths(months));
      }
    }
    return adjusted;
  }

  @XLFunction(
      name = "IntersectTimeSeries",
      typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
  public TimeSeries intersectTimeSeries(
      @XLParameter(name = "ts") final TimeSeries ts) {
    final TimeSeries result = TimeSeries.newTimeSeries();
    for (final LocalDate date : _schedule) {
      final double value = ts.get(date);
      result.put(date, value);
    }
    return result;
  }

```
and has mutator methods that offset the dates in the schedule by a fixed amount, and a method that performs an intersection with a time series.

### Calling an instance method

The adjuster is created:

![Static call 1](images/static-call-1.png)

and we unsuccessfully try to adjust the schedule by a 4 day offset:

![Failed instance call 1](images/failed-instance-call-1.png)

The last error message in the log does not give many clues:

![Failed instance call 2](images/failed-instance-call-2.png)

but the lines above show what the problem is. The arguments to the function are `XLNumber` and `XLMissing`. For instance methods, the first argument to the Excel function must be the object that the method is being called on. The call handler is expecting an `XLObject` (the schedule adjuster) and `XLNumber` (the number of days to adjust by). Once the schedule adjuster is passed into the function as the first argument, the function completes successfully:

![Successful instance call 1](images/successful-instance-call-1.png)

### Too few arguments

We have already seen the effect of inadvertently supplying too few arguments when calling an instance function:

![Too few arguments 1](images/too-few-arguments-1.png)

and there is a message in the log showing the arguments that were supplied and the number that were expected:

![Too few arguments 2](images/too-few-arguments-2.png)

Unless some or all of the arguments to a method or constructor are `Optional` (** TODO link to section**), an argument of `XLMissing` shows that the wrong number of arguments were provided.

### Wrong argument type

The ![argument converters](https://github.com/McLeodMoores/xl4j/blob/master/xll-core/src/main/java/com/mcleodmoores/xl4j/v1/typeconvert/converters/) available in the `xll-core` project can handle most of the expected types to and from Excel types (e.g. `XLNumber`, `XLString`, etc.). However, especially for functions with many arguments, it can be easy to get the arguments in the wrong order or provide the wrong type.

In an extremely artifical example, we supply a string where an integer (`XLString` -> `int` instead of `XLNumber` -> `int`) is expected:

![Wrong argument type 1](images/wrong-argument-type-1.png)

In the majority of cases, an exception will be thrown in the type converter that was registered for the function:

![Wrong argument type 2](images/wrong-argument-type-2.png)

The solution to this is to either fix the function call by supplying an argument of the expected type, or changing the function and rebuilding.

### Optional arguments

Optional arguments to XL4J functions are provided in the same way as for other Excel functions. If the optional argument is in the middle of a list of arguments, the value can be left empty (e.g. `=FUNC(A1,A2,,,A3)`). If the last argument is optional, then either `=FUNC(A1,A2,)` or `=FUNC(A1,A2)` will work.

The `withMonthOffset` function in our schedule adjuster takes an optional argument (the number of days per month). This function works if we provide a value for this field:

![Successful optional argument 1](images/successful-optional-argument-1.png)

but not if it's left out:

![Failed optional argument 1](images/failed-optional-argument-1.png)

Looking in the log, we can see the issue. 

![Failed optional argument 2](images/failed-optional-argument-2.png)

Missing values are passed into Java methods / constructors as `null`. However, this value is dereferenced before any check for null. If we change the method to check the value of `daysPerMonth` only after a `null` check:

``` java
  @XLFunction(
      name = "WithMonthOffset",
      typeConversionMode = TypeConversionMode.OBJECT_RESULT)
  public Schedule withMonthOffset(
      @XLParameter(name = "months") final int months,
      @XLParameter(name = "daysPerMonth", optional = true) final Integer daysPerMonth) {
    final Schedule adjusted = new Schedule();
    if (daysPerMonth != null) {
      ArgumentChecker.notNegative(daysPerMonth, "The number of days per month cannot be negative");
      for (final LocalDate date : _schedule) {
        adjusted.add(date.plusDays(daysPerMonth * months));
      }
    } else {
      for (final LocalDate date : _schedule) {
        adjusted.add(date.plusMonths(months));
      }
    }
    return adjusted;
  }
```
then the function returns a schedule:

![Successful optional argument 2](images/successful-optional-argument-2.png)

In this case, `null` was allowed as an input to the method because the type was `Integer`. If this is replaced by `int`, an exception is thrown before the method is called:

![Failed optional argument 3](images/failed-optional-argument-3.png)


## \#VALUE!

### Too many arguments

If a function is called with too many arguments:

![Too many arguments 1](images/too-many-arguments-1.png)

then Excel itself will return a `#VALUE!` error:

![Too many arguments 2](images/too-many-arguments-2.png)

According to the Excel help page, this error means "There's something wrong with the way your formula is typed." Unfortunately, there is no way to get a more detailed error for these type of problems.


## Unexpected value returned to Excel

Finally, everything seems to work so we try to intersect the dates of the schedule with the dates of a time series. However, instead of the `TimeSeries` object returned by `ScheduleAdjusterV2.intersectTimeSeries()`, we get a single number output:

![Unexpected result 1](images/unexpected-result-1.png)

This is because the `TypeConversionMode` of this function is `SIMPLEST_RESULT`. This means that the add-in searches for a type converter for `TimeSeries`, which is available and ![converts `TimeSeries` to `XLArray`](https://github.com/McLeodMoores/xl4j/blob/master/xll-examples/src/main/java/com/mcleodmoores/xl4j/examples/timeseries/TimeSeriesTypeConverter.java). So, the result returned to Excel is an array, rather than an object reference. What Excel is displaying is the [0,0]th element of the array.

There are three ways of dealing with this:
  - change the type conversion mode to `OBJECT_RESULT`
  - remove the time series type converter
  - expand the result as an array formula
  
To expand the result as an array, select a range:

![Unexpected result 2](images/unexpected-result-2.png)

and hit `CTRL+SHIFT+ENTER`, which tells Excel that this is an array formula (see the ![hints and tips](hints-and-tips.md) document for more information about array formulas). Note that we've transposed the array, as the time series type converter happens to return a time series as two columns.

![Unexpected result 3](images/unexpected-result-3.png)

The formula is now surrounded by curly brackets, which shows that it is an Excel array formula. 

Finally, the first row of the time series is formatted as dates:
  
![Unexpected result 4](images/unexpected-result-4.png)

![Expected result 1](images/expected-result-1.png)

 ### Dates in Excel
 
The previous example showed something that can cause issues: dates in Excel are numbers. This doesn't cause a problem for the XL4J add-in, as conversion to and from dates takes this into account. However, it's worth noting that Excel will display any dates returned from the add-in as numbers, which can be a bit disconcerting. Formatting the cells as dates, although not necessary, can help prevent confusion.

# Attaching a debugger

If you have the source code / jars available, it's often quicker and easier to debug any problems within a function using an IDE. Once the code is reached, the debugger behaves the same as it would for any other Java project.

## Attaching a debugger (Eclipse)
## Attaching a debugger (IntelliJ)


