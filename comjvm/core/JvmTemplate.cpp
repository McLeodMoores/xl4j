/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "JvmTemplate.h"
#include "ClasspathEntries.h"
#include "core.h"
#include "internal.h"

/// <summary>Creates a new instance.</summary>
CJvmTemplate::CJvmTemplate ()
: m_lRefCount (1), m_pClasspath (NULL), m_bstrType (), m_pDefaults (NULL) {
	IncrementActiveObjectCount ();
}

/// <summary>Destroys an instance.</summary>
///
/// <para>This should not be called directly but as a result of calling IUnknown#Release on the instance.</para>
CJvmTemplate::~CJvmTemplate () {
	assert (m_lRefCount == 0);
	if (m_pClasspath) m_pClasspath->Release ();
	if (m_pDefaults) m_pDefaults->Release ();
	DecrementActiveObjectCount ();
}

/// <summary>Loads any settings specified as the "base" in the given configuration.</summary>
///
/// <para>A configuration may include a reference to another, base, configuration set which is
/// loaded prior to the main configuration.</para>
///
/// <param name="oSettings">Configuration data being loaded</param>
/// <returns>S_OK if data was loaded, S_FALSE if there is no base configuration, or an error code otherwise</returns>
HRESULT CJvmTemplate::LoadBaseSettings (const CSettings &oSettings) {
	try {
		_bstr_t bstrBase = oSettings.GetString (JVM_TEMPLATE_CONFIG, JVM_TEMPLATE_CONFIG_BASE);
		if (!bstrBase) return S_FALSE;
		CSettings oBaseSettings (SETTINGS_JVM_TEMPLATE, (PCTSTR)bstrBase);
		return Load (oBaseSettings);
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}

/// <summary>Loads any settings specified as the "over-ride" in the given configuration.</summary>
///
/// <para>A configuration may include a reference to another, over-ride, configuration set which
/// is applied to the template, using AppendDefaults, prior to it being used to connect to a JVM.</para>
///
/// <param name="oSettings">Configuration data being loaded</param>
/// <returns>S_OK if data was loaded, S_FALSE if there is no over-ride configuration, or an error code otherwise</returns>
HRESULT CJvmTemplate::LoadOverrideSettings (const CSettings &oSettings) {
	CJvmTemplate *pOverride = NULL;
	HRESULT hr;
	try {
		_bstr_t bstrOverride = oSettings.GetString (JVM_TEMPLATE_CONFIG, JVM_TEMPLATE_CONFIG_OVERRIDE);
		if (!bstrOverride) return S_FALSE;
		CSettings oOverrideSettings (SETTINGS_JVM_TEMPLATE, (PCTSTR)bstrOverride);
		pOverride = new CJvmTemplate ();
		hr = pOverride->Load (oOverrideSettings);
		if (FAILED (hr)) _com_raise_error (hr);
		m_pDefaults = pOverride;
		return S_OK;
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	}
	if (pOverride) pOverride->Release ();
	return hr;
}

/// <summary>Loads any classpath details from the given configuration.</summary>
///
/// <para>A configuration may include a reference to another, base, configuration set which is
/// loaded prior to the main configuration.</para>
///
/// <param name="oSettings">Configuration data being loaded</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT CJvmTemplate::LoadClasspath (const CSettings &oSettings) {
	try {
		long l = 0;
		_std_string_t strClasspath (JVM_TEMPLATE_CLASSPATH);
		_bstr_t bstr;
		while (!(!(bstr = oSettings.GetString (strClasspath, l++)))) {
			if (!m_pClasspath) m_pClasspath = new CClasspathEntries ();
			IClasspathEntry *pEntry;
			HRESULT hr = ComJvmCreateClasspathEntry (bstr, &pEntry);
			if (FAILED (hr)) return hr;
			hr = m_pClasspath->Add (pEntry);
			pEntry->Release ();
			if (FAILED (hr)) return hr;
		}
		return S_OK;
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	} catch (_com_error &e) {
		return e.Error ();
	}
}

/// <summary>Loads the JVM connection type from the given configuration.</summary>
///
/// <param name="oSettings">Configuration data being loaded</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT CJvmTemplate::LoadType (const CSettings &oSettings) {
	try {
		m_bstrType = oSettings.GetString (JVM_TEMPLATE_JVM, JVM_TEMPLATE_JVM_TYPE);
		return S_OK;
	} catch (_com_error &e) {
		return e.Error ();
	}
}

/// <summary>Loads all settings from the given configuration.</summary>
///
/// <param name="oSettings">Configuration data being loaded</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT CJvmTemplate::Load (const CSettings &oSettings) {
	if (!oSettings.IsValid ()) return HRESULT_FROM_WIN32 (ERROR_NOT_FOUND);
	HRESULT hr;
	if (FAILED (hr = LoadBaseSettings (oSettings))) return hr;
	if (FAILED (hr = LoadOverrideSettings (oSettings))) return hr;
	if (FAILED (hr = LoadClasspath (oSettings))) return hr;
	if (FAILED (hr = LoadType (oSettings))) return hr;
	return S_OK;
}

static HRESULT AppendClasspath (IJvmTemplate *pSource, IJvmTemplate *pDest) {
	IClasspathEntries *pClasspathSource;
	IClasspathEntries *pClasspathDest;
	HRESULT hr;
	if (SUCCEEDED (hr = pSource->get_Classpath (&pClasspathSource))) {
		if (SUCCEEDED (hr = pDest->get_Classpath (&pClasspathDest))) {
			hr = CClasspathEntries::Append (pClasspathSource, pClasspathDest);
			pClasspathDest->Release ();
		}
		pClasspathSource->Release ();
	}
	return hr;
}

static HRESULT AppendType (IJvmTemplate *pSource, IJvmTemplate *pDest) {
	BSTR bstrType;
	HRESULT hr;
	if (SUCCEEDED (hr = pSource->get_Type (&bstrType)) && bstrType) {
		hr = pDest->put_Type (bstrType);
		SysFreeString (bstrType);
	}
	return hr;
}

/// <summary>Copies values from one template to another.</summary>
///
/// <para>The content of the source template is added to the destination. This
/// does not include any referenced over-ride configuration.</para>
///
/// <param name="pSource">Source template to copy data from</param>
/// <param name="pDest">Destination template to copy data to</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT CJvmTemplate::Append (IJvmTemplate *pSource, IJvmTemplate *pDest) {
	if (!pSource) return E_POINTER;
	if (!pDest) return E_POINTER;
	HRESULT hr;
	if (FAILED (hr = AppendClasspath (pSource, pDest))) return hr;
	if (FAILED (hr = AppendType (pSource, pDest))) return hr;
	return S_OK;
}

/// <summary>Tests if the one template's classpath is compatible with another's.</summary>
///
/// <para>All of the classpath entries from the right template must be present on the
/// left's classpath.</para>
///
/// <param name="pLeft">Existing template to test</param>
/// <param name="pRight">New template to check whether the existing one can support it</param>
/// <returns>S_OK if the classpaths are compatible, S_FALSE if incompatible, an error code otherwise</returns>
static HRESULT IsCompatibleClasspath (IJvmTemplate *pLeft, IJvmTemplate *pRight) {
	IClasspathEntries *pClasspathLeft;
	IClasspathEntries *pClasspathRight;
	HRESULT hr;
	if (SUCCEEDED (hr = pLeft->get_Classpath (&pClasspathLeft))) {
		if (SUCCEEDED (hr = pRight->get_Classpath (&pClasspathRight))) {
			hr = CClasspathEntries::IsCompatible (pClasspathLeft, pClasspathRight);
			pClasspathRight->Release ();
		}
		pClasspathLeft->Release ();
	}
	return hr;
}

/// <summary>Tests if one template is compatible with another.</summary>
///
/// <para>An existing template is compatible with another if it contains sufficient
/// configuration that would meet any requirements dictated by the other. For
/// example, the existing classpath must contain all elements of the other classpath.</para>
///
/// <param name="pLeft">Existing template to test</param>
/// <param name="pRight">New template to check whether the existing one can support it</param>
/// <returns>S_OK if the templates are compatible, S_FALSE if incompatible, an error code otherwise</returns>
HRESULT CJvmTemplate::IsCompatible (IJvmTemplate *pLeft, IJvmTemplate *pRight) {
	if (!pLeft) return E_POINTER;
	if (!pRight) return E_POINTER;
	return IsCompatibleClasspath (pLeft, pRight);
}

HRESULT STDMETHODCALLTYPE CJvmTemplate::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*>(this);
	} else if (riid == IID_IJvmTemplate) {
		*ppvObject = static_cast<IJvmTemplate*>(this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef ();
	return S_OK;
}

ULONG STDMETHODCALLTYPE CJvmTemplate::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CJvmTemplate::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CJvmTemplate::get_Classpath ( 
    /* [retval][out] */ IClasspathEntries **ppClasspath
	) {
	if (!ppClasspath) return E_POINTER;
	try {
		if (!m_pClasspath) {
			m_pClasspath = new CClasspathEntries ();
		}
		m_pClasspath->AddRef ();
		*ppClasspath = m_pClasspath;
		return S_OK;
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CJvmTemplate::get_Type (
	/* [retval][out] */ BSTR *pbstrType
	) {
	if (!pbstrType) return E_POINTER;
	try {
		*pbstrType = m_bstrType.copy ();
		return S_OK;
	} catch (_com_error &e) {
		return e.Error ();
	}
}

/* [propput] */ HRESULT STDMETHODCALLTYPE CJvmTemplate::put_Type (
	/* [in] */ BSTR bstrType
	) {
	try {
		m_bstrType = bstrType;
		return S_OK;
	} catch (_com_error &e) {
		return e.Error ();
	}
}

HRESULT STDMETHODCALLTYPE CJvmTemplate::AppendDefaults () {
	if (!m_pDefaults) return S_FALSE;
	return Append (m_pDefaults, this);
}
