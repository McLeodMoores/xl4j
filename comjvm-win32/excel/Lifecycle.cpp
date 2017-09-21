/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */
#include <Windows.h>
#include "stdafx.h"
#include "Excel.h"
#include "AddinFileUtils.h"

typedef HRESULT (STDAPICALLTYPE *DllInstall_t)(BOOL bInstall, _In_opt_  LPCWSTR pszCmdLine);

HRESULT LoadDLLs () {
	//XLOPER12 xDLL;
	//Excel12f (xlGetName, &xDLL, 0);
	wchar_t szDirPath[MAX_PATH];
	wchar_t *pszDlls[] = { L"utils.dll", L"core.dll", L"jni.dll", L"helper.dll", L"settings.dll", L"local.dll", L"rtd-server"
#ifdef NDEBUG
		,L"msvcr120.dll", L"msvcp120.dll", /*L"vccorlib120.dll",*/ L"mfc120u.dll", L"mfcm120u.dll"
#endif
	};
	HRESULT hr;
	for (int i = 0; i < _countof (pszDlls); i++) {
		if (SUCCEEDED (hr = AddinFileUtils::GetAddinAbsolutePath (szDirPath, MAX_PATH, pszDlls[i]))) {
			//LOGTRACE ("Loading DLL %s", szDirPath);
			if (!LoadLibraryExW(szDirPath, NULL, LOAD_WITH_ALTERED_SEARCH_PATH)) {
				_com_error err(HRESULT_FROM_WIN32(GetLastError()));
				//LOGFATAL("Fatal error loading DLL %s: %s", szDirPath, err.ErrorMessage());
				return E_FAIL;
			}
		} else {
			//LOGERROR ("Error gettings AddinDirectory path");
		}
	}
	//Excel12f (xlFree, 0, 1, (LPXLOPER12)&xDLL);
	HMODULE hModule;
	// set this module to never unload.
	GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_PIN | GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, reinterpret_cast<LPCTSTR>(LoadDLLs), &hModule);
	return S_OK;
}

HRESULT RegisterRTDServer() {
	HMODULE module;
	HRESULT hr;
	wchar_t szDirPath[MAX_PATH];
	if (SUCCEEDED(hr = AddinFileUtils::GetAddinAbsolutePath(szDirPath, MAX_PATH, L"rtd-server.dll"))) {
		if (!(module = LoadLibraryExW(szDirPath, NULL, LOAD_WITH_ALTERED_SEARCH_PATH))) {
			_com_error err(HRESULT_FROM_WIN32(GetLastError()));
			return E_FAIL;
		}
	} else {
		return E_FAIL;
	}
	LOGINFO("rtd-server.dll module handle is %x", module);
	DllInstall_t DllInstall = (DllInstall_t)GetProcAddress(module, "DllInstall");
	LOGINFO("rtd-server DllInstall address is %p", DllInstall);
	if (FAILED(DllInstall(TRUE, L"user"))) { // uninstall
		LOGERROR("Could not install rtd-server dll");
	} else {
		LOGINFO("Successfully called DllInstall(TRUE, \"user\")");
	}
}

HRESULT UnregisterRTDServer() {
	HMODULE module;
	HRESULT hr;
	wchar_t szDirPath[MAX_PATH];
	if (SUCCEEDED(hr = AddinFileUtils::GetAddinAbsolutePath(szDirPath, MAX_PATH, L"rtd-server.dll"))) {
		if (!(module = LoadLibraryExW(szDirPath, NULL, LOAD_WITH_ALTERED_SEARCH_PATH))) {
			_com_error err(HRESULT_FROM_WIN32(GetLastError()));
			return E_FAIL;
		}
	} else {
		//LOGERROR ("Error gettings AddinDirectory path");
	}
	LOGINFO("rtd-server.dll module handle is %x", module);
	DllInstall_t DllInstall = (DllInstall_t)GetProcAddress(module, "DllInstall");
	LOGINFO("rtd-server DllInstall address is %p", DllInstall);
	if (FAILED(DllInstall(FALSE, L"user"))) { // uninstall
		LOGERROR("Could not uninstall rtd-server dll");
	}
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