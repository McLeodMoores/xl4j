/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

/// @file

#include "stdafx.h"
#include "core.h"
#include "ClassFolder.h"
#include "JarFile.h"
#include "MultiClasspathEntry.h"

static HRESULT CreateJarFile (const _bstr_t &bstrName, HANDLE hFile, IClasspathEntry **ppJarFile) {
	try {
		*ppJarFile = new CJarFile (bstrName, hFile);
		return S_OK;
	} catch (std::bad_alloc) {
		CloseHandle (hFile);
		return E_OUTOFMEMORY;
	}
}

static HRESULT CreateClassFolder (const _bstr_t &bstrName, IClasspathEntry **ppFolder) {
	try {
		*ppFolder = new CClassFolder (bstrName);
		return S_OK;
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}

/// <summary>Creates an IClasspathEntry instance.</summary>
///
/// <para>This is the ANSI implementation of IJvmSupport#CreateClasspathEntry.</para>
///
/// <param name="pszLocalPath">Path to create the instance for, local to the host where this IJvmSupport instance resides</param>
/// <param name="ppEntry">Receives the new IClasspathEntry instance</param>
/// <returns>S_OK if the entry was created, an error code otherwise</returns>
HRESULT COMJVM_CORE_API ComJvmCreateClasspathEntryA (/* [in] */ PCSTR pszLocalPath, /* [out][retval] */ IClasspathEntry **ppEntry) {
	if (!pszLocalPath) return E_POINTER;
	if (!ppEntry) return E_POINTER;
	*ppEntry = NULL;
	HRESULT hr;
	HANDLE hFind = NULL;
	CMultiClasspathEntry *pMultiClasspathEntry = NULL;
	try {
		_bstr_t bstrLocalPath (pszLocalPath);
		size_t cch = strlen (pszLocalPath);
		while (cch > 0) {
			cch--;
			if (pszLocalPath[cch] == '\\') break;
			if ((pszLocalPath[cch] == '?') || (pszLocalPath[cch] == '*')) {
				while (cch > 0) {
					cch--;
					if (pszLocalPath[cch] == '\\') break;
				}
				WIN32_FIND_DATAA wfd;
				HANDLE hFind = FindFirstFileA (pszLocalPath, &wfd);
				if (hFind == INVALID_HANDLE_VALUE) return HRESULT_FROM_WIN32 (GetLastError ());
				pMultiClasspathEntry = new CMultiClasspathEntry (bstrLocalPath);
				size_t cchBuffer = MAX_PATH + 1;
				std::vector<CHAR> vBuffer (cchBuffer);
				if (cch > 0) {
					cch++;
					CopyMemory (vBuffer.data (), pszLocalPath, cch * sizeof (CHAR));
				}
				do {
					if (wfd.cFileName[0] == '.') continue;
					size_t cchFileName = strlen (wfd.cFileName) + 1;
					size_t cchPath = cch + cchFileName;
					if (cchPath > cchBuffer) {
						cchBuffer = cchPath;
						vBuffer.resize (cchBuffer);
					}
					CopyMemory (vBuffer.data () + cch, wfd.cFileName, cchFileName * sizeof (CHAR));
					IClasspathEntry *pEntry;
					HRESULT hr = ComJvmCreateClasspathEntryA (vBuffer.data (), &pEntry);
					if (FAILED (hr)) _com_issue_error (hr);
					hr = pMultiClasspathEntry->Add (pEntry);
					pEntry->Release ();
					if (FAILED (hr)) _com_issue_error (hr);
				} while (FindNextFileA (hFind, &wfd));
				FindClose (hFind);
				*ppEntry = pMultiClasspathEntry;
				return S_OK;
			}
		}
		DWORD dwAttributes = GetFileAttributesA (pszLocalPath);
		if (dwAttributes == INVALID_FILE_ATTRIBUTES) {
			DWORD err = GetLastError ();
			return HRESULT_FROM_WIN32 (err);
		}
		if (dwAttributes & FILE_ATTRIBUTE_DIRECTORY) {
			hr = CreateClassFolder (bstrLocalPath, ppEntry);
		} else {
			HANDLE hFile = CreateFileA (pszLocalPath, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
			if (hFile == INVALID_HANDLE_VALUE) {
				hr = HRESULT_FROM_WIN32 (GetLastError ());
			} else {
				hr = CreateJarFile (bstrLocalPath, hFile, ppEntry);
			}
		}
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	} catch (_com_error &e) {
		hr = e.Error ();
	}
	if (hFind) FindClose (hFind);
	if (pMultiClasspathEntry) pMultiClasspathEntry->Release ();
	return hr;
}

/// <summary>Creates an IClasspathEntry instance.</summary>
///
/// <para>This is the wide-character implementation of IJvmSupport#CreateClasspathEntry.</para>
///
/// <param name="pszLocalPath">Path to create the instance for, local to the host where this IJvmSupport instance resides</param>
/// <param name="ppEntry">Receives the new IClasspathEntry instance</param>
/// <returns>S_OK if the entry was created, an error code otherwise</returns>
HRESULT COMJVM_CORE_API ComJvmCreateClasspathEntryW (/* [in] */ PCWSTR pszLocalPath, /* [out][retval] */ IClasspathEntry **ppEntry) {
	if (!pszLocalPath) return E_POINTER;
	if (!ppEntry) return E_POINTER;
	*ppEntry = NULL;
	HRESULT hr;
	HANDLE hFind = NULL;
	CMultiClasspathEntry *pMultiClasspathEntry = NULL;
	try {
		_bstr_t bstrLocalPath (pszLocalPath);
		size_t cch = wcslen (pszLocalPath);
		while (cch > 0) {
			cch--;
			if (pszLocalPath[cch] == '\\') break;
			if ((pszLocalPath[cch] == '?') || (pszLocalPath[cch] == '*')) {
				while (cch > 0) {
					cch--;
					if (pszLocalPath[cch] == '\\') break;
				}
				WIN32_FIND_DATAW wfd;
				hFind = FindFirstFileW (pszLocalPath, &wfd);
				if (hFind == INVALID_HANDLE_VALUE) return HRESULT_FROM_WIN32 (GetLastError ());
				pMultiClasspathEntry = new CMultiClasspathEntry (bstrLocalPath);
				size_t cchBuffer = MAX_PATH + 1;
				std::vector<WCHAR> vBuffer (cchBuffer);
				if (cch > 0) {
					cch++;
					CopyMemory (vBuffer.data (), pszLocalPath, cch * sizeof (WCHAR));
				}
				do {
					if (wfd.cFileName[0] == '.') continue;
					size_t cchFileName = wcslen (wfd.cFileName) + 1;
					size_t cchPath = cch + cchFileName;
					if (cchPath > cchBuffer) {
						cchBuffer = cchPath;
						vBuffer.resize (cchBuffer);
					}
					CopyMemory (vBuffer.data () + cch, wfd.cFileName, cchFileName * sizeof (WCHAR));
					IClasspathEntry *pEntry;
					HRESULT hr = ComJvmCreateClasspathEntryW (vBuffer.data (), &pEntry);
					if (FAILED (hr)) _com_issue_error (hr);
					hr = pMultiClasspathEntry->Add (pEntry);
					pEntry->Release ();
					if (FAILED (hr)) _com_issue_error (hr);
				} while (FindNextFileW (hFind, &wfd));
				FindClose (hFind);
				*ppEntry = pMultiClasspathEntry;
				return S_OK;
			}
		}
		DWORD dwAttributes = GetFileAttributesW (pszLocalPath);
		if (dwAttributes == INVALID_FILE_ATTRIBUTES) {
			DWORD err = GetLastError ();
			return HRESULT_FROM_WIN32 (err);
		}
		if (dwAttributes & FILE_ATTRIBUTE_DIRECTORY) {
			hr = CreateClassFolder (bstrLocalPath, ppEntry);
		} else {
			HANDLE hFile = CreateFileW (pszLocalPath, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
			if (hFile == INVALID_HANDLE_VALUE) {
				hr = HRESULT_FROM_WIN32 (GetLastError ());
			} else {
				hr = CreateJarFile (bstrLocalPath, hFile, ppEntry);
			}
		}
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	} catch (_com_error &e) {
		hr = e.Error ();
	}
	if (hFind) FindClose (hFind);
	if (pMultiClasspathEntry) pMultiClasspathEntry->Release ();
	return hr;
}
