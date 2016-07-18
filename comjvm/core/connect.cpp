/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

/// @file

#include "stdafx.h"
#include "core.h"
#include "internal.h"
#include "JvmTemplate.h"
#include "Settings.h"
#include "JvmConnectors.h"
#include "JvmContainer.h"

static CJvmConnectors s_oJvmConnectors;

HRESULT ComJvmCreateTemplateB (const _bstr_t &bstrIdentifier, IJvmTemplate **ppTemplate) {
	if (!ppTemplate) return E_POINTER;
	CJvmTemplate *pTemplate = NULL;
	HRESULT hr;
	try {
		pTemplate = new CJvmTemplate ();
		if (!(!bstrIdentifier)) {
			CSettings oSettings (SETTINGS_JVM_TEMPLATE, (PCTSTR)bstrIdentifier, CSettings::MOST_LOCAL);
			hr = pTemplate->Load (oSettings);
			if (FAILED (hr)) _com_raise_error (hr);
		}
		*ppTemplate = pTemplate;
		return S_OK;
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	} catch (_com_error &e) {
		hr = e.Error ();
	}
	if (pTemplate) pTemplate->Release ();
	return hr;
}

/// <summary>Creates a new IJvmTemplate instance.</summary>
///
/// <para>This is the ANSI form of IJvmSupport#CreateTemplate.</para>
///
/// <param name="pszIdentifier">Template identifier, or NULL for an empty template</param>
/// <param name="ppTemplate">Receives the new IJvmTemplate instance</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT COMJVM_CORE_API ComJvmCreateTemplateA (/* [in][optional] */ PCSTR pszIdentifier, /* [out][retval] */ IJvmTemplate **ppTemplate) {
	try {
		return ComJvmCreateTemplateB (pszIdentifier, ppTemplate);
	} catch (_com_error &e) {
		return e.Error ();
	}
}

/// <summary>Creates a new IJvmTemplate instance.</summary>
///
/// <para>This is the wide-character form of IJvmSupport#CreateTemplate.</para>
///
/// <param name="pszIdentifier">Template identifier, or NULL for an empty template</param>
/// <param name="ppTemplate">Receives the new IJvmTemplate instance</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT COMJVM_CORE_API ComJvmCreateTemplateW (/* [in][optional] */ PCWSTR pszIdentifier, /* [out][retval] */ IJvmTemplate **ppTemplate) {
	try {
		return ComJvmCreateTemplateB (pszIdentifier, ppTemplate);
	} catch (_com_error &e) {
		return e.Error ();
	}
}

/// <summary>Copies a IJvmTemplate instance.</summary>
///
/// <para>This is the implementation of IJvmSupport#CopyTemplate.</para>
///
/// <param name="pSource">Source template to copy details from</param>
/// <param name="ppDest">Receives the new template instance</param>
/// <returns>S_OK if the template was copied, an error code otherwise</returns>
HRESULT COMJVM_CORE_API ComJvmCopyTemplate (/* [in] */ IJvmTemplate *pSource, /* [out][retval] */ IJvmTemplate **ppDest) {
	if (!pSource) return E_POINTER;
	if (!ppDest) return E_POINTER;
	try {
		*ppDest = new CJvmTemplate ();
		return CJvmTemplate::Append (pSource, *ppDest);
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}

HRESULT ComJvmConnectB (/* [in][optional] */ const _bstr_t &bstrIdentifier, /* [in][optional] */ IJvmTemplate *pTemplate, /* [out][retval] */ IJvmContainer **ppJvmContainer) {
	if (!bstrIdentifier && !pTemplate) return E_INVALIDARG;
	if (!ppJvmContainer) return E_POINTER;
	HRESULT hr;
	IJvmTemplate *pBaseTemplate = NULL;
	IJvmConnector *pConnector = NULL;
	try {
		if (FAILED (ComJvmCreateTemplateB (bstrIdentifier, &pBaseTemplate))) {
			pBaseTemplate = new CJvmTemplate ();
		}
		if (pTemplate) {
			if (FAILED (hr = pTemplate->AppendDefaults ())
			 || FAILED (hr = CJvmTemplate::Append (pTemplate, pBaseTemplate))) {
				_com_raise_error (hr);
			}
		}
		if (FAILED (hr = pBaseTemplate->AppendDefaults ())) _com_raise_error (hr);
		BSTR bstr;
		if (FAILED (hr = pBaseTemplate->get_Type (&bstr))) _com_raise_error (hr);
		if (!bstr) _com_raise_error (E_INVALIDARG);
		_bstr_t bstrType (bstr, FALSE);
		if (FAILED (hr = s_oJvmConnectors.Find (bstrType, &pConnector))) _com_raise_error (hr);
		*ppJvmContainer = new CJvmContainer (bstrIdentifier, pBaseTemplate, pConnector);
		hr = S_OK;
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	} catch (_com_error &e) {
		hr = e.Error ();
	}
	if (pBaseTemplate) pBaseTemplate->Release ();
	if (pConnector) pConnector->Release ();
	return hr;
}

/// <summary>Establishes a connection to a JVM.</summary>
///
/// <para>This is the ANSI form of IJvmSupport#Connect.</para>
///
/// <param name="pszIdentifier">JVM identifier, omit if an anonymous JVM is required</param>
/// <param name="pTemplate">JVM configuration template, omit for defaults mandated by the identifier or to connect to an existing JVM</param>
/// <param name="ppJvmContainer">Receives the IJvmContainer instance which will give access to the underlying JVM</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT COMJVM_CORE_API ComJvmConnectA (/* [in][optional] */ PCSTR pszIdentifier, /* [in][optional] */ IJvmTemplate *pTemplate, /* [out][retval] */ IJvmContainer **ppJvmContainer) {
	try {
		_bstr_t bstrIdentifier (pszIdentifier);
		return ComJvmConnectB (bstrIdentifier, pTemplate, ppJvmContainer);
	} catch (_com_error &e) {
		return e.Error ();
	}
}

/// <summary>Establishes a connection to a JVM.</summary>
///
/// <para>This is the wide-character form of IJvmSupport#Connect.</para>
///
/// <param name="pszIdentifier">JVM identifier, omit if an anonymous JVM is required</param>
/// <param name="pTemplate">JVM configuration template, omit for defaults mandated by the identifier or to connect to an existing JVM</param>
/// <param name="ppJvmContainer">Receives the IJvmContainer instance which will give access to the underlying JVM</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT COMJVM_CORE_API ComJvmConnectW (/* [in][optional] */ PCWSTR pszIdentifier, /* [in][optional] */ IJvmTemplate *pTemplate, /* [out][retval] */ IJvmContainer **ppJvmContainer) {
	try {
		_bstr_t bstrIdentifier (pszIdentifier);
		return ComJvmConnectB (bstrIdentifier, pTemplate, ppJvmContainer);
	} catch (_com_error &e) {
		return e.Error ();
	}
}
