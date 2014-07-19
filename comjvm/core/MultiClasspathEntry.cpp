/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "MultiClasspathEntry.h"
#include "ClasspathEntries.h"
#include "internal.h"

/// <summary>Creates a new instance.</summary>
///
/// <param name="bstrName">Local, wild-card character containing, path to where the constituent items were found.</param>
CMultiClasspathEntry::CMultiClasspathEntry (const _bstr_t &bstrName)
: m_lRefCount (1), m_bstrName (bstrName) {
	IncrementActiveObjectCount ();
}

/// <summary>Destroys an instance.</summary>
///
/// <para>This should not be called directly but as a result of calling IUnknown#Release on the instance.</para>
CMultiClasspathEntry::~CMultiClasspathEntry() {
	assert (m_lRefCount == 0);
	DecrementActiveObjectCount ();
}

/// <summary>Adds a classpath entry to this composite.</summary>
///
/// <para>All of the entries that are part of this instance will be added in turn when AddToClasspath is called.</para>
///
/// <param name="pEntry">Entry to add</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT CMultiClasspathEntry::Add (IClasspathEntry *pEntry) {
	if (!pEntry) return E_POINTER;
	return m_oImpl.Add (pEntry);
}

HRESULT STDMETHODCALLTYPE CMultiClasspathEntry::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*>(this);
	} else if (riid == IID_IClasspathEntry) {
		*ppvObject = static_cast<IClasspathEntry*>(this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef ();
	return S_OK;
}

ULONG STDMETHODCALLTYPE CMultiClasspathEntry::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CMultiClasspathEntry::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CMultiClasspathEntry::get_Name ( 
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

/// <inheritdoc/>
/// <para>Every matched entry is added to the IClasspath in turn.</para>
HRESULT STDMETHODCALLTYPE CMultiClasspathEntry::AddToClasspath (
    /* [in] */ IClasspath *pClasspath
	) {
	if (!pClasspath) return E_POINTER;
	long l, lCount = m_oImpl.get_Count ();
	for (l = 1; l <= lCount; l++) {
		IClasspathEntry *pEntry;
		HRESULT hr = m_oImpl.get_Item (l, &pEntry);
		if (SUCCEEDED (hr)) {
			pEntry->AddToClasspath (pClasspath);
			pEntry->Release ();
		} else {
			return hr;
		}
	}
	return S_OK;
}
