/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "utils/Debug.h"
#include "AddinFileUtils.h"
#include "Shlwapi.h"

AddinFileUtils::AddinFileUtils () {
}

/**
 * Get an canonical, absolute path to a file relative to the add-in location.
 * @param szFullPath pointer to buffer to take a null terminated wide C-string of the resulting path
 * @param cFullPath the size of the buffer, in characters (recommended to use at minimum MAX_PATH)
 * @param szFileName point to null-terminated wide C-string of the add-in relative path
 * @return result code
 */
HRESULT AddinFileUtils::GetAddinAbsolutePath (wchar_t *szFullPath, size_t cFullPath, const wchar_t *szFileName) {
	HRESULT hr;
	wchar_t szRawPath[MAX_PATH]; 
	if (FAILED (hr = GetAddinDirectory (szRawPath, MAX_PATH))) {
		LOGERROR ("GetAddinDirectory failed: %s", HRESULT_TO_STR(hr));
		return hr;
	}
	hr = StringCchCatW (szRawPath, MAX_PATH, szFileName);
	wchar_t szResult[MAX_PATH];
	// We would like to use PathCchCanonicalize here, but it's only Win8+
	if (PathCanonicalize (szResult, szRawPath)) {
		if (FAILED(hr = StringCchCopyW(szFullPath, cFullPath, szResult))) {
			LOGERROR("Buffer too small for path: %s ", HRESULT_TO_STR(hr));
			return hr;
		}
		return S_OK;
	}
	LOGERROR ("PathCanonicalize failed: %s", HRESULT_TO_STR(hr));
	return HRESULT_FROM_WIN32 (GetLastError ());
}

/**
 * Get the add-in directory path.  This is the path of the XLL file minus the actual xll filename.
 * @param szDirectory pointer to a buffer into which the funciton will attempt to write a null-terminated wide c-string
 * @param cDirectory size of the buffer, in characters, should be at least MAX_PATH
 * @return result code E_FAIL indicates buffer to small
 */
HRESULT AddinFileUtils::GetAddinDirectory (wchar_t *szDirectory, size_t cDirectory) {
	wchar_t szDirPath[MAX_PATH];
	HRESULT hr;
	if (FAILED (hr = GetDllFileName (&szDirPath[0], MAX_PATH))) {
		LOGERROR ("GetDllFileName failed");
		return hr;
	}
	if (cDirectory < 3) { // just to protect the assumption that the buffer is at least 3 chars in the code below.
		LOGERROR ("Buffer to small");
		return E_FAIL;
	}
	if (_wsplitpath_s (&szDirPath[0], szDirectory, 3, NULL, 0, NULL, 0, NULL, 0)) {
		LOGERROR ("_wsplitpath_s failed");
		return HRESULT_FROM_WIN32 (GetLastError ());
	}
	if (_wsplitpath_s (&szDirPath[0], NULL, 0, &szDirectory[2], cDirectory - 2, NULL, 0, NULL, 0)) {
		LOGERROR ("_wsplitpath_s failed");
		return HRESULT_FROM_WIN32 (GetLastError ());
	}
	return S_OK;
}

/**
 * Extract the directory path from a fully qualified file path.
 * @param szDirectory pointer to a buffer to hold the resulting path as a null terminated wide C-string
 * @param cDirectory the size of the buffer, in characters
 * @param szFullPath pointer to a null terminated wide C-string containing the full file path
 * @return result code E_FAIL means buffer too small
 */
HRESULT AddinFileUtils::GetDirectoryFromFullPath(wchar_t *szDirectory, size_t cDirectory, const wchar_t *szFullPath) {
	if (cDirectory < 3) { // just to protect the assumption that the buffer is at least 3 chars in the code below.
		LOGERROR("Buffer to small");
		return E_FAIL;
	}
	if (_wsplitpath_s(&szFullPath[0], szDirectory, 3, NULL, 0, NULL, 0, NULL, 0)) {
		LOGERROR("_wsplitpath_s failed");
		return HRESULT_FROM_WIN32(GetLastError());
	}
	LOGTRACE("Path with drive is %s", szDirectory);
	if (_wsplitpath_s(&szFullPath[0], NULL, 0, &szDirectory[2], cDirectory - 2, NULL, 0, NULL, 0)) {
		LOGERROR("_wsplitpath_s failed");
		return HRESULT_FROM_WIN32(GetLastError());
	}
	LOGTRACE("Path with drive + dir is %s", szDirectory);
	return S_OK;
}

/**
 * Get the file path of this DLL (utils.dll).  Any buffer is zeroed.
 * @param szFilename pointer to a buffer to hold the null-terminated wide C-string file name of the DLL
 * @param cFilename size of the buffer, in characters
 * @return result code
 */
HRESULT AddinFileUtils::GetDllFileName (wchar_t *szFilename, size_t cFilename) {
	HMODULE hModule;
	if (GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, (LPCTSTR)AddinFileUtils::GetDllFileName, &hModule)) {
		ZeroMemory (szFilename, cFilename); // to please the code analyzer gods
		DWORD dwLength = GetModuleFileName (hModule, szFilename, cFilename);
	} else { // there was an error
		LOGERROR("Error getting module filename: %s", GETLASTERROR_TO_STR());
		return GETLASTERROR_TO_HRESULT();
	}
	FreeLibrary (hModule);
	return S_OK;
}

	

