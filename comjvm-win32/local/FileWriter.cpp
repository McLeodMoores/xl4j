/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "FileWriter.h"
#include "Internal.h"

CFileWriter::CFileWriter (HANDLE hFile)
: m_lRefCount (1), m_hFile (hFile) {
	IncrementActiveObjectCount ();
}

CFileWriter::~CFileWriter () {
	assert (m_lRefCount == 0);
	Close ();
	DecrementActiveObjectCount ();
}

HRESULT STDMETHODCALLTYPE CFileWriter::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*>(this);
	} else if (riid == IID_IFileWriter) {
		*ppvObject = static_cast<IFileWriter*>(this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef ();
	return S_OK;
}

ULONG STDMETHODCALLTYPE CFileWriter::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CFileWriter::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}

HRESULT STDMETHODCALLTYPE CFileWriter::Close () {
	if (m_hFile == INVALID_HANDLE_VALUE) {
		return S_FALSE;
	} else {
		CloseHandle (m_hFile);
		m_hFile = INVALID_HANDLE_VALUE;
		return S_OK;
	}
}

HRESULT STDMETHODCALLTYPE CFileWriter::Write ( 
    /* [in] */ BYTE_SIZEDARR *pData
	) {
	if (!pData) return E_POINTER;
	if (!pData->clSize || !pData->pData) return E_INVALIDARG;
	PBYTE pBuffer = pData->pData;
	DWORD cbBuffer = pData->clSize;
	DWORD dwWritten;
	while (WriteFile (m_hFile, pBuffer, cbBuffer, &dwWritten, NULL)) {
		if (dwWritten == 0) return E_FAIL;
		if (dwWritten < cbBuffer) {
			cbBuffer -= dwWritten;
			pBuffer += dwWritten;
		} else {
			return S_OK;
		}
	}
	return HRESULT_FROM_WIN32 (GetLastError ());
}
