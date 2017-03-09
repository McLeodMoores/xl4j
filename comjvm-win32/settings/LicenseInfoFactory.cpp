/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "LicenseInfoInterface.h"
#include "LicenseInfo.h"
#include "utils/Debug.h"

HRESULT CLicenseInfoFactory::Create(HWND hWndParent, wchar_t * szAppName, wchar_t * szLicenseeText, wchar_t * szLicenseText, ILicenseInfo ** ppLicenseInfo) {
	LOGTRACE("About to init MFC");
	if (!AfxWinInit(::GetModuleHandle(L"settings"), NULL, ::GetCommandLine(), 0)) {
		LOGERROR("Fatal Error: MFC initialization failed");
		return E_ABORT;
	}
	*ppLicenseInfo =  new CLicenseInfo(CWnd::FromHandle(hWndParent), szAppName, szLicenseeText, szLicenseText);
	if (!*ppLicenseInfo) return E_OUTOFMEMORY;
	return S_OK;
}