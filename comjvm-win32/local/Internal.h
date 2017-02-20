/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

extern volatile long g_lActiveObjects;

#define IncrementActiveObjectCount() InterlockedIncrement (&g_lActiveObjects)
#define DecrementActiveObjectCount() InterlockedDecrement (&g_lActiveObjects)
