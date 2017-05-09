/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries.troubleshooting;

import java.util.function.BiFunction;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.examples.timeseries.Schedule;

/**
 * Marker interface for functions used in the debugging documentation.
 */
public interface ScheduleFunctionV2 extends BiFunction<LocalDate, LocalDate, Schedule> {

}
