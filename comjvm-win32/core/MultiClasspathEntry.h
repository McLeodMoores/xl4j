/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

#include "ClasspathEntries.h"

/// <summary>Implementation of IClasspathEntry used for paths containing wild-card characters.</summary>
///
/// <para>If a path contains a wild-card then it might not correspond to a single JAR file or class
/// folder. This implementation will make repeated calls to IClasspath, when requested, for any files
/// or folders that are matched by the wildcard pattern.</para>
class CMultiClasspathEntry : public IClasspathEntry {
private:
	volatile ULONG m_lRefCount;
	const _bstr_t m_bstrName;
	CClasspathEntriesImpl m_oImpl;
	~CMultiClasspathEntry ();
public:
	CMultiClasspathEntry (const _bstr_t &bstrName);
	HRESULT Add (IClasspathEntry *pEntry);
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
};