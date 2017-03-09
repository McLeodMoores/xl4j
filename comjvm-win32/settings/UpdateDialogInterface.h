/*
* Copyright 2014-present by McLeod Moores Software Limited.
* See distribution for license.
*/

#pragma once
#include <comdef.h>
#include "core/Settings.h"

#ifdef COMJVM_UPGRADEDIALOG_EXPORT
# define COMJVM_UPGRADEDIALOG_API __declspec(dllexport)
#else
# define COMJVM_UPGRADEDIALOG_API __declspec(dllimport)
#endif /* ifndef COMJVM_UPGRADEDIALOG_API */

class COMJVM_UPGRADEDIALOG_API IUpdateDialog {
public:
	virtual INT_PTR Open(HWND hwndParent) = 0;
};

class COMJVM_UPGRADEDIALOG_API CUpdateDialogFactory {
public:
	static HRESULT Create(HWND hwndParent, CSettings *pSettings, wchar_t *szUpdateText, wchar_t *szURL, IUpdateDialog **ppLicenseInfo);
};