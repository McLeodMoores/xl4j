/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once
#include "stdafx.h"
#include "core_h.h"

///	<summary>Implementation of an IClasspathEntry collection.</summary>
///
/// <para>This is the underlying implementation of CClasspathEntries.</para>
///
/// <para>This implementation is not safe for use by multiple threads, except as a read-only store.</para>
class CJvmOptionEntriesImpl {
private:
	std::vector<_bstr_t> m_vpBuffer;
public:
	CJvmOptionEntriesImpl ();
	~CJvmOptionEntriesImpl ();
	long get_Count () const;
	HRESULT get_Item (long lIndex, BSTR *ppEntry) const;
	HRESULT put_Item (long lIndex, BSTR pEntry);
	HRESULT Add (BSTR pEntry);
	void Clear ();
	HRESULT Remove (long lIndex);
};

/// <summary>Implementation of ICommandLineOptions.</summary>
///
/// <para>This implementation is backed by a CCommandLineOptionsImpl instance that is protected by a critical section.</para>
class CJvmOptionEntries : public IJvmOptionEntries {
private:
	CRITICAL_SECTION m_cs;
	volatile ULONG m_lRefCount;
	CJvmOptionEntriesImpl m_oImpl;
	~CJvmOptionEntries ();
public:
	CJvmOptionEntries ();
	static HRESULT Append (IJvmOptionEntries *pSource, IJvmOptionEntries *pDest);
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
    ULONG STDMETHODCALLTYPE AddRef ();
    ULONG STDMETHODCALLTYPE Release ();
	// IClasspathEntries
    /* [propget] */ HRESULT STDMETHODCALLTYPE get_Count (
        /* [retval][out] */ long *plCount);
    /* [propget] */ HRESULT STDMETHODCALLTYPE get_Item ( 
        /* [in] */ long lIndex,
        /* [retval][out] */ BSTR *ppEntry);
    /* [propput] */ HRESULT STDMETHODCALLTYPE put_Item ( 
        /* [in] */ long lIndex,
        /* [in] */ BSTR pEntry);
    HRESULT STDMETHODCALLTYPE Add ( 
        /* [in] */ BSTR pEntry);
    HRESULT STDMETHODCALLTYPE Clear ();
    HRESULT STDMETHODCALLTYPE Remove ( 
        /* [in] */ long lIndex);
};