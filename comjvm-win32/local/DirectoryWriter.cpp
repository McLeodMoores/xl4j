/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "DirectoryWriter.h"
#include "local.h"
#include "Internal.h"

CDirectoryWriter::CDirectoryWriter (const _std_string_t &strPath)
: m_lRefCount (1), m_strPath (strPath) {
	IncrementActiveObjectCount ();
}

CDirectoryWriter::~CDirectoryWriter () {
	assert (m_lRefCount == 0);
	DecrementActiveObjectCount ();
}

HRESULT STDMETHODCALLTYPE CDirectoryWriter::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*>(this);
	} else if (riid == IID_IDirectoryWriter) {
		*ppvObject = static_cast<IDirectoryWriter*>(this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef ();
	return S_OK;
}

ULONG STDMETHODCALLTYPE CDirectoryWriter::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CDirectoryWriter::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}

HRESULT STDMETHODCALLTYPE CDirectoryWriter::Directory ( 
    /* [in] */ BSTR bstrFolder,
    /* [retval][out] */ IDirectoryWriter **ppWriter
	) {
	if (!bstrFolder) return E_POINTER;
	if (!ppWriter) return E_POINTER;
	try {
		UINT uLen = SysStringLen (bstrFolder), u;
		if (uLen == 0) return E_INVALIDARG;
		if (bstrFolder[0] == '.') {
			if (uLen == 1) {
				AddRef ();
				*ppWriter = this;
				return S_OK;
			}
			if ((uLen == 2) && (bstrFolder[1] == '.')) return E_INVALIDARG;
		}
		for (u = 0; u < uLen; u++) {
			if (bstrFolder[u] == '\\') return E_INVALIDARG;
		}
		return ComJvmCreateDirectoryWriter ((m_strPath + TEXT ("\\") + (PCTSTR)_bstr_t (bstrFolder)).data (), ppWriter);
	} catch (_com_error &e) {
		return e.Error ();
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}

HRESULT STDMETHODCALLTYPE CDirectoryWriter::File ( 
    /* [in] */ BSTR bstrFile,
    /* [retval][out] */ IFileWriter **ppWriter
	) {
	if (!bstrFile) return E_POINTER;
	if (!ppWriter) return E_POINTER;
	try {
		UINT uLen = SysStringLen (bstrFile), u;
		if (uLen == 0) return E_INVALIDARG;
		if (bstrFile[0] == '.') {
			if (uLen == 1) return E_INVALIDARG;
			if ((uLen == 2) && (bstrFile[1] == '.')) return E_INVALIDARG;
		}
		for (u = 0; u < uLen; u++) {
			if (bstrFile[u] == '\\') return E_INVALIDARG;
		}
		return ComJvmCreateFileWriter ((m_strPath + TEXT ("\\") + (PCTSTR)_bstr_t (bstrFile)).data (), ppWriter);
	} catch (_com_error &e) {
		return e.Error ();
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}
