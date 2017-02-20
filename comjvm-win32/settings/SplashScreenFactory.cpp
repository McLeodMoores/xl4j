/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "SplashScreenInterface.h"
#include "SplashScreen.h"
#include "../utils/Debug.h"

HRESULT CSplashScreenFactory::Create(wchar_t *szLicenseeText, ISplashScreen **ppSplashScreen) {
	if (!AfxWinInit(::GetModuleHandle(L"settings"), NULL, ::GetCommandLine(), 0)) {
		LOGERROR("Fatal Error: MFC initialization failed");
		return E_ABORT;
	}
	*ppSplashScreen = new CSplashScreen(CString(szLicenseeText), nullptr);
	return S_OK;
}