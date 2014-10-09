/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
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

// Headers for CppUnitTest:
#include "CppUnitTest.h"

// Headers for Excel
#include <xlcall.h>
#include <framewrk.h>

// comjvm stuff
#include <iostream>
#include "comjvm/local.h"
#include "comjvm/core.h"

#endif /* ifndef _STDAFX_H_ */