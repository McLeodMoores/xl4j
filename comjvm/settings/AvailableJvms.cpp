#include "stdafx.h"
#include <strsafe.h>
#include "AvailableJvms.h"
#include "../core/Settings.h"

#define MAX_KEY_LENGTH 255
#define MAX_VALUE_NAME 16383

CAvailableJvms::CAvailableJvms () {
}


CAvailableJvms::~CAvailableJvms () {
}

HRESULT CAvailableJvms::Search (LPCTSTR pszVendor, std::vector<LPTSTR>& versions) {
	BOOL bResult = FALSE;
	HKEY hkeySoftware;
	HRESULT result;
	if ((result = RegOpenKey (HKEY_LOCAL_MACHINE, TEXT ("SOFTWARE"), &hkeySoftware)) == ERROR_SUCCESS) {
		HKEY hkeyVendor;
		if ((result = RegOpenKey (hkeySoftware, pszVendor, &hkeyVendor)) == ERROR_SUCCESS) {
			HKEY hkeyJRE;
			if ((result = RegOpenKey (hkeyVendor, TEXT ("Java Runtime Environment"), &hkeyJRE)) == ERROR_SUCCESS) {
				DWORD i = 0;
				TCHAR pVersionKeyName[MAX_KEY_LENGTH];
				DWORD cchVersionKeyName;
				while ((result =  RegEnumKeyEx (hkeyJRE, i++, pVersionKeyName, &cchVersionKeyName, NULL, NULL, NULL, NULL)) == ERROR_SUCCESS) {
					LPTSTR version = (LPTSTR) malloc ((cchVersionKeyName + 1) * sizeof (TCHAR));
					if (version == NULL) {
						result = E_OUTOFMEMORY;
						break;
					}
					if (FAILED (result = StringCchCopy (version, cchVersionKeyName, pVersionKeyName))) {
						break;
					}
					versions.push_back (version);
				}
				RegCloseKey (hkeyJRE);
			}
			RegCloseKey (hkeyVendor);
		}
		RegCloseKey (hkeySoftware);
	}
	return result;
}
