/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */



#ifndef _STDAFX_H_
#define _STDAFX_H_

#define _WIN32_DCOM
#define _WIN32_FUSION 0x0100

#include "targetver.h"

#define WIN32_LEAN_AND_MEAN             // Exclude rarely-used stuff from Windows headers

#include <windows.h>
#include <Shlobj.h>
#include <Shlwapi.h>
#include <comdef.h>
#include <assert.h>
#include <tchar.h>
#include <strsafe.h>
#include <list>
#include <unordered_map>
#include <unordered_set>
#include "../utils/Debug.h"

#ifdef _UNICODE
typedef std::wstring _std_string_t;
#else /* ifdef _UNICODE */
typedef std::string _std_string_t;
#endif /* ifdef _UNICODE */

#endif /* ifndef _STDAFX_H_ */