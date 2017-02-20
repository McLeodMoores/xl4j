/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

#include "core_h.h"

/// <summary>Implementation of IJarFile.</summary>
///
/// <para>Instances of this are created by IJvmSupport#CreateClasspathEntry when
/// a single file is referenced.</para>
class CJarFile : public IJarFile {
private:
	volatile ULONG m_lRefCount;
	CRITICAL_SECTION m_cs;
	const _bstr_t m_bstrName;
	HANDLE m_hFile;
	~CJarFile ();
public:
	CJarFile (const _bstr_t &bstrName, HANDLE hFile);
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
    ULONG STDMETHODCALLTYPE AddRef ();
    ULONG STDMETHODCALLTYPE Release ();
	// IClasspathEntry
    /* [propget] */ HRESULT STDMETHODCALLTYPE get_Name ( 
        /* [retval][out] */ BSTR *pbstrName);
    HRESULT STDMETHODCALLTYPE AddToClasspath (
        /* [in] */ IClasspath *pClasspath);
	// IJarFile
    /* [propget] */ HRESULT STDMETHODCALLTYPE get_LocalPath ( 
        /* [retval][out] */ BSTR *pbstrPath);
    HRESULT STDMETHODCALLTYPE CacheInfo ( 
        /* [out] */ BSTR *pbstrHost,
        /* [out] */ long *plLength,
        /* [out] */ MIDL_uhyper *puTimestamp);
	HRESULT STDMETHODCALLTYPE Write (
		/* [in] */IFileWriter *pWriter);
};