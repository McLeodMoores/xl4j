#include "stdafx.h"
#include "Debug.h"
#include "FileUtils.h"
#include "Shlwapi.h"

FileUtils::FileUtils () {
}

HRESULT FileUtils::GetAddinAbsolutePath (wchar_t *szFullPath, size_t cFullPath, const wchar_t *szFileName) {
	HRESULT hr;
	wchar_t szRawPath[MAX_PATH]; 
	if (FAILED (hr = GetAddinDirectory (szRawPath, MAX_PATH))) {
		LOGERROR ("GetAddinDirectory failed");
		return hr;
	}
	hr = StringCchCatW (szRawPath, MAX_PATH, szFileName);
	
	if (PathCanonicalize (szFullPath, szRawPath)) {
		return S_OK;
	}
	LOGERROR ("PathCanonicalize failed");
	return HRESULT_FROM_WIN32 (GetLastError ());
}

HRESULT FileUtils::GetAddinDirectory (wchar_t *szDirectory, size_t cDirectory) {
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
	LOGTRACE ("Path with drive is %s", szDirectory);
	if (_wsplitpath_s (&szDirPath[0], NULL, 0, &szDirectory[2], cDirectory - 2, NULL, 0, NULL, 0)) {
		LOGERROR ("_wsplitpath_s failed");
		return HRESULT_FROM_WIN32 (GetLastError ());
	}
	LOGTRACE ("Path with drive + dir is %s", szDirectory);
	return S_OK;
}

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

HRESULT FileUtils::GetDllFileName (wchar_t *szFilename, size_t cFilename) {
	HMODULE hModule;
	if (GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, (LPCTSTR)FileUtils::GetDllFileName, &hModule)) {
		ZeroMemory (szFilename, cFilename); // to please the code analyzer gods
		DWORD dwLength = GetModuleFileName (hModule, szFilename, cFilename);
	} else { // there was an error
		LPWSTR pErrorMsg;
		FormatMessage (FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM, NULL, GetLastError (), 0, (LPWSTR)&pErrorMsg, 0, NULL);
		OutputDebugStringW (pErrorMsg);
		LocalFree (pErrorMsg);
		return HRESULT_FROM_WIN32 (GetLastError ());
	}
	FreeLibrary (hModule);
	return S_OK;
}

HRESULT FileUtils::GetTemporaryFileName(const wchar_t *pszLeafFilename, wchar_t *pszBuffer, size_t cBuffer) {
	if (!GetTempPathW(cBuffer, pszBuffer)) {
		OutputDebugStringW(TEXT("GetTempPathW failed"));
		return HRESULT_FROM_WIN32(GetLastError());
	}
	return StringCchCatW(pszBuffer, cBuffer, pszLeafFilename);
}

bool FileUtils::FileExists(const wchar_t *szPath) {
	DWORD dwAttrib = GetFileAttributes(szPath);
	return (dwAttrib != INVALID_FILE_ATTRIBUTES && !(dwAttrib & FILE_ATTRIBUTE_DIRECTORY));
}

