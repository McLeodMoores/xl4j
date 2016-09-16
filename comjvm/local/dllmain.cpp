/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "../core/core_h.h"
#include "local_h.h"
#include "internal.h"
#include "JvmConnector.h"

volatile long g_lActiveObjects = 0;

extern "C" HRESULT APIENTRY DllGetImplementingCLSID (REFIID iid, DWORD dwIndex, BSTR *pProgId, CLSID *pCLSID) {
	if (!pProgId) return E_POINTER;
	if (!pCLSID) return E_POINTER;
	if (iid == IID_IJvmConnector) {
		if (dwIndex == 1) {
			try {
				_bstr_t bstr (TEXT ("Beerdragon.ConnectLocalJvm.1"));
				*pProgId = bstr.Detach ();
				*pCLSID = CLSID_LocalJvmConnector;
				return S_OK;
			} catch (_com_error &e) {
				return e.Error ();
			}
		} else {
			return CLASS_E_CLASSNOTAVAILABLE;
		}
	}
	return E_NOINTERFACE;
}

HRESULT APIENTRY DllGetClassObject (REFCLSID clsid, REFIID iid, LPVOID *ppv) {
	if (!ppv) return E_POINTER;
	if (clsid == CLSID_LocalJvmConnector) {
		try {
			CJvmConnectorFactory *pFactory = CJvmConnectorFactory::Instance ();
			HRESULT hr = pFactory->QueryInterface (iid, ppv);
			pFactory->Release ();
			return hr;
		} catch (std::bad_alloc) {
			return E_OUTOFMEMORY;
		}
	} else {
		return CLASS_E_CLASSNOTAVAILABLE;
	}
}

static HRESULT RegisterServer (HKEY hkeyRoot) {
	HRESULT hr;
	HKEY hkeyCLSID;
	LPOLESTR lpsz;
	hr = StringFromCLSID (CLSID_LocalJvmConnector, &lpsz);
	if (FAILED (hr)) return hr;
	TCHAR szKey[MAX_PATH];
	hr = StringCbPrintf (szKey, sizeof (szKey), TEXT ("%s%ws"), TEXT ("SOFTWARE\\Classes\\CLSID\\"), lpsz);
	CoTaskMemFree (lpsz);
	if (FAILED (hr)) return hr;
	DWORD dwError = RegCreateKeyEx (hkeyRoot, szKey, 0, NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS, NULL, &hkeyCLSID, NULL);
	if (dwError == ERROR_SUCCESS) {
		dwError = RegSetValueEx (hkeyCLSID, NULL, 0, REG_SZ, (LPBYTE)TEXT ("BeerDragon.ConnectLocalJvm.1"), 28 * sizeof (TCHAR));
		if (dwError != ERROR_SUCCESS) hr = HRESULT_FROM_WIN32 (dwError);
		HKEY hkeyServer;
		dwError = RegCreateKeyEx (hkeyCLSID, TEXT ("InprocServer32"), 0, NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS, NULL, &hkeyServer, NULL);
		if (dwError == ERROR_SUCCESS) {
			HMODULE hModule;
			if (GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, (LPCTSTR)RegisterServer, &hModule)) {
				TCHAR szFilename[MAX_PATH + 1];
				if (GetModuleFileName (hModule, szFilename, sizeof (szFilename) / sizeof (TCHAR)) <= MAX_PATH) {
					size_t cch = _tcslen (szFilename);
					dwError = RegSetValueEx (hkeyServer, NULL, 0, REG_SZ, (LPBYTE)szFilename, ((DWORD)cch + 1) * sizeof (TCHAR));
					if (dwError != ERROR_SUCCESS) hr = HRESULT_FROM_WIN32 (dwError);
				}
				FreeLibrary (hModule);
			}
			dwError = RegSetValueEx (hkeyServer, TEXT ("ThreadingModel"), 0, REG_SZ, (LPBYTE)TEXT ("Both"), 5 * sizeof (TCHAR));
			if (dwError != ERROR_SUCCESS) hr = HRESULT_FROM_WIN32 (dwError);
			RegCloseKey (hkeyServer);
		} else {
			hr = HRESULT_FROM_WIN32 (hr);
		}
		RegCloseKey (hkeyCLSID);
	} else {
		hr = HRESULT_FROM_WIN32 (dwError);
	}
	return hr;
}

static HRESULT UnregisterServer (HKEY hkeyRoot) {
	HRESULT hr;
	LPOLESTR lpsz;
	hr = StringFromCLSID (CLSID_LocalJvmConnector, &lpsz);
	if (FAILED (hr)) return hr;
	TCHAR szKey[MAX_PATH];
	hr = StringCbPrintf (szKey, sizeof (szKey), TEXT ("%s%ws"), TEXT ("SOFTWARE\\Classes\\CLSID\\"), lpsz);
	CoTaskMemFree (lpsz);
	if (FAILED (hr)) return hr;
	DWORD dwError = RegDeleteTree (hkeyRoot, szKey);
	return HRESULT_FROM_WIN32 (dwError);
}

HRESULT APIENTRY DllRegisterServer () {
	HRESULT hr;
	hr = RegisterServer (HKEY_LOCAL_MACHINE);
	if (FAILED (hr)) {
		hr = RegisterServer (HKEY_CURRENT_USER);
	}
	return hr;
}

HRESULT APIENTRY DllUnregisterServer () {
	UnregisterServer (HKEY_CURRENT_USER);
	UnregisterServer (HKEY_LOCAL_MACHINE);
	return S_OK;
}

HRESULT APIENTRY DllCanUnloadNow () {
	if (g_lActiveObjects) {
		return S_FALSE;
	} else {
		return S_OK;
	}
}

BOOL APIENTRY DllMain (
	HMODULE hModule,
	DWORD  dwReason,
	LPVOID lpReserved
	) {
	switch (dwReason) {
	case DLL_PROCESS_ATTACH:
		// TODO
		break;
	case DLL_THREAD_ATTACH:
		// TODO
		break;
	case DLL_THREAD_DETACH:
		// TODO
		break;
	case DLL_PROCESS_DETACH:
		// TODO
		break;
	}
	return TRUE;
}

