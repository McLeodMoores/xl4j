/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once
#include <comdef.h>

#ifdef COMJVM_LICENSEINFO_EXPORT
# define COMJVM_LICENSEINFO_API __declspec(dllexport)
#else
# define COMJVM_LICENSEINFO_API __declspec(dllimport)
#endif /* ifndef COMJVM_DEBUG_API */

class COMJVM_LICENSEINFO_API ILicenseInfo {
public:
	virtual INT_PTR Open(HWND hwndParent) = 0;
};

class COMJVM_LICENSEINFO_API CLicenseInfoFactory {
public:
	static HRESULT Create(HWND hwndParent, wchar_t *szAppName, wchar_t *szLicenseeText, wchar_t *szLicenseText, ILicenseInfo **ppLicenseInfo);
};