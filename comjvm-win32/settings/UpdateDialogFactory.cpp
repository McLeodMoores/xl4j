/*
* Copyright 2014-present by McLeod Moores Software Limited.
* See distribution for license.
*/

#include "stdafx.h"
#include "UpdateDialogInterface.h"
#include "UpdateDialog.h"
#include "utils/Debug.h"

HRESULT CUpdateDialogFactory::Create(HWND hWndParent, CSettings *pSettings, wchar_t * szLicenseeText, wchar_t * szLicenseText, IUpdateDialog ** ppUpgradeDialog) {
	LOGTRACE("About to init MFC");
	if (!AfxWinInit(::GetModuleHandle(L"settings"), NULL, ::GetCommandLine(), 0)) {
		LOGERROR("Fatal Error: MFC initialization failed");
		return E_ABORT;
	}
	*ppUpgradeDialog = new CUpdateDialog(CWnd::FromHandle(hWndParent), pSettings, szLicenseeText, szLicenseText);
	if (!*ppUpgradeDialog) return E_OUTOFMEMORY;
	return S_OK;
}