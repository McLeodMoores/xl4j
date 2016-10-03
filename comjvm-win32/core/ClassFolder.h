/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

#include "core_h.h"

/// <summary>Implementation of IClassFolder.</summary>
class CClassFolder : public IClassFolder {
private:
	volatile ULONG m_lRefCount;
	const _bstr_t m_bstrName;
	const _bstr_t m_bstrPath;
	~CClassFolder ();
public:
	CClassFolder (const _bstr_t &bstrName);
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
	// IClassFolder
    /* [propget] */ HRESULT STDMETHODCALLTYPE get_LocalPath (
        /* [retval][out] */ BSTR *pbstrPath);
    HRESULT STDMETHODCALLTYPE CacheInfo (
        /* [out] */ BSTR *pbstrHost,
        /* [out] */ long *plFiles,
        /* [out] */ long *plLength,
        /* [out] */ MIDL_uhyper *puTimestampHash);
    HRESULT STDMETHODCALLTYPE Write ( 
        /* [in] */ IDirectoryWriter *pWriter);
};