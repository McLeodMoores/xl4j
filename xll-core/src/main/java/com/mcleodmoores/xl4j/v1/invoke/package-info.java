/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited. All rights reserved.
 *
 * Concrete classes for implementation of function invocation or field getting associated with an incoming function call 
 * from Excel.  These classes reflectively analyse methods and fields to statically (where possible) determine appropriate
 * type converters to use during function calls.  Passthrough invokers/getters avoid double-conversion when doing reflective
 * method invocation on Java-side, see the xll-java project for an example.
 */
package com.mcleodmoores.xl4j.v1.invoke;