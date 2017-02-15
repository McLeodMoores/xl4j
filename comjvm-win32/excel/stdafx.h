/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#ifndef _STDAFX_H_
#define _STDAFX_H_

#include "targetver.h"

#define WIN32_LEAN_AND_MEAN             // Exclude rarely-used stuff from Windows headers

// Windows Header Files:
#include <windows.h>
#include <comdef.h>

// Standard header files:
#include <assert.h>
#include <tchar.h>
#include <vector>
#include <string>

// Headers for CppUnitTest:
#include "CppUnitTest.h"

// Headers for Excel
#include <xlcall.h>
#include <framewrk.h>

// comjvm stuff
#include <comdef.h>
#include <iostream>
#include "comjvm/local.h"
#include "comjvm/core.h"
#include <jni.h>

#include "utils/Debug.h"

#endif /* ifndef _STDAFX_H_ */