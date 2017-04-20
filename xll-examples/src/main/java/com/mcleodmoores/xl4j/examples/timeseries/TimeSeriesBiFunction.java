/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries;

import java.util.function.BiFunction;

import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;

/**
 * Marker interface for binary time series functions.
 *
 * @param <T>
 *          the type of the second argument
 * @param <U>
 *          the return type
 */
@XLNamespace("TimeSeries")
public interface TimeSeriesBiFunction<T, U> extends BiFunction<TimeSeries, T, U> {

}
