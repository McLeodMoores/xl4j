/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

#include "../core/core_h.h"

class CDirectoryWriter : public IDirectoryWriter {
private:
	volatile ULONG m_lRefCount;
	const _std_string_t m_strPath;
	~CDirectoryWriter ();
public:
	CDirectoryWriter (const _std_string_t &strPath);
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
    ULONG STDMETHODCALLTYPE AddRef ();
    ULONG STDMETHODCALLTYPE Release ();
	// IFileSystemWriter
    HRESULT STDMETHODCALLTYPE Directory ( 
        /* [in] */ BSTR bstrFolder,
        /* [retval][out] */ IDirectoryWriter **ppWriter);
    HRESULT STDMETHODCALLTYPE File ( 
        /* [in] */ BSTR bstrFile,
        /* [retval][out] */ IFileWriter **ppWriter);
};