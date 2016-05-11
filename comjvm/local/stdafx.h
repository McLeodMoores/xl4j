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
#include <windows.h>
#include <comdef.h>
#include <assert.h>
#include <tchar.h>
#include <strsafe.h>
#include <list>
#include <vector>
#include <jni.h>

#include "utils/Debug.h"

#ifdef _UNICODE
typedef std::wstring _std_string_t;
__inline const _std_string_t _tstring (const std::string &ansi) {
	return (PCWSTR)_bstr_t (ansi.data ());
}
__inline const _std_string_t _tstring (const std::wstring &wide) {
	return wide;
}
#else /* ifdef _UNICODE */
typedef std::string _std_string_t;
__inline const _std_string_t _tstring (const std::string &ansi) {
	return ansi;
}
__inline const _std_string_t _tstring (const std::wstring &wide) {
	return (PCSTR)_bstr_t (ansi.data ());
}
#endif /* ifdef _UNICODE */

#endif /* ifndef _STDAFX_H_ */
