/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.function.Function;

/**
 * Marker interface for unary time series functions.
 *
 * @param <U>
 *            the return type
 */
public interface TimeSeriesFunction<U> extends Function<TimeSeries, U> {

}
