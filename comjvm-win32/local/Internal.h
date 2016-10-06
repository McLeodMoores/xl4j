/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

extern volatile long g_lActiveObjects;

#define IncrementActiveObjectCount() InterlockedIncrement (&g_lActiveObjects)
#define DecrementActiveObjectCount() InterlockedDecrement (&g_lActiveObjects)
