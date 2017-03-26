/*
* Copyright 2014-present by McLeod Moores Software Limited.
* See distribution for license.
*/

#include "stdafx.h"
#include "Debug.h"
#include "FileUtils.h"
#include "Shlwapi.h"

FileUtils::FileUtils() {
}

/**
* Get an canonical, absolute path to a file relative to the add-in location.
* @param szFullPath pointer to buffer to take a null terminated wide C-string of the resulting path
* @param cFullPath the size of the buffer, in characters (recommended to use at minimum MAX_PATH)
* @param szFileName point to null-terminated wide C-string of the add-in relative path
* @return result code
*/
HRESULT FileUtils::GetAddinAbsolutePath(wchar_t *szFullPath, size_t cFullPath, const wchar_t *szFileName) {
	HRESULT hr;
	wchar_t szRawPath[MAX_PATH];
	if (FAILED(hr = GetAddinDirectory(szRawPath, MAX_PATH))) {
		LOGERROR("GetAddinDirectory failed: %s", HRESULT_TO_STR(hr));
		return hr;
	}
	hr = StringCchCatW(szRawPath, MAX_PATH, szFileName);
	wchar_t szResult[MAX_PATH];
	// We would like to use PathCchCanonicalize here, but it's only Win8+
	if (PathCanonicalize(szResult, szRawPath)) {
		if (FAILED(hr = StringCchCopyW(szFullPath, cFullPath, szResult))) {
			LOGERROR("Buffer too small for path: %s ", HRESULT_TO_STR(hr));
			return hr;
		}
		return S_OK;
	}
	LOGERROR("PathCanonicalize failed: %s", HRESULT_TO_STR(hr));
	return HRESULT_FROM_WIN32(GetLastError());
}

/**
* Get the add-in directory path.  This is the path of the XLL file minus the actual xll filename.
* @param szDirectory pointer to a buffer into which the funciton will attempt to write a null-terminated wide c-string
* @param cDirectory size of the buffer, in characters, should be at least MAX_PATH
* @return result code E_FAIL indicates buffer to small
*/
HRESULT FileUtils::GetAddinDirectory(wchar_t *szDirectory, size_t cDirectory) {
	wchar_t szDirPath[MAX_PATH];
	HRESULT hr;
	if (FAILED(hr = GetDllFileName(&szDirPath[0], MAX_PATH))) {
		LOGERROR("GetDllFileName failed");
		return hr;
	}
	if (cDirectory < 3) { // just to protect the assumption that the buffer is at least 3 chars in the code below.
		LOGERROR("Buffer to small");
		return E_FAIL;
	}
	if (_wsplitpath_s(&szDirPath[0], szDirectory, 3, NULL, 0, NULL, 0, NULL, 0)) {
		LOGERROR("_wsplitpath_s failed");
		return HRESULT_FROM_WIN32(GetLastError());
	}
	if (_wsplitpath_s(&szDirPath[0], NULL, 0, &szDirectory[2], cDirectory - 2, NULL, 0, NULL, 0)) {
		LOGERROR("_wsplitpath_s failed");
		return HRESULT_FROM_WIN32(GetLastError());
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
HRESULT FileUtils::GetDirectoryFromFullPath(wchar_t *szDirectory, size_t cDirectory, const wchar_t *szFullPath) {
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
HRESULT FileUtils::GetDllFileName(wchar_t *szFilename, size_t cFilename) {
	HMODULE hModule;
	if (GetModuleHandleEx(GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, (LPCTSTR)FileUtils::GetDllFileName, &hModule)) {
		ZeroMemory(szFilename, cFilename); // to please the code analyzer gods
		DWORD dwLength = GetModuleFileName(hModule, szFilename, cFilename);
	} else { // there was an error
		LOGERROR("Error getting module filename: %s", GETLASTERROR_TO_STR());
		return GETLASTERROR_TO_HRESULT();
	}
	FreeLibrary(hModule);
	return S_OK;
}

/**
* Append a file name to the temporary directory path.
* @param pszLeafFilename const pointer to a null-terminated wide C-string containing the path relative to the temporary directory
* @param pszBuffer pointer to a buffer to hold the resulting null-terminated wide C-string fully qualified path
* @param cBuffer the size of the buffer, in characters.  Recommended to be minimum of MAX_PATH.
* @return result code
*/
HRESULT FileUtils::GetTemporaryFileName(const wchar_t *pszLeafFilename, wchar_t *pszBuffer, size_t cBuffer) {
	if (!GetTempPathW(cBuffer, pszBuffer)) {
		OutputDebugStringW(TEXT("GetTempPathW failed"));
		return HRESULT_FROM_WIN32(GetLastError());
	}
	return StringCchCatW(pszBuffer, cBuffer, pszLeafFilename);
}

/**
* Does the file path exist as a file (and is also NOT a directory).
* @param szPath pointer to a null-terminated wide C-string containing the path to check
* @return true, if the file exists and is not a directory, false otherwise
*/
bool FileUtils::FileExists(const wchar_t *szPath) {
	DWORD dwAttrib = GetFileAttributes(szPath);
	return (dwAttrib != INVALID_FILE_ATTRIBUTES && !(dwAttrib & FILE_ATTRIBUTE_DIRECTORY));
}

/**
* Get the size of a file (which is also NOT a directory).
* The file size is not written (i.e. it's value is undefined) if the file is not accessible or some
* error occurs, except if the file does not exist, in which case, zero is written.
* @param szPath pointer to a null-terminated wide C-string containing the path to the file
* @param pSize pointer to a LARGE_INTEGER to which the file size is written.   See above for caveats.
* @return result code, typically S_OK or ERROR_FILE_NOT_FOUND, but others are possible (e.g. permissions)
*/
HRESULT FileUtils::FileSize(const wchar_t *szPath, PLARGE_INTEGER pSize) {
	if (FileUtils::FileExists(szPath)) {
		HANDLE hFile = CreateFile(szPath, GENERIC_READ,
			FILE_SHARE_READ,
			NULL,
			OPEN_ALWAYS,
			FILE_ATTRIBUTE_NORMAL,
			NULL);
		if (hFile == INVALID_HANDLE_VALUE) {
			LOGERROR("Could not open log file to find size: %s", GETLASTERROR_TO_STR());
			return GETLASTERROR_TO_HRESULT();
		}
		if (!GetFileSizeEx(hFile, pSize)) {
			LOGERROR("Could not get log file size: %s", GETLASTERROR_TO_STR());
			HRESULT hr = GETLASTERROR_TO_HRESULT();
			CloseHandle(hFile);
			return hr;
		}
		CloseHandle(hFile);
		return S_OK;
	} else {
		pSize->QuadPart = 0LL;
		return ERROR_FILE_NOT_FOUND;
	}
}


