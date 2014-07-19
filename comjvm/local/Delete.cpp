/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "Delete.h"

HRESULT DeleteFilesAndFoldersA (PCSTR pszPath) {
	HRESULT hr;
	HANDLE hFind = NULL;
	try {
		DWORD dwAttr = GetFileAttributesA (pszPath);
		if (dwAttr == INVALID_FILE_ATTRIBUTES) return S_FALSE;
		if (dwAttr & FILE_ATTRIBUTE_DIRECTORY) {
			WIN32_FIND_DATAA wfd;
			std::string strSearch (pszPath);
			strSearch += "\\*.*";
			hFind = FindFirstFileA (strSearch.data (), &wfd);
			if (hFind == INVALID_HANDLE_VALUE) {
				DWORD dwError = GetLastError ();
				if (dwError != ERROR_FILE_NOT_FOUND) return HRESULT_FROM_WIN32 (dwError);
			} else {
				do {
					if (wfd.cFileName[0] == '.') {
						if (!wfd.cFileName[1]) continue;
						if ((wfd.cFileName[1] == '.') && !wfd.cFileName[2]) continue;
					}
					strSearch = pszPath;
					strSearch.push_back ('\\');
					strSearch += wfd.cFileName;
					hr = DeleteFilesAndFoldersA (strSearch.data ());
					if (FAILED (hr)) _com_raise_error (hr);
				} while (FindNextFileA (hFind, &wfd));
				FindClose (hFind);
				hFind = NULL;
			}
			if (RemoveDirectoryA (pszPath)) {
				hr = S_OK;
			} else {
				hr = HRESULT_FROM_WIN32 (GetLastError ());
			}
		} else {
			if (DeleteFileA (pszPath)) {
				hr = S_OK;
			} else {
				hr = HRESULT_FROM_WIN32 (GetLastError ());
			}
		}
	} catch (_com_error &e) {
		hr = e.Error ();
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	}
	if (hFind) FindClose (hFind);
	return hr;
}

HRESULT DeleteFilesAndFoldersW (PCWSTR pszPath) {
	HRESULT hr;
	HANDLE hFind = NULL;
	try {
		DWORD dwAttr = GetFileAttributesW (pszPath);
		if (dwAttr == INVALID_FILE_ATTRIBUTES) return S_FALSE;
		if (dwAttr & FILE_ATTRIBUTE_DIRECTORY) {
			WIN32_FIND_DATAW wfd;
			std::wstring strSearch (pszPath);
			strSearch += L"\\*.*";
			hFind = FindFirstFileW (strSearch.data (), &wfd);
			if (hFind == INVALID_HANDLE_VALUE) {
				DWORD dwError = GetLastError ();
				if (dwError != ERROR_FILE_NOT_FOUND) return HRESULT_FROM_WIN32 (dwError);
			} else {
				do {
					if (wfd.cFileName[0] == '.') {
						if (!wfd.cFileName[1]) continue;
						if ((wfd.cFileName[1] == '.') && !wfd.cFileName[2]) continue;
					}
					strSearch = pszPath;
					strSearch.push_back ('\\');
					strSearch += wfd.cFileName;
					HRESULT hr = DeleteFilesAndFoldersW (strSearch.data ());
					if (FAILED (hr)) _com_raise_error (hr);
				} while (FindNextFileW (hFind, &wfd));
				FindClose (hFind);
				hFind = NULL;
			}
			if (RemoveDirectoryW (pszPath)) {
				hr = S_OK;
			} else {
				hr = HRESULT_FROM_WIN32 (GetLastError ());
			}
		} else {
			if (DeleteFileW (pszPath)) {
				hr = S_OK;
			} else {
				hr = HRESULT_FROM_WIN32 (GetLastError ());
			}
		}
	} catch (_com_error &e) {
		hr = e.Error ();
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	}
	if (hFind) FindClose (hFind);
	return hr;
}
