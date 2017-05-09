/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.examples.timeseries.troubleshooting;

import java.util.function.BiFunction;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.xl4j.examples.timeseries.Schedule;
import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLNamespace;

/**
 * Marker interface for functions used in the debugging documentation.
 */
@XLNamespace("Schedule.")
@XLFunctions(
    typeConversionMode = TypeConversionMode.OBJECT_RESULT,
    category = "Schedule")
public interface ScheduleFunctionV1 extends BiFunction<LocalDate, LocalDate, Schedule> {

}
