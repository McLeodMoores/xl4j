/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

#include "core_h.h"

///	<summary>Implementation of an IClasspathEntry collection.</summary>
///
/// <para>This is the underlying implementation of CClasspathEntries.</para>
///
/// <para>This implementation is not safe for use by multiple threads, except as a read-only store.</para>
class CClasspathEntriesImpl {
private:
	std::vector<IClasspathEntry *> m_vpBuffer;
public:
	CClasspathEntriesImpl ();
	~CClasspathEntriesImpl ();
	long get_Count () const;
	HRESULT get_Item (long lIndex, IClasspathEntry **ppEntry) const;
	HRESULT put_Item (long lIndex, IClasspathEntry *pEntry);
	HRESULT Add (IClasspathEntry *pEntry);
	void Clear ();
	HRESULT Remove (long lIndex);
};

/// <summary>Implementation of IClasspathEntries.</summary>
///
/// <para>This implementation is backed by a CClasspathEntriesImpl instance that is protected by a critical section.</para>
class CClasspathEntries : public IClasspathEntries {
private:
	CRITICAL_SECTION m_cs;
	volatile ULONG m_lRefCount;
	CClasspathEntriesImpl m_oImpl;
	~CClasspathEntries ();
public:
	CClasspathEntries ();
	static HRESULT Append (IClasspathEntries *pSource, IClasspathEntries *pDest);
	static HRESULT IsCompatible (IClasspathEntries *pLeft, IClasspathEntries *pRight);
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
        /* [retval][out] */ IClasspathEntry **ppEntry);
    /* [propput] */ HRESULT STDMETHODCALLTYPE put_Item ( 
        /* [in] */ long lIndex,
        /* [in] */ IClasspathEntry *pEntry);
    HRESULT STDMETHODCALLTYPE Add ( 
        /* [in] */ IClasspathEntry *pEntry);
    HRESULT STDMETHODCALLTYPE Clear ();
    HRESULT STDMETHODCALLTYPE Remove ( 
        /* [in] */ long lIndex);
};