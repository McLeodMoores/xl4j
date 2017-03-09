/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "Excel.h"

HRESULT LoadDLLs () {
	XLOPER12 xDLL;
	Excel12f (xlGetName, &xDLL, 0);
	wchar_t szDirPath[MAX_PATH];
	wchar_t *pszDlls[] = { L"core.dll", L"jni.dll", L"helper.dll", L"settings.dll", L"local.dll", L"utils.dll"
#ifdef NDEBUG
		,L"msvcr120.dll", L"msvcp120.dll", /*L"vccorlib120.dll",*/ L"mfc120u.dll", L"mfcm120u.dll"
#endif
	};
	HRESULT hr;
	for (int i = 0; i < _countof (pszDlls); i++) {
		if (SUCCEEDED (hr = FileUtils::GetAddinAbsolutePath (szDirPath, MAX_PATH, pszDlls[i]))) {
			LOGTRACE ("Loading DLL %s", szDirPath);
			if (!LoadLibraryExW(szDirPath, NULL, LOAD_WITH_ALTERED_SEARCH_PATH)) {
				_com_error err(HRESULT_FROM_WIN32(GetLastError()));
				LOGFATAL("Fatal error loading DLL %s: %s", szDirPath, err.ErrorMessage());
				return E_FAIL;
			}
		} else {
			LOGERROR ("Error gettings AddinDirectory path");
		}
	}
	Excel12f (xlFree, 0, 1, (LPXLOPER12)&xDLL);
	HMODULE hModule;
	// set this module to never unload.
	GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_PIN | GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, reinterpret_cast<LPCTSTR>(LoadDLLs), &hModule);
	return S_OK;
}

//void InitAddin () {
//	if (!g_pAddinEnv) {
//		g_pAddinEnv = new CAddinEnvironment ();
//		g_pAddinEnv->Start();
//	}
//}
//
//void InitJvm () {
//	g_pJvmEnv = new CJvmEnvironment (g_pAddinEnv);
//	g_pJvmEnv->Start();
//}
//
//
//void RestartJvm () {
//	g_pJvmEnv->Shutdown();
//	g_pJvmEnv->Start();
//}