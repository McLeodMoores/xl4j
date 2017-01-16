/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

#include "stdafx.h"
#include "core.h"
#include "JvmSupport.h"
#include "internal.h"

volatile long g_lActiveObjects = 0;

HRESULT APIENTRY DllGetClassObject (REFCLSID clsid, REFIID iid, LPVOID *ppv) {
	LOGTRACE("Called");
	if (!ppv) return E_POINTER;
	if (clsid == CLSID_JvmSupport) {
		LPOLESTR szInterfaceName;
		if (SUCCEEDED(StringFromIID(iid, &szInterfaceName))) {
			LOGERROR("requested interface was %s", szInterfaceName);
			CoTaskMemFree(szInterfaceName);
		}
		if (iid != IID_IJvmSupport) {
			LOGERROR("Trying to get class object for %x-%x%x%x", iid.Data1, iid.Data2, iid.Data3, iid.Data4);
		}
		HRESULT hr;
		CJvmSupportFactory *pFactory = NULL;
		try {
			pFactory = CJvmSupportFactory::Instance ();
			hr = pFactory->QueryInterface (iid, ppv);
		} catch (std::bad_alloc) {
			hr = E_OUTOFMEMORY;
		}
		if (pFactory) pFactory->Release ();
		if (FAILED(hr)) {
			LOGTRACE("Falling through to ProxyDllGetClassObject");
			return ProxyDllGetClassObject(clsid, iid, ppv);
		}
		return hr;
	} else {
		LOGTRACE("Falling through to ProxyDllGetClassObject");
		return ProxyDllGetClassObject (clsid, iid, ppv);
	}
}

HRESULT APIENTRY RegisterTypeLibrary () {
	LOGTRACE("Called");
	HRESULT hr;
	HMODULE hModule;
	if (GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, (LPCTSTR)DllGetClassObject, &hModule)) {
		TCHAR szFilename[MAX_PATH + 1];
		ZeroMemory (szFilename, sizeof (szFilename)); // to please the code analyzer gods
		DWORD dwLength = GetModuleFileName (hModule, szFilename, sizeof (szFilename) / sizeof (TCHAR));
		if (dwLength <= MAX_PATH && dwLength > 0) {
			size_t cch = _tcslen (szFilename);
			hr = StringCchCopy (szFilename + cch - 3, 4, TEXT ("tlb"));
			ITypeLib *typelib;
			OutputDebugStringW (szFilename);
			hr = LoadTypeLibEx (szFilename, REGKIND_REGISTER, &typelib);
			if (FAILED (hr)) {
				_com_error err (hr);
				OutputDebugStringW (err.ErrorMessage());
				return hr;
			}
			//hr = RegisterTypeLibForUser (typelib, szFilename, NULL);
			//if (FAILED (hr)) {
			//	_com_error err (hr);
			//	OutputDebugStringW (err.ErrorMessage());
			//	return hr;
			//}
		} else { // there was an error
			LPWSTR pErrorMsg;
			FormatMessage (FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM, NULL, GetLastError (), 0,(LPWSTR)&pErrorMsg, 0, NULL);
			OutputDebugStringW (pErrorMsg);
			LocalFree (pErrorMsg);
			return HRESULT_FROM_WIN32 (GetLastError());
		}
		FreeLibrary (hModule);
	}
	return S_OK;
}

static HRESULT RegisterAppIDEntry(HKEY hkeyRoot) {
	LOGTRACE("Called");
	HRESULT hr;
	HKEY hkeyAppID;
	TCHAR szKey[MAX_PATH];
	DWORD dwError = RegCreateKeyEx(hkeyRoot, TEXT("SOFTWARE\\Classes\\AppID\\{2027AC1B-AB3E-4BE5-A98F-CB499990EA0E}"), 0, NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS, NULL, &hkeyAppID, NULL);
	if (dwError == ERROR_SUCCESS) {
		dwError = RegSetValueEx(hkeyAppID, NULL, 0, REG_SZ, (LPBYTE)TEXT("XL4J AppID"), 11 * sizeof(TCHAR));
		if (dwError == ERROR_SUCCESS) {
			dwError = RegSetValueEx(hkeyAppID, TEXT("DllSurrogate"), 0, REG_SZ, (LPBYTE)TEXT(""), 1 * sizeof(TCHAR));
			if (dwError != ERROR_SUCCESS) {
				LOGERROR("Error creating DllSurrogate entry for AppID registry key");
				return HRESULT_FROM_WIN32(dwError);
			}
		} else { 
			LOGERROR("Error creating root entry for AppID registry entry");
			return HRESULT_FROM_WIN32(dwError);
		}
		RegCloseKey(hkeyAppID);
	} else {
		LOGERROR("Error creating AppID registry entry");
		return HRESULT_FROM_WIN32(dwError);
	}
	return S_OK;
}

static HRESULT RegisterServer (HKEY hkeyRoot) {
	LOGTRACE("Called");
	HRESULT hr;
	HKEY hkeyCLSID;
	LPOLESTR lpsz;
	hr = StringFromCLSID (CLSID_JvmSupport, &lpsz);
	if (FAILED (hr)) return hr;
	TCHAR szKey[MAX_PATH];
	hr = StringCbPrintf (szKey, sizeof (szKey), TEXT ("%s%ws"), TEXT ("SOFTWARE\\Classes\\CLSID\\"), lpsz);
	CoTaskMemFree (lpsz);
	if (FAILED (hr)) return hr;
	DWORD dwError = RegCreateKeyEx (hkeyRoot, szKey, 0, NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS, NULL, &hkeyCLSID, NULL);
	if (dwError == ERROR_SUCCESS) {
		dwError = RegSetValueEx(hkeyCLSID, NULL, 0, REG_SZ, (LPBYTE)TEXT("ComJvmCore.JvmSupport.1"), 24 * sizeof(TCHAR));
		if (dwError != ERROR_SUCCESS) {
			LOGERROR("Could not create default value on CLSID registry entry");
			hr = HRESULT_FROM_WIN32(dwError);
		}
		dwError = RegSetValueEx(hkeyCLSID, TEXT("AppID"), 0, REG_SZ, (LPBYTE)TEXT("{2027AC1B-AB3E-4BE5-A98F-CB499990EA0E}"), 38 * sizeof(TCHAR));
		if (dwError != ERROR_SUCCESS) {
			LOGERROR("Could not create AppID value on CLSID registry entry");
			hr = HRESULT_FROM_WIN32(dwError);
		}
	} else {
		_com_error err(HRESULT_FROM_WIN32(dwError));
		LOGERROR("Could not create class entry %s: %s", szKey, err.ErrorMessage());
	}
	if (dwError == ERROR_SUCCESS) {
		HKEY hkeyServer;
		dwError = RegCreateKeyEx (hkeyCLSID, TEXT ("InprocServer32"), 0, NULL, REG_OPTION_NON_VOLATILE, KEY_ALL_ACCESS, NULL, &hkeyServer, NULL);
		if (dwError == ERROR_SUCCESS) {
			HMODULE hModule;
			if (GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, (LPCTSTR)RegisterServer, &hModule)) {
				TCHAR szFilename[MAX_PATH + 1];
				ZeroMemory (szFilename, sizeof (szFilename)); // please the code analyzer gods
				DWORD dwLength = GetModuleFileName (hModule, szFilename, sizeof (szFilename) / sizeof (TCHAR));
				if (dwLength <= MAX_PATH && dwLength > 0) {
					size_t cch = _tcslen (szFilename);
					dwError = RegSetValueEx (hkeyServer, NULL, 0, REG_SZ, (LPBYTE)szFilename, ((DWORD)cch + 1) * sizeof (TCHAR));
					if (dwError != ERROR_SUCCESS) hr = HRESULT_FROM_WIN32 (dwError);
				} else { // there was an error
					return HRESULT_FROM_WIN32 (GetLastError());
				}
				FreeLibrary (hModule);
			} else { // there was an error
				return HRESULT_FROM_WIN32 (GetLastError());
			}
			dwError = RegSetValueEx (hkeyServer, TEXT ("ThreadingModel"), 0, REG_SZ, (LPBYTE)TEXT ("Both"), 5 * sizeof (TCHAR));
			if (dwError != ERROR_SUCCESS) hr = HRESULT_FROM_WIN32 (dwError);
			RegCloseKey (hkeyServer);
		} else {
			hr = HRESULT_FROM_WIN32 (hr);
		}
		RegCloseKey (hkeyCLSID);
	} else {
		LOGERROR("Could not create class key");
		hr = HRESULT_FROM_WIN32 (dwError);
	}
	if (SUCCEEDED(hr)) {
		hr = RegisterTypeLibrary ();
	}
	if (SUCCEEDED(hr)) {
		hr = RegisterAppIDEntry (hkeyRoot);
	}
	if (FAILED(hr)) {
		_com_error err(hr);
		LOGERROR("Problem registering AppID: %s", err.ErrorMessage());
	}
	return hr;
}

static HRESULT UnregisterTypeLibrary () {
	LOGTRACE("Called");
	return UnRegisterTypeLibForUser (LIBID_ComJvmCore, 1, 0, 0, SYS_WIN32);
}

static HRESULT UnregisterServer (HKEY hkeyRoot) {
	LOGTRACE("Called");
	HRESULT hr;
	LPOLESTR lpsz;
	hr = StringFromCLSID (CLSID_JvmSupport, &lpsz);
	if (FAILED (hr)) return hr;
	TCHAR szKey[MAX_PATH];
	hr = StringCbPrintf (szKey, sizeof (szKey), TEXT ("%s%ws"), TEXT ("SOFTWARE\\Classes\\CLSID\\"), lpsz);
	CoTaskMemFree (lpsz);
	if (FAILED (hr)) return hr;
	DWORD dwError = RegDeleteTree (hkeyRoot, szKey);
	hr = HRESULT_FROM_WIN32 (dwError);
	if (SUCCEEDED (hr)) {
		hr = UnregisterTypeLibrary ();
	}
	if (SUCCEEDED(hr)) {
		dwError = RegDeleteTree(hkeyRoot, TEXT("SOFTWARE\\Classes\\AppID\\{2027AC1B-AB3E-4BE5-A98F-CB499990EA0E}"));
		hr = HRESULT_FROM_WIN32 (dwError);
	}
	return hr;
}

HRESULT APIENTRY DllRegisterServer () {
	LOGTRACE("Called");
	HRESULT hr;
	hr = RegisterServer (HKEY_LOCAL_MACHINE);
	if (FAILED(hr)) {
		hr = RegisterServer(HKEY_CURRENT_USER);
	}
	// this was in an else block for HKCU
	LOGTRACE("Calling ProxyDllRegisterServer");
	hr = ProxyDllRegisterServer();
	return hr;
}

HRESULT APIENTRY DllUnregisterServer () {
	UnregisterServer (HKEY_CURRENT_USER);
	UnregisterServer (HKEY_LOCAL_MACHINE);
	LOGTRACE("Calling ProxyDllUnregisterServer");
	ProxyDllUnregisterServer ();
	return S_OK;
}

HRESULT APIENTRY DllCanUnloadNow () {
	if (g_lActiveObjects) {
		return S_FALSE;
	} else {
		return ProxyDllCanUnloadNow ();
	}
}

BOOL APIENTRY DllMain (
	HMODULE hModule,
	DWORD  dwReason,
	LPVOID lpReserved
	) {
	LOGTRACE("Calling ProxyDllMain");
	return ProxyDllMain (hModule, dwReason, lpReserved);
}

