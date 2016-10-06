/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "JarFile.h"
#include "core.h"
#include "internal.h"

/// <summary>Creates a new instance.</summary>
///
/// <param name="bstrName">Local path to the JAR file</param>
/// <param name="hFile">Handle to the file; this instance will take ownership of the handle</param>
CJarFile::CJarFile (const _bstr_t &bstrName, HANDLE hFile)
: m_lRefCount (1), m_bstrName (bstrName), m_hFile (hFile) {
	IncrementActiveObjectCount ();
	InitializeCriticalSection (&m_cs);
}

/// <summary>Destroys an instance.</summary>
///
/// <para>This should not be called directly but as a result of calling IUnknown#Release on the instance.</para>
CJarFile::~CJarFile () {
	assert (m_lRefCount == 0);
	CloseHandle (m_hFile);
	DeleteCriticalSection (&m_cs);
	DecrementActiveObjectCount ();
}

HRESULT STDMETHODCALLTYPE CJarFile::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*>(this);
	} else if (riid == IID_IClasspathEntry) {
		*ppvObject = static_cast<IClasspathEntry*>(this);
	} else if (riid == IID_IJarFile) {
		*ppvObject = static_cast<IJarFile*>(this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef ();
	return S_OK;
}

ULONG STDMETHODCALLTYPE CJarFile::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CJarFile::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CJarFile::get_Name ( 
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

HRESULT STDMETHODCALLTYPE CJarFile::AddToClasspath (
    /* [in] */ IClasspath *pClasspath
	) {
	if (!pClasspath) return E_POINTER;
	return pClasspath->AddJar (this);
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CJarFile::get_LocalPath ( 
    /* [retval][out] */ BSTR *pbstrPath
	) {
	if (!pbstrPath) return E_POINTER;
	try {
		DWORD cchPath = MAX_PATH;
		do {
			std::vector<TCHAR> vPath (cchPath);
			DWORD cchResult = GetFinalPathNameByHandle (m_hFile, vPath.data (), cchPath, VOLUME_NAME_DOS);
			if (cchResult >= cchPath) {
				cchPath = cchResult + 1;
				continue;
			}
			_bstr_t bstrPath (_tcsncmp (vPath.data (), TEXT ("\\\\?\\"), 4) ? vPath.data () : vPath.data () + 4);
			*pbstrPath = bstrPath.Detach ();
			return S_OK;
		} while (TRUE);
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	} catch (_com_error &e) {
		return e.Error ();
	}
}

HRESULT STDMETHODCALLTYPE CJarFile::CacheInfo ( 
    /* [out] */ BSTR *pbstrHost,
    /* [out] */ long *plLength,
    /* [out] */ MIDL_uhyper *puTimestamp
	) {
	if (!pbstrHost) return E_POINTER;
	if (!plLength) return E_POINTER;
	if (!puTimestamp) return E_POINTER;
	BY_HANDLE_FILE_INFORMATION info;
	if (!GetFileInformationByHandle (m_hFile, &info)) return HRESULT_FROM_WIN32 (GetLastError ());
	HRESULT hr;
	if (FAILED (hr = ComJvmGetHostB (pbstrHost))) return hr;
	*plLength = info.nFileSizeLow;
	*puTimestamp = ((MIDL_uhyper)info.ftLastWriteTime.dwHighDateTime << 32) | (MIDL_uhyper)info.ftLastWriteTime.dwLowDateTime;
	return S_OK;
}

HRESULT WriteFileContents (HANDLE hFile, IFileWriter *pWriter, DWORD dwBufferSize) {
	if (hFile == INVALID_HANDLE_VALUE) return E_INVALIDARG;
	if (!pWriter) return E_POINTER;
	SetFilePointer (hFile, 0, NULL, FILE_BEGIN);
	try {
		std::vector<BYTE> vBuffer (dwBufferSize);
		DWORD dwRead;
		while (ReadFile (hFile, vBuffer.data (), dwBufferSize, &dwRead, NULL)) {
			if (dwRead > 0) {
				BYTE_SIZEDARR bsa;
				bsa.clSize = dwRead;
				bsa.pData = vBuffer.data ();
				HRESULT hr = pWriter->Write (&bsa);
				if (FAILED (hr)) return hr;
			} else {
				return S_OK;
			}
		}
		return HRESULT_FROM_WIN32 (GetLastError ());
	} catch (std::bad_alloc ){
		return E_OUTOFMEMORY;
	}
}

HRESULT STDMETHODCALLTYPE CJarFile::Write ( 
    /* [in] */ IFileWriter *pWriter
	) {
	EnterCriticalSection (&m_cs);
	HRESULT hr = WriteFileContents (m_hFile, pWriter, 65536);
	LeaveCriticalSection (&m_cs);
	return hr;
}
