/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "ClasspathEntries.h"
#include "internal.h"

///	<summary>Creates a new instance.</summary>
///
///	<para>The collection is initially empty.</para>
CClasspathEntriesImpl::CClasspathEntriesImpl () {
	IncrementActiveObjectCount ();
}

/// <summary>Destroys an instance.</summary>
CClasspathEntriesImpl::~CClasspathEntriesImpl () {
	size_t cSize = m_vpBuffer.size (), c;
	for (c = 0; c < cSize; c++) {
		m_vpBuffer[c]->Release ();
	}
	DecrementActiveObjectCount ();
}

/// <summary>Queries the number of entries in the collection.</summary>
///
/// <returns>The number of entries</returns>
long CClasspathEntriesImpl::get_Count () const {
	return (long)m_vpBuffer.size ();
}

/// <summary>Queries an element from the collection.</summary>
///
/// <param name="lIndex">The 1-based index to query</param>
/// <param name="ppEntry">Receives the element at the index in the collection</param>
/// <returns>S_OK if the element was returned, an error code otherwise</returns>
HRESULT CClasspathEntriesImpl::get_Item (long lIndex, IClasspathEntry **ppEntry) const {
	if (!ppEntry) return E_POINTER;
	if ((lIndex <= 0) || ((size_t)lIndex > m_vpBuffer.size ())) return E_INVALIDARG;
	IClasspathEntry *pEntry = m_vpBuffer[lIndex - 1];
	pEntry->AddRef ();
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
HRESULT CClasspathEntriesImpl::put_Item (long lIndex, IClasspathEntry *pEntry) {
	if (!pEntry) return E_POINTER;
	if ((lIndex <= 0) || ((size_t)lIndex > m_vpBuffer.size () + 1)) return E_INVALIDARG;
	if (lIndex == m_vpBuffer.size () + 1) {
		return Add (pEntry);
	} else {
		pEntry->AddRef ();
		m_vpBuffer[lIndex - 1]->Release ();
		m_vpBuffer[lIndex - 1] = pEntry;
		return S_OK;
	}
}

/// <summary>Appends an element to the collection.</summary>
///
/// <param name="pEntry">Element to add</param>
/// <returns>S_OK if the element was appended, an error code otherwise</returns>
HRESULT CClasspathEntriesImpl::Add (IClasspathEntry *pEntry) {
	if (!pEntry) return E_POINTER;
	pEntry->AddRef ();
	try {
		m_vpBuffer.push_back (pEntry);
		return S_OK;
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}

/// <summary>Removes all elements from the collection.</summary>
void CClasspathEntriesImpl::Clear () {
	size_t cSize = m_vpBuffer.size (), c;
	for (c = 0; c < cSize; c++) {
		m_vpBuffer[c]->Release ();
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
HRESULT CClasspathEntriesImpl::Remove (long lIndex) {
	if ((lIndex <= 0) || ((size_t)lIndex > m_vpBuffer.size ())) return E_INVALIDARG;
	m_vpBuffer[lIndex - 1]->Release ();
	if (m_vpBuffer.size () > (size_t)lIndex) {
		MoveMemory (m_vpBuffer.data () + (lIndex - 1), m_vpBuffer.data () + lIndex, (m_vpBuffer.size () - lIndex) * sizeof (IClasspathEntry*));
	}
	m_vpBuffer.pop_back ();
	return S_OK;
}

/// <summary>Creates a new instance.</summary>
CClasspathEntries::CClasspathEntries ()
: m_lRefCount (1) {
	InitializeCriticalSection (&m_cs);
}

/// <summary>Destroys an instance.</summary>
///
/// <para>This should not be called directly but as a result of calling IUnknown#Release on the instance.</para>
CClasspathEntries::~CClasspathEntries () {
	assert (m_lRefCount == 0);
	DeleteCriticalSection (&m_cs);
}

/// <summary>Applies the content of one IClasspathEntries instance to another.</summary>
///
/// <para>All of the components from the source instance are added to the destination.</para>
///
/// <param name="pSource">Source instance to copy values from</param>
/// <param name="pDest">Destination instance to copy values to</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT CClasspathEntries::Append (IClasspathEntries *pSource, IClasspathEntries *pDest) {
	if (!pSource) return E_POINTER;
	if (!pDest) return E_POINTER;
	HRESULT hr;
	long lCount, l;
	hr = pSource->get_Count (&lCount);
	if (SUCCEEDED (hr)) {
		for (l = 1; l <= lCount; l++) {
			IClasspathEntry *pEntry;
			hr = pSource->get_Item (l, &pEntry);
			if (SUCCEEDED (hr)) {
				hr = pDest->Add (pEntry);
				if (FAILED (hr)) {
					l = lCount + 1;
				}
				pEntry->Release ();
			} else {
				l = lCount;
			}
		}
	}
	return hr;
}

/// <summary>Tests if one classpath is compatible with another.</summary>
///
/// <para>An existing classpath is compatible with another if it contains all of the
/// elements of the other classpath.</para>
///
/// <param name="pLeft">Existing classpath to test</param>
/// <param name="pRight">New classpath to check whether the existing one can support it</param>
/// <returns>S_OK if the classpaths are compatible, S_FALSE if incompatible, an error code otherwise</returns>
HRESULT CClasspathEntries::IsCompatible (IClasspathEntries *pLeft, IClasspathEntries *pRight) {
	HRESULT hr;
	try {
		long lCount, lIndex;
		if (FAILED (hr = pLeft->get_Count (&lCount))) return hr;
		std::hash_set<_std_string_t> oNames;
		for (lIndex = 1; lIndex <= lCount; lIndex++) {
			IClasspathEntry *pEntry;
			if (FAILED (hr = pLeft->get_Item (lIndex, &pEntry))) return hr;
			BSTR bstr;
			pEntry->get_Name (&bstr);
			pEntry->Release ();
			_bstr_t bstrName (bstr, FALSE);
			oNames.insert (std::hash_set<_std_string_t>::value_type ((PCTSTR)bstrName));
		}
		if (FAILED (hr = pRight->get_Count (&lCount))) return hr;
		for (lIndex = 1; lIndex <= lCount; lIndex++) {
			IClasspathEntry *pEntry;
			if (FAILED (hr = pRight->get_Item (lIndex, &pEntry))) return hr;
			BSTR bstr;
			pEntry->get_Name (&bstr);
			pEntry->Release ();
			_bstr_t bstrName (bstr, FALSE);
			if (oNames.find ((PCTSTR)bstrName) == oNames.end ()) return S_FALSE;
		}
		hr = S_OK;
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	} catch (_com_error &e) {
		hr = e.Error ();
	}
	return hr;
}

HRESULT STDMETHODCALLTYPE CClasspathEntries::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*>(this);
	} else if (riid == IID_IClasspathEntries) {
		*ppvObject = static_cast<IClasspathEntries*>(this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef ();
	return S_OK;
}

ULONG STDMETHODCALLTYPE CClasspathEntries::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CClasspathEntries::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CClasspathEntries::get_Count (  
    /* [retval][out] */ long *plCount
	) {
	if (!plCount) return E_POINTER;
	EnterCriticalSection (&m_cs);
	*plCount = m_oImpl.get_Count ();
	LeaveCriticalSection (&m_cs);
	return S_OK;
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CClasspathEntries::get_Item ( 
    /* [in] */ long lIndex,
    /* [retval][out] */ IClasspathEntry **ppEntry
	) {
	EnterCriticalSection (&m_cs);
	HRESULT hr = m_oImpl.get_Item (lIndex, ppEntry);
	LeaveCriticalSection (&m_cs);
	return hr;
}

/* [propput] */ HRESULT STDMETHODCALLTYPE CClasspathEntries::put_Item ( 
    /* [in] */ long lIndex,
    /* [in] */ IClasspathEntry *pEntry
	) {
	EnterCriticalSection (&m_cs);
	HRESULT hr = m_oImpl.put_Item (lIndex, pEntry);
	LeaveCriticalSection (&m_cs);
	return hr;
}

HRESULT STDMETHODCALLTYPE CClasspathEntries::Add ( 
    /* [in] */ IClasspathEntry *pEntry
	) {
	EnterCriticalSection (&m_cs);
	HRESULT hr = m_oImpl.Add (pEntry);
	LeaveCriticalSection (&m_cs);
	return hr;
}

HRESULT STDMETHODCALLTYPE CClasspathEntries::Clear () {
	EnterCriticalSection (&m_cs);
	m_oImpl.Clear ();
	LeaveCriticalSection (&m_cs);
	return S_OK;
}

HRESULT STDMETHODCALLTYPE CClasspathEntries::Remove ( 
    /* [in] */ long lIndex
	) {
	EnterCriticalSection (&m_cs);
	HRESULT hr = m_oImpl.Remove (lIndex);
	LeaveCriticalSection (&m_cs);
	return hr;
}