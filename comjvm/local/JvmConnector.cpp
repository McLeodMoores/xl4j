/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "JvmConnector.h"
#include "Jvm.h"
#include "Classpath.h"
#include "Internal.h"
#include "jni/jni.h"
#include "comjvm/core.h"

CInstanceHolder<CJvmConnectorFactory> CJvmConnectorFactory::s_oInstance;

/// <summary>Creates a new instance.</summary>
///
/// <para>This should not be called directly but as a result of calling Instance.</para>
CJvmConnectorFactory::CJvmConnectorFactory ()
: m_lRefCount (1) {
	IncrementActiveObjectCount ();
}

/// <summary>Destroys an instance.</summary>
///
/// <para>This should not be called directly but as a result of calling IUnknown#Release on the instance.</para>
CJvmConnectorFactory::~CJvmConnectorFactory () {
	assert (m_lRefCount == 0);
	DecrementActiveObjectCount ();
}

/// <summary>Obtains the existing, or creates a new, instance.</summary>
///
/// <returns>The object instance.</returns>
CJvmConnectorFactory *CJvmConnectorFactory::Instance () {
	return s_oInstance.Instance ();
}

HRESULT STDMETHODCALLTYPE CJvmConnectorFactory::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*>(this);
	} else if (riid == IID_IClassFactory) {
		*ppvObject = static_cast<IClassFactory*>(this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef ();
	return S_OK;
}

ULONG STDMETHODCALLTYPE CJvmConnectorFactory::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CJvmConnectorFactory::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}

HRESULT STDMETHODCALLTYPE CJvmConnectorFactory::CreateInstance (
	/* [in] */ IUnknown *pUnkOuter,
	/* [in] */ REFIID riid,
	/* [out] */ void **ppvObject
	) {
	if (pUnkOuter) return CLASS_E_NOAGGREGATION;
	if (!ppvObject) return E_POINTER;
	*ppvObject = NULL;
	CJvmConnector *pInstance = NULL;
	HRESULT hr;
	try {
		pInstance = new CJvmConnector ();
		hr = pInstance->QueryInterface (riid, ppvObject);
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	}
	if (pInstance) pInstance->Release ();
	return hr;
}

HRESULT STDMETHODCALLTYPE CJvmConnectorFactory::LockServer (
	/* [in] */ BOOL fLock
	) {
	return fLock ? s_oInstance.Lock (this) : s_oInstance.Unlock ();
}

#define LAST_USE_TIMEOUT 5000

/// <summary>Underlying implementation of CJvmConnector.</summary>
///
/// <para>Multiple CJvmConnector instances may be created, but they can
/// only ever work with a single, global, instance of this because of
/// limitations of the underlying JNI.</para>
class CJvmConnectorImpl {
private:
	CRITICAL_SECTION m_cs;
	HANDLE m_hSemaphore;
	BOOL m_bLocked;
	DWORD m_dwLastUse;
	DWORD m_dwJvm;
	GUID m_guid;
	std::list<const _std_string_t> m_astrClasspath;

	HRESULT CreateJvmWrapper (IJvm **ppJvm) {
		HRESULT hr;
		IJvmTemplate *pTemplate = NULL;
		IClasspathEntries *pClasspath = NULL;
		IClasspathEntry *pEntry = NULL;
		try {
			if (FAILED (hr = ComJvmCreateTemplate (NULL, &pTemplate))) _com_raise_error (hr);
			if (FAILED (hr = pTemplate->get_Classpath (&pClasspath))) _com_raise_error (hr);
			for (std::list<const _std_string_t>::iterator itr = m_astrClasspath.begin (), end = m_astrClasspath.end (); itr != end; itr++) {
				if (FAILED (hr = ComJvmCreateClasspathEntry (itr->data (), &pEntry))) _com_raise_error (hr);
				if (FAILED (hr = pClasspath->Add (pEntry))) _com_raise_error (hr);
				pEntry->Release ();
				pEntry = NULL;
			}
			*ppJvm = new CJvm (pTemplate, &m_guid, m_dwJvm);
		} catch (std::bad_alloc) {
			hr = E_OUTOFMEMORY;
		} catch (_com_error &e) {
			hr = e.Error ();
		}
		if (pTemplate) pTemplate->Release ();
		if (pClasspath) pClasspath->Release ();
		if (pEntry) pEntry->Release ();
		return hr;
	}

	HRESULT FindJvmImpl (long lIndex, BSTR bstrLogicalIdentifier, IJvm **ppJvm) {
		if (!m_bLocked) return E_NOT_VALID_STATE;
		if (lIndex < 1) return E_INVALIDARG;
		if (!ppJvm) return E_POINTER;
		if (m_dwJvm) {
			if (lIndex == 1) {
				return CreateJvmWrapper (ppJvm);
			} else {
				return E_INVALIDARG;
			}
		} else {
			return E_INVALIDARG;
		}
	}

	HRESULT CreateJvmImpl (IJvmTemplate *pTemplate, BSTR bstrLogicalIdentifier, IJvm **ppJvm) {
		if (!m_bLocked) return E_NOT_VALID_STATE;
		if (!pTemplate) return E_POINTER;
		if (!ppJvm) return E_POINTER;
		if (!m_dwJvm) {
			HRESULT hr;
			CClasspath *pClasspath = nullptr;
			PTSTR pszClasspath = nullptr;
			IClasspathEntries *pEntries = nullptr;
			IClasspathEntry *pEntry = nullptr;
			IJvmOptionEntries *pOptionEntries = nullptr;
			
			try {
				pClasspath = new CClasspath (bstrLogicalIdentifier ? (PCTSTR)_bstr_t (bstrLogicalIdentifier) : TEXT (""));
				if (FAILED (hr = pTemplate->get_Classpath (&pEntries))) _com_raise_error (hr);
				long lCount;
				if (FAILED (hr = pEntries->get_Count (&lCount))) _com_raise_error (hr);
				long lIndex;
				for (lIndex = 1; lIndex <= lCount; lIndex++) {
					if (FAILED (hr = pEntries->get_Item (lIndex, &pEntry))) _com_raise_error (hr);
					if (FAILED (hr = pEntry->AddToClasspath (pClasspath))) _com_raise_error (hr);
					pEntry->Release ();
					pEntry = nullptr;
				}
				m_astrClasspath = pClasspath->GetPathComponents ();
				pszClasspath = pClasspath->GetPath ();
				// Options
				if (FAILED (hr = pTemplate->get_Options (&pOptionEntries))) _com_raise_error (hr);
				long cOptions;
				if (FAILED (hr = pOptionEntries->get_Count (&cOptions))) _com_raise_error (hr);
				JAVA_VM_PARAMETERS params;
				ZeroMemory (&params, sizeof (params));
				params.cbSize = sizeof (params);
				params.pszClasspath = pszClasspath;
				params.cOptions = cOptions;
				params.ppszOptions = new PCTSTR[cOptions];
				for (long i = 0; i < cOptions; i++) {
					BSTR pOptionEntry = nullptr;
					if (FAILED (hr = pOptionEntries->get_Item (i + 1, &pOptionEntry))) _com_raise_error (hr);
					params.ppszOptions[i] = pOptionEntry;
				}
				hr = JNICreateJavaVM (&m_dwJvm, &params);
			} catch (std::bad_alloc) {
				hr = E_OUTOFMEMORY;
			} catch (_com_error &e) {
				hr = e.Error ();
			}
			if (pClasspath) pClasspath->Release ();
			if (pszClasspath) delete pszClasspath;
			if (pEntries) pEntries->Release ();
			if (pEntry) pEntry->Release ();
			if (pOptionEntries) pOptionEntries->Release ();
			if (FAILED (hr)) return hr;
		}
		return CreateJvmWrapper (ppJvm);
	}

public:

	/// <summary>Creates a new instance.</summary>
	CJvmConnectorImpl () {
		InitializeCriticalSection (&m_cs);
		m_hSemaphore = NULL;
		m_bLocked = FALSE;
		m_dwLastUse = GetTickCount ();
		CoCreateGuid (&m_guid);

	}
	/// <summary>Destroys an instance.</summary>
	~CJvmConnectorImpl () {
		DeleteCriticalSection (&m_cs);
		if (m_hSemaphore) CloseHandle (m_hSemaphore);
		if (m_dwJvm) JNIDestroyJavaVM (m_dwJvm);
	}

	/// <summary>Claims the lock.</summary>
	///
	/// <para>If the lock is already claimed, this will wait for either the lock
	/// to be released or five seconds to elapse since the object was last used.</para>
	///
	/// <returns>S_OK if successful, an error code otherwise</returns>
	HRESULT Lock () {
		EnterCriticalSection (&m_cs);
		while (m_bLocked) {
			DWORD dwMaxWait = (m_dwLastUse + LAST_USE_TIMEOUT) - GetTickCount ();
			if (dwMaxWait >= 0x80000000) {
				// Not been used recently, so timeout the previous lock
				break;
			}
			// Wait for the previous use to complete (or the timeout to elapse)
			if (!m_hSemaphore) {
				m_hSemaphore = CreateSemaphore (NULL, 0, 1, NULL);
			}
			HANDLE hSemaphore = m_hSemaphore;
			LeaveCriticalSection (&m_cs);
			if (!hSemaphore) return HRESULT_FROM_WIN32 (GetLastError ());
			WaitForSingleObject (hSemaphore, dwMaxWait);
			EnterCriticalSection (&m_cs);
		}
		m_bLocked = TRUE;
		m_dwLastUse = GetTickCount ();
		LeaveCriticalSection (&m_cs);
		return S_OK;
	}

	HRESULT FindJvm (long lIndex, BSTR bstrLogicalIdentifier, IJvm **ppJvm) {
		EnterCriticalSection (&m_cs);
		HRESULT hr = FindJvmImpl (lIndex, bstrLogicalIdentifier, ppJvm);
		m_dwLastUse = GetTickCount ();
		LeaveCriticalSection (&m_cs);
		return hr;
	}

	HRESULT CreateJvm (IJvmTemplate *pTemplate, BSTR bstrLogicalIdentifier, IJvm **ppJvm) {
		EnterCriticalSection (&m_cs);
		HRESULT hr = CreateJvmImpl (pTemplate, bstrLogicalIdentifier, ppJvm);
		m_dwLastUse = GetTickCount ();
		LeaveCriticalSection (&m_cs);
		return hr;
	}

	/// <summary>Releases the lock.</summary>
	///
	/// <returns>S_OK if successful, an error code otherwise</returns>
	HRESULT Unlock () {
		HRESULT hr;
		EnterCriticalSection (&m_cs);
		if (m_bLocked) {
			m_bLocked = FALSE;
			if (m_hSemaphore) {
				ReleaseSemaphore (m_hSemaphore, 1, NULL);
			}
			hr = S_OK;
		} else {
			hr = E_NOT_VALID_STATE;
		}
		LeaveCriticalSection (&m_cs);
		return hr;
	}

};

static CJvmConnectorImpl g_oSingleton;

/// <summary>Creates a new instance.</summary>
CJvmConnector::CJvmConnector ()
: m_lRefCount (1) {
	IncrementActiveObjectCount ();
}

/// <summary>Destroys an instance.</summary>
///
/// <para>This should not be called directly but as a result of using IUnknown#Release.</para>
CJvmConnector::~CJvmConnector () {
	assert (m_lRefCount == 0);
	DecrementActiveObjectCount ();
}

HRESULT STDMETHODCALLTYPE CJvmConnector::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*> (this);
	} else if (riid == IID_IJvmConnector)  {
		*ppvObject = static_cast<IJvmConnector*> (this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef ();
	return S_OK;
}

ULONG STDMETHODCALLTYPE CJvmConnector::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CJvmConnector::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}

HRESULT STDMETHODCALLTYPE CJvmConnector::Lock () {
	return g_oSingleton.Lock ();
}

HRESULT STDMETHODCALLTYPE CJvmConnector::FindJvm ( 
    /* [in] */ long lIndex,
    /* [optional][in] */ BSTR bstrLogicalIdentifier,
    /* [retval][out] */ IJvm **ppJvm
	) {
	return g_oSingleton.FindJvm (lIndex, bstrLogicalIdentifier, ppJvm);
}

HRESULT STDMETHODCALLTYPE CJvmConnector::CreateJvm ( 
    /* [in] */ IJvmTemplate *pTemplate,
    /* [optional][in] */ BSTR bstrLogicalIdentifier,
    /* [retval][out] */ IJvm **ppJvm
	) {
	return g_oSingleton.CreateJvm (pTemplate, bstrLogicalIdentifier, ppJvm);
}

HRESULT STDMETHODCALLTYPE CJvmConnector::Unlock () {
	return g_oSingleton.Unlock ();
}
