/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "LicenseInfoInterface.h"
#include "LicenseInfo.h"

HRESULT CLicenseInfoFactory::Create(HWND hWndParent, wchar_t * szAppName, wchar_t * szLicenseeText, wchar_t * szLicenseText, ILicenseInfo ** ppLicenseInfo) {
	*ppLicenseInfo =  new CLicenseInfo(CWnd::FromHandle(hWndParent), szAppName, szLicenseeText, szLicenseText);
	if (!*ppLicenseInfo) return E_OUTOFMEMORY;
	return S_OK;
}