/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

/*
* JVM as a COM object
*
* Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
* Released under the GNU General Public License.
*/

#include "stdafx.h"
#include "JvmOptionEntries.h"
#include "internal.h"

///	<summary>Creates a new instance.</summary>
///
///	<para>The collection is initially empty.</para>
CJvmOptionEntriesImpl::CJvmOptionEntriesImpl () {
	IncrementActiveObjectCount ();
}

/// <summary>Destroys an instance.</summary>
CJvmOptionEntriesImpl::~CJvmOptionEntriesImpl () {
	size_t cSize = m_vpBuffer.size (), c;
	for (c = 0; c < cSize; c++) {
		m_vpBuffer[c].Detach ();// ->Release ();
	}
	DecrementActiveObjectCount ();
}

/// <summary>Queries the number of entries in the collection.</summary>
///
/// <returns>The number of entries</returns>
long CJvmOptionEntriesImpl::get_Count () const {
	return (long)m_vpBuffer.size ();
}

/// <summary>Queries an element from the collection.</summary>
///
/// <param name="lIndex">The 1-based index to query</param>
/// <param name="ppEntry">Receives the element at the index in the collection</param>
/// <returns>S_OK if the element was returned, an error code otherwise</returns>
HRESULT CJvmOptionEntriesImpl::get_Item (long lIndex, BSTR *ppEntry) const {
	if (!ppEntry) return E_POINTER;
	if ((lIndex <= 0) || ((size_t)lIndex > m_vpBuffer.size ())) return E_INVALIDARG;
	BSTR pEntry = m_vpBuffer[lIndex - 1].copy ();
	*ppEntry = pEntry;
	return S_OK;
}

/// <summary>Updates an element in the collection.</summary>
///
/// <para>If the element index is at the end of the collection then the new entry is
/// appended to the collection.</para>
///
/// <param name="lIndex">The 1-based index to update</param>
/// <param name="pEntry">Element to store</param>
/// <returns>S_OK if the element was stored, an error code otherwise</returns>
HRESULT CJvmOptionEntriesImpl::put_Item (long lIndex, BSTR pEntry) {
	if (!pEntry) return E_POINTER;
	if ((lIndex <= 0) || ((size_t)lIndex > m_vpBuffer.size () + 1)) return E_INVALIDARG;
	if (lIndex == m_vpBuffer.size () + 1) {
		return Add (pEntry);
	} else {
		_bstr_t bstr (pEntry, FALSE);
		m_vpBuffer[lIndex - 1].Detach ();
		m_vpBuffer[lIndex - 1] = bstr;
		return S_OK;
	}
}

/// <summary>Appends an element to the collection.</summary>
///
/// <param name="pEntry">Element to add</param>
/// <returns>S_OK if the element was appended, an error code otherwise</returns>
HRESULT CJvmOptionEntriesImpl::Add (BSTR pEntry) {
	if (!pEntry) return E_POINTER;
	_bstr_t bstr (pEntry);
	try {
		m_vpBuffer.push_back (bstr);
		return S_OK;
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}

/// <summary>Removes all elements from the collection.</summary>
void CJvmOptionEntriesImpl::Clear () {
	size_t cSize = m_vpBuffer.size (), c;
	for (c = 0; c < cSize; c++) {
		m_vpBuffer[c].Detach ();
	}
	m_vpBuffer.clear ();
}

/// <summary>Removes an element from the collection.</summary>
///
/// <para>Any elements with an index greater than the one removed are shifted
/// left.</para>
///
/// <param name="lIndex">The 1-based index of the element to remove</param>
/// <returns>S_OK if the element was removed, an error code otherwise</returns>
HRESULT CJvmOptionEntriesImpl::Remove (long lIndex) {
	if ((lIndex <= 0) || ((size_t)lIndex > m_vpBuffer.size ())) return E_INVALIDARG;
	m_vpBuffer[lIndex - 1].Detach ();
	if (m_vpBuffer.size () > (size_t)lIndex) {
		MoveMemory (m_vpBuffer.data () + (lIndex - 1), m_vpBuffer.data () + lIndex, (m_vpBuffer.size () - lIndex) * sizeof (_bstr_t));
	}
	m_vpBuffer.pop_back ();
	return S_OK;
}

/// <summary>Creates a new instance.</summary>
CJvmOptionEntries::CJvmOptionEntries ()
	: m_lRefCount (1) {
	InitializeCriticalSection (&m_cs);
}

/// <summary>Destroys an instance.</summary>
///
/// <para>This should not be called directly but as a result of calling IUnknown#Release on the instance.</para>
CJvmOptionEntries::~CJvmOptionEntries () {
	assert (m_lRefCount == 0);
	DeleteCriticalSection (&m_cs);
}

/// <summary>Applies the content of one IJvmOptionEntries instance to another.</summary>
///
/// <para>All of the components from the source instance are added to the destination.</para>
///
/// <param name="pSource">Source instance to copy values from</param>
/// <param name="pDest">Destination instance to copy values to</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT CJvmOptionEntries::Append (IJvmOptionEntries *pSource, IJvmOptionEntries *pDest) {
	if (!pSource) return E_POINTER;
	if (!pDest) return E_POINTER;
	HRESULT hr;
	long lCount, l;
	hr = pSource->get_Count (&lCount);
	if (SUCCEEDED (hr)) {
		for (l = 1; l <= lCount; l++) {
			BSTR pEntry;
			hr = pSource->get_Item (l, &pEntry);
			if (SUCCEEDED (hr)) {
				hr = pDest->Add (pEntry);
				if (FAILED (hr)) {
					l = lCount + 1;
				}
				SysFreeString(pEntry);
			} else {
				l = lCount;
			}
		}
	}
	return hr;
}

HRESULT STDMETHODCALLTYPE CJvmOptionEntries::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*>(this);
	} else if (riid == IID_IJvmOptionEntries) {
		*ppvObject = static_cast<IJvmOptionEntries*>(this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef ();
	return S_OK;
}

ULONG STDMETHODCALLTYPE CJvmOptionEntries::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CJvmOptionEntries::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CJvmOptionEntries::get_Count (
	/* [retval][out] */ long *plCount
	) {
	if (!plCount) return E_POINTER;
	EnterCriticalSection (&m_cs);
	*plCount = m_oImpl.get_Count ();
	LeaveCriticalSection (&m_cs);
	return S_OK;
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CJvmOptionEntries::get_Item (
	/* [in] */ long lIndex,
	/* [retval][out] */ BSTR *ppEntry
	) {
	EnterCriticalSection (&m_cs);
	HRESULT hr = m_oImpl.get_Item (lIndex, ppEntry);
	LeaveCriticalSection (&m_cs);
	return hr;
}

/* [propput] */ HRESULT STDMETHODCALLTYPE CJvmOptionEntries::put_Item (
	/* [in] */ long lIndex,
	/* [in] */ BSTR pEntry
	) {
	EnterCriticalSection (&m_cs);
	HRESULT hr = m_oImpl.put_Item (lIndex, pEntry);
	LeaveCriticalSection (&m_cs);
	return hr;
}

HRESULT STDMETHODCALLTYPE CJvmOptionEntries::Add (
	/* [in] */ BSTR pEntry
	) {
	EnterCriticalSection (&m_cs);
	HRESULT hr = m_oImpl.Add (pEntry);
	LeaveCriticalSection (&m_cs);
	return hr;
}

HRESULT STDMETHODCALLTYPE CJvmOptionEntries::Clear () {
	EnterCriticalSection (&m_cs);
	m_oImpl.Clear ();
	LeaveCriticalSection (&m_cs);
	return S_OK;
}

HRESULT STDMETHODCALLTYPE CJvmOptionEntries::Remove (
	/* [in] */ long lIndex
	) {
	EnterCriticalSection (&m_cs);
	HRESULT hr = m_oImpl.Remove (lIndex);
	LeaveCriticalSection (&m_cs);
	return hr;
}