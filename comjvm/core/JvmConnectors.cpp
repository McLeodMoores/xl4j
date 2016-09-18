/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "JvmConnectors.h"
#include "internal.h"

static HRESULT CreateSystemObject (REFCLSID clsid, IJvmConnector **ppConnector) {
	// TODO
	//LOGTRACE ("CreateSystemObject not implemented");
	return E_NOTIMPL;
}

/// <summary>Holds the creating DLL open for the lifetime of the IJvmConnector instance.</summary>
class CJvmConnectorWrapper : public IJvmConnector {
private:
	volatile ULONG m_lRefCount;
	IJvmConnector *m_pInstance;
	HMODULE m_hModule;
	~CJvmConnectorWrapper () {
		m_pInstance->Release ();
		FreeLibrary (m_hModule);
		DecrementActiveObjectCount ();
	}
public:
	CJvmConnectorWrapper (IJvmConnector *pInstance, HMODULE hModule)
	: m_lRefCount (1), m_pInstance (pInstance), m_hModule (hModule) {
		IncrementActiveObjectCount ();
	}
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
		) {
		if (!ppvObject) return E_POINTER;
		if (riid == IID_IUnknown) {
			*ppvObject = static_cast<IUnknown*> (this);
		} else if (riid == IID_IJvmConnector) {
			*ppvObject = static_cast<IJvmConnector*> (this);
		} else {
			*ppvObject = NULL;
			return E_NOINTERFACE;
		}
		return S_OK;
	}
    ULONG STDMETHODCALLTYPE AddRef () {
		return InterlockedIncrement (&m_lRefCount);
	}
    ULONG STDMETHODCALLTYPE Release () {
		ULONG lResult = InterlockedDecrement (&m_lRefCount);
		if (!lResult) delete this;
		return lResult;
	}
	// IJvmConnector
    HRESULT STDMETHODCALLTYPE Lock () {
		return m_pInstance->Lock ();
	}
    HRESULT STDMETHODCALLTYPE FindJvm ( 
        /* [in] */ long lIndex,
        /* [optional][in] */ BSTR bstrLogicalIdentifier,
        /* [retval][out] */ IJvm **ppJvm
		) {
		return m_pInstance->FindJvm (lIndex, bstrLogicalIdentifier, ppJvm);
	}
    HRESULT STDMETHODCALLTYPE CreateJvm ( 
        /* [in] */ IJvmTemplate *pTemplate,
        /* [optional][in] */ BSTR bstrLogicalIdentifier,
        /* [retval][out] */ IJvm **ppJvm
		) {
		return m_pInstance->CreateJvm (pTemplate, bstrLogicalIdentifier, ppJvm);
	}
    HRESULT STDMETHODCALLTYPE Unlock () {
		return m_pInstance->Unlock ();
	}
};

typedef HRESULT (APIENTRY *DLL_GET_CLASS_OBJECT) (REFCLSID, REFIID, LPVOID *);

static HRESULT CreateLocalObject (const _bstr_t &bstrModule, REFCLSID clsid, IJvmConnector **ppConnector) {
	HMODULE hModule = LoadLibrary (bstrModule);
	if (hModule == NULL) return CLASS_E_CLASSNOTAVAILABLE;
	IClassFactory *pFactory = NULL;
	IJvmConnector *pConnector = NULL;
	HRESULT hr;
	try {
		DLL_GET_CLASS_OBJECT pfnGetClassObject = (DLL_GET_CLASS_OBJECT)GetProcAddress (hModule, "DllGetClassObject");
		if (!pfnGetClassObject) _com_raise_error (CLASS_E_CLASSNOTAVAILABLE);
		HRESULT hr = pfnGetClassObject (clsid, __uuidof (IClassFactory), (LPVOID*)&pFactory);
		if (FAILED (hr)) _com_raise_error (hr);
		hr = pFactory->CreateInstance (NULL, __uuidof (IJvmConnector), (LPVOID*)&pConnector);
		pFactory->Release ();
		pFactory = NULL;
		if (FAILED (hr)) _com_raise_error (hr);
		*ppConnector = new CJvmConnectorWrapper (pConnector, hModule);
		return S_OK;
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	} catch (_com_error &e) {
		hr = e.Error ();
	}
	if (hModule) FreeLibrary (hModule);
	if (pFactory) pFactory->Release ();
	if (pConnector) pConnector->Release ();
	return hr;
}

CJvmConnectorFactory::CJvmConnectorFactory (REFCLSID clsid, const _bstr_t &bstrModule)
: m_clsid (clsid), m_bstrModule (bstrModule) {
}

CJvmConnectorFactory::CJvmConnectorFactory (REFCLSID clsid)
: m_clsid (clsid), m_bstrModule ((PCTSTR)NULL) {
}

CJvmConnectorFactory::CJvmConnectorFactory ()
: m_clsid (CLSID_NULL), m_bstrModule ((PCTSTR)NULL) {
}

CJvmConnectorFactory::CJvmConnectorFactory (const CJvmConnectorFactory &ref)
: m_clsid (ref.m_clsid), m_bstrModule (ref.m_bstrModule) {
}

CJvmConnectorFactory::~CJvmConnectorFactory () {
}

HRESULT CJvmConnectorFactory::Create (IJvmConnector **ppConnector) const {
	if (m_clsid != CLSID_NULL) {
		if (!m_bstrModule) {
			return CreateSystemObject (m_clsid, ppConnector);
		} else {
			return CreateLocalObject (m_bstrModule, m_clsid, ppConnector);
		}
	} else {
		return E_INVALIDARG;
	}
}

/// <summary>Creates a new instance.</summary>
CJvmConnectors::CJvmConnectors () {
	InitializeCriticalSection (&m_cs);
}

/// <summary>Destroys an instance.</summary>
CJvmConnectors::~CJvmConnectors () {
	DeleteCriticalSection (&m_cs);
}

/// <summary>Extracts the connector type from a ProgId string.</summary>
///
/// <para>The ProgId will be of the form "&lt;Vendor&gt;Connect&lt;Type&gt;Jvm.&lt;Version&gt;".</para>
///
/// <param name="bstrProgId">ProgId string to process</param>
/// <returns>The type fragment</returns>
static const _bstr_t ConnectorType (const _bstr_t &bstrProgId) {
	_bstr_t bstrResult (bstrProgId);
	try {
		PCTSTR pszProgId = bstrProgId;
		while (*pszProgId) {
			if (!_tcsncmp (pszProgId, TEXT (".Connect"), 8)) {
				pszProgId += 8;
				size_t cch = 0;
				while (pszProgId[cch] && (pszProgId[cch] != '.')) {
					cch++;
				}
				if (cch > 3) {
					// Lose the Jvm suffix
					if (!_tcsncmp (pszProgId + (cch - 3), TEXT ("Jvm"), 3)) {
						cch -= 3;
					}
				}
				std::vector<TCHAR> psz (cch + 1);
				psz[0] = _totlower (pszProgId[0]);
				CopyMemory (psz.data () + 1, pszProgId + 1, (cch - 1) * sizeof (TCHAR));
				psz[cch] = 0;
				bstrResult = psz.data ();
				break;
			}
			pszProgId++;
		}
	} catch (...) {
		// Ignore
	}
	return bstrResult;
}

typedef HRESULT (APIENTRY *DllGetImplementingCLSID)(REFIID, DWORD, BSTR *, CLSID *);

/// <summary>Scans the folder containing this DLL for any that defined IJvmConnector instances.</summary>
///
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT CJvmConnectors::FindLocalDLLs () {
	HMODULE hModule;
	TCHAR szFilename[MAX_PATH + 1];
	size_t cchSlash;
	ZeroMemory (szFilename, sizeof (szFilename)); // please the code analyzer gods
	if (GetModuleHandleEx (GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS, (LPCTSTR)ConnectorType, &hModule)) {
		if (GetModuleFileName (hModule, szFilename, sizeof (szFilename) / sizeof (TCHAR)) <= MAX_PATH) {
			cchSlash = _tcslen (szFilename);
			while (cchSlash > 0) {
				cchSlash--;
				if (szFilename[cchSlash] == '\\') {
					StringCchCopy (szFilename + cchSlash + 1, (sizeof (szFilename) / sizeof (TCHAR)) - (cchSlash + 1), TEXT ("*.dll"));
					break;
				}
			}
		} else {
			StringCbCopy (szFilename, sizeof (szFilename), TEXT (".\\*.dll"));
			cchSlash = 1;
		}
		FreeLibrary (hModule);
	} else {
		StringCbCopy (szFilename, sizeof (szFilename), TEXT (".\\*.dll"));
		cchSlash = 1;
	}
	WIN32_FIND_DATA wfd;
	HANDLE hFind;
	hFind = FindFirstFile (szFilename, &wfd);
	if (hFind == INVALID_HANDLE_VALUE) return HRESULT_FROM_WIN32 (GetLastError ());
	do {
		StringCchCopy (szFilename + cchSlash + 1, (sizeof (szFilename) / sizeof (TCHAR)) - (cchSlash + 1), wfd.cFileName);
		hModule = LoadLibrary (szFilename);
		if (hModule != NULL) {
			DllGetImplementingCLSID pfn = (DllGetImplementingCLSID)GetProcAddress (hModule, "DllGetImplementingCLSID");
			if (pfn) {
				try {
					_bstr_t bstrModule (szFilename);
					DWORD dwIndex = 1;
					BSTR bstr;
					CLSID clsid;
					while (SUCCEEDED (pfn (__uuidof (IJvmConnector), dwIndex++, &bstr, &clsid))) {
						_bstr_t bstrProgId (bstr, FALSE);
						// TODO: Convert the ProgId to just the fragment we need
						m_cache.insert (CJvmConnectorFactoryCache::value_type (ConnectorType (bstrProgId), CJvmConnectorFactory (clsid, bstrModule)));
					}
				} catch (...) {
					// Ignore
				}
			}
			FreeLibrary (hModule);
		}
	} while (FindNextFile (hFind, &wfd));
	FindClose (hFind);
	return S_OK;
}

/// <summary>Locates an IJvmConnector implementation by name.</summary>
///
/// <para>Any DLLs adjacent to this module are scanned for any implementations they
/// contain. If the name is found there then the DLL is loaded and an instance created.</para>
///
/// <para>If the local DLLs do not define the data type then the registry is queried
/// for a COM object named "BeerDragon.Connect&lt;name&gt;.1". This gives a CLSID which is then
/// requested from COM.</para>
///
/// <param name="bstrName">Name of the connector</param>
/// <param name="ppConnector">Receives the connector instance</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT CJvmConnectors::Find (const _bstr_t &bstrName, IJvmConnector **ppConnector) {
	if (!bstrName) return E_INVALIDARG;
	if (!ppConnector) return E_POINTER;
	CJvmConnectorFactory oFactory;
	EnterCriticalSection (&m_cs);
	if (m_cache.size () == 0) {
		// First call; anything later will have at least one entry from the cached
		// failure or the valid construction.
		HRESULT hr = FindLocalDLLs ();
		if (FAILED (hr)) {
			LeaveCriticalSection (&m_cs);
			return hr;
		}
	}
	CJvmConnectorFactoryCache::iterator itr = m_cache.find (bstrName);
	if (itr == m_cache.end ()) {
		// Not in the cache; stick a dummy entry there for next time
		m_cache.insert (CJvmConnectorFactoryCache::value_type (bstrName, oFactory));
		// TODO: Form the ProgId and resolve it to a CLSID
	} else {
		// Found something in the cache
		oFactory = itr->second;
	}
	LeaveCriticalSection (&m_cs);
	return oFactory.Create (ppConnector);
}
