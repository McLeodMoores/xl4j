/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "ClassFolder.h"
#include "internal.h"

static const _bstr_t Expand (const _bstr_t &bstrRelative) {
	_bstr_t bstrResult (bstrRelative);
	DWORD cchBuffer = MAX_PATH;
	PTSTR pszBuffer = NULL;
	try {
		do {
			pszBuffer = new TCHAR[cchBuffer];
			DWORD cchResult = GetFullPathName (bstrRelative, cchBuffer, pszBuffer, NULL);
			if (cchResult > cchBuffer) {
				delete pszBuffer;
				pszBuffer = NULL;
				cchBuffer = cchResult;
				continue;
			}
			bstrResult = pszBuffer;
			break;
		} while (TRUE);
	} catch (...) {
		// Ignore
	}
	delete pszBuffer;
	return bstrResult;
}

/// <summary>Creates a new instance.</summary>
///
/// <param name="bstrName">Local path to the folder</param>
CClassFolder::CClassFolder (const _bstr_t &bstrName)
: m_lRefCount (1), m_bstrName (bstrName), m_bstrPath (Expand (bstrName)) {
	IncrementActiveObjectCount ();
}

/// <summary>Destroys an instance.</summary>
///
/// <para>This should not be called directly but as a result of calling IUnknown#Release on the instance.</para>
CClassFolder::~CClassFolder() {
	assert (m_lRefCount == 0);
	DecrementActiveObjectCount ();
}

HRESULT STDMETHODCALLTYPE CClassFolder::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*>(this);
	} else if (riid == IID_IClasspathEntry) {
		*ppvObject = static_cast<IClasspathEntry*>(this);
	} else if (riid == IID_IClassFolder) {
		*ppvObject = static_cast<IClassFolder*>(this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef ();
	return S_OK;
}

ULONG STDMETHODCALLTYPE CClassFolder::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CClassFolder::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CClassFolder::get_Name ( 
    /* [retval][out] */ BSTR *pbstrName
	) {
	if (!pbstrName) return E_POINTER;
	try {
		*pbstrName = m_bstrName.copy ();
		return S_OK;
	} catch (_com_error &e) {
		return e.Error ();
	}
}

HRESULT STDMETHODCALLTYPE CClassFolder::AddToClasspath (
    /* [in] */ IClasspath *pClasspath
	) {
	if (!pClasspath) return E_POINTER;
	return pClasspath->AddFolder (this);
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CClassFolder::get_LocalPath (
    /* [retval][out] */ BSTR *pbstrPath
	) {
	if (!pbstrPath) return E_POINTER;
	try {
		*pbstrPath = m_bstrPath.copy ();
		return S_OK;
	} catch (_com_error &e) {
		return e.Error ();
	}
}

static HRESULT Scan (const _std_string_t &strPath, long *plFiles, long *plLength, MIDL_uhyper *puTimestampHash) {
	HRESULT hr;
	std::list<_std_string_t> oFiles;
	HANDLE hFind = NULL;
	try {
		_std_string_t strSearch (strPath);
		strSearch += TEXT ("\\*.*");
		WIN32_FIND_DATA wfd;
		HANDLE hFind = FindFirstFile (strSearch.data (), &wfd);
		if (hFind == INVALID_HANDLE_VALUE) {
			DWORD dwError = GetLastError ();
			if (dwError == ERROR_FILE_NOT_FOUND) {
				return S_OK;
			} else {
				return HRESULT_FROM_WIN32 (dwError);
			}
		}
		do {
			if (wfd.cFileName[0] == '.') {
				if (!wfd.cFileName[1]) continue; // .
				if ((wfd.cFileName[1] == '.') && !wfd.cFileName[2]) continue; // ..
			}
			oFiles.push_back (wfd.cFileName);
		} while (FindNextFile (hFind, &wfd));
		FindClose (hFind);
		hFind = NULL;
		oFiles.sort ();
		for (std::list<_std_string_t>::iterator itr = oFiles.begin (), end = oFiles.end (); itr != end; itr++) {
			strSearch = strPath;
			strSearch += TEXT ("\\");
			strSearch += *itr;
			if (!GetFileAttributesEx (strSearch.data (), GetFileExInfoStandard, &wfd)) _com_issue_error (HRESULT_FROM_WIN32 (GetLastError ()));
			(*puTimestampHash) = (*puTimestampHash) * 31 + (((MIDL_uhyper)wfd.ftLastWriteTime.dwHighDateTime << 32) | wfd.ftLastWriteTime.dwLowDateTime);
			if (wfd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) {
				hr = Scan (strSearch, plFiles, plLength, puTimestampHash);
				if (FAILED (hr)) _com_issue_error (hr);
			} else {
				(*plFiles)++;
				(*plLength) += wfd.nFileSizeLow;
			}
		}
		hr = S_OK;
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	} catch (_com_error &e) {
		hr = e.Error ();
	}
	if (hFind) FindClose (hFind);
	return hr;
}

HRESULT STDMETHODCALLTYPE CClassFolder::CacheInfo (
    /* [out] */ BSTR *pbstrHost,
    /* [out] */ long *plFiles,
    /* [out] */ long *plLength,
    /* [out] */ MIDL_uhyper *puTimestampHash
	) {
	if (!pbstrHost) return E_POINTER;
	if (!plFiles) return E_POINTER;
	if (!plLength) return E_POINTER;
	if (!puTimestampHash) return E_POINTER;
	*plFiles = 0;
	*plLength = 0;
	*puTimestampHash = 0;
	HRESULT hr;
	if (FAILED (hr = Scan ((PCTSTR)m_bstrPath, plFiles, plLength, puTimestampHash))) return hr;
	if (FAILED (hr = ComJvmGetHostB (pbstrHost))) return hr;
	return S_OK;
}

HRESULT WriteFileContents (HANDLE hFile, IFileWriter *pWriter, DWORD dwBuffer);

static HRESULT WriteFolderContents (const _std_string_t &strPath, IDirectoryWriter *pWriter, DWORD dwBuffer) {
	HRESULT hr;
	HANDLE hFind = NULL;
	try {
		_std_string_t strSearch (strPath);
		strSearch += TEXT ("\\*.*");
		WIN32_FIND_DATA wfd;
		hFind = FindFirstFile (strSearch.data (), &wfd);
		if (hFind == INVALID_HANDLE_VALUE) {
			DWORD dwError = GetLastError ();
			if (dwError == ERROR_FILE_NOT_FOUND) {
				return S_OK;
			} else {
				return HRESULT_FROM_WIN32 (dwError);
			}
		}
		do {
			if (wfd.cFileName[0] == '.') {
				if (!wfd.cFileName[1]) continue; // .
				if ((wfd.cFileName[1] == '.') && !wfd.cFileName[2]) continue; // ..
			}
			strSearch = strPath;
			strSearch += TEXT ("\\");
			strSearch += wfd.cFileName;
			_bstr_t bstr (wfd.cFileName);
			if (wfd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) {
				IDirectoryWriter *pDirectory;
				if (SUCCEEDED (hr = pWriter->Directory (bstr, &pDirectory))) {
					hr = WriteFolderContents (strSearch, pDirectory, dwBuffer);
					pDirectory->Release ();
				}
			} else {
				HANDLE hFile = CreateFile (strSearch.data (), GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
				if (hFile == INVALID_HANDLE_VALUE) _com_issue_error (HRESULT_FROM_WIN32 (GetLastError ()));
				IFileWriter *pFile;
				if (SUCCEEDED (hr = pWriter->File (bstr, &pFile))) {
					hr = WriteFileContents (hFile, pFile, dwBuffer);
					pFile->Release ();
				}
				CloseHandle (hFile);
			}
			if (FAILED (hr)) _com_issue_error (hr);
		} while (FindNextFile (hFind, &wfd));
		hr = S_OK;
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	} catch (_com_error &e) {
		hr = e.Error ();
	}
	if (hFind) FindClose (hFind);
	return hr;
}

HRESULT STDMETHODCALLTYPE CClassFolder::Write ( 
    /* [in] */ IDirectoryWriter *pWriter
	) {
	if (!pWriter) return E_POINTER;
	return WriteFolderContents ((PCTSTR)m_bstrPath, pWriter, 65536);
}
