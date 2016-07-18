#include "stdafx.h"
#include <strsafe.h>
#include "AvailableJvms.h"
//#include "../core/Settings.h"
//#include "../utils/Debug.h"
#define MAX_KEY_LENGTH 8192 //255
#define MAX_VALUE_NAME 16383

CAvailableJvms::CAvailableJvms () {
}


CAvailableJvms::~CAvailableJvms () {
}

HRESULT CAvailableJvms::Search (LPCTSTR pszVendor, std::vector<LPTSTR>& versions) {
	/*BOOL bResult = FALSE;
	HKEY hkeySoftware;
	HRESULT result;
	if ((result = RegOpenKey (HKEY_LOCAL_MACHINE, TEXT ("SOFTWARE"), &hkeySoftware)) == ERROR_SUCCESS) {
		HKEY hkeyVendor;
		if ((result = RegOpenKey (hkeySoftware, pszVendor, &hkeyVendor)) == ERROR_SUCCESS) {
			HKEY hkeyJRE;
			if ((result = RegOpenKey (hkeyVendor, TEXT ("Java Runtime Environment"), &hkeyJRE)) == ERROR_SUCCESS) {
				DWORD cchMaxSubKeyLen;
				DWORD cSubKeys;
				if ((result = RegQueryInfoKey (hkeyJRE, NULL, NULL, NULL, &cSubKeys, &cchMaxSubKeyLen, NULL, NULL, NULL, NULL, NULL, NULL)) == ERROR_SUCCESS) {
					LOGTRACE ("Max sub key length is %d, there are %d sub keys", cchMaxSubKeyLen, cSubKeys);
					DWORD i = 0;
					TCHAR *pVersionKeyName = (TCHAR *) malloc ((cchMaxSubKeyLen + 1) * sizeof (TCHAR));
					DWORD cchVersionKeyName = cchMaxSubKeyLen;
					for (DWORD i = 0; i < cSubKeys; i++) {
						cchVersionKeyName = cchMaxSubKeyLen + 1;
						result = RegEnumKeyEx (hkeyJRE, i, pVersionKeyName, &cchVersionKeyName, NULL, NULL, NULL, NULL);
						LOGTRACE ("RegEnumKeyEx(%d) returned subkey %d characters long: %s", i, cchVersionKeyName, pVersionKeyName);
						if (FAILED (result)) {
							_com_error err (result);
							LOGERROR ("RegEnumKeyEx failed: cchVersionKeyName size = %d, %s", cchVersionKeyName, err.ErrorMessage ());
						}
						LPTSTR version = (LPTSTR)malloc ((cchVersionKeyName + 1) * sizeof (TCHAR));
						if (version == NULL) {
							result = E_OUTOFMEMORY;
							break;
						}
						if (FAILED (result = StringCchCopy (version, cchVersionKeyName + 1, pVersionKeyName))) {
							LOGERROR ("StringCchCopy failed: cchVersionKeyName = %d", cchVersionKeyName);
							break;
						}
						versions.push_back (version);
					}
				
					free (pVersionKeyName);
					RegCloseKey (hkeyJRE);
				} else {
					_com_error err (result);
					LOGERROR ("RegQueryInfoKey failed: %s", err.ErrorMessage ());
				}
			} else {
				LOGERROR ("Error opening Java Runtime Environment key in registry");
			}
			RegCloseKey (hkeyVendor);
		} else {
			LOGERROR ("Error opening vendor key in registry");
		}
		RegCloseKey (hkeySoftware);
	} else {
		LOGERROR ("Error opening SOFTWARE key in registry");
	}
	return result;*/return S_OK;
}
