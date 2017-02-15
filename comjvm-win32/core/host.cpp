/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

/// @file

#include "stdafx.h"
#include "core.h"
#include "internal.h"
#include "JvmSupport.h"

HRESULT ComJvmGetHostB (BSTR *pbstrHost) {
	if (!pbstrHost) return E_POINTER;
	PTSTR pszHost = NULL;
	HRESULT hr;
	try {
		if (FAILED (hr = ComJvmGetHost (&pszHost))) return hr;
		_bstr_t bstrHost (pszHost);
		*pbstrHost = bstrHost.Detach ();
		return S_OK;
	} catch (_com_error &e) {
		if (pszHost) CoTaskMemFree (pszHost);
		return e.Error ();
	}
}

/// <summary>Queries the host name.</summary>
///
/// <para>This is the ANSI form of IJvmSupport#get_Host.</para>
///
/// <param name="ppszHost">Receives the host name. The caller must free this string when done using CoTaskMemFree</param>
/// <returns>S_OK if successful, otherwise an error code</returns>
HRESULT COMJVM_CORE_API ComJvmGetHostA (/* [out][retval] */ PSTR *ppszHost) {
	if (!ppszHost) return E_POINTER;
	DWORD cchName = 0;
	if (GetComputerNameExA (ComputerNameDnsFullyQualified, NULL, &cchName)) return E_ABORT;
	if (GetLastError () != ERROR_MORE_DATA) return E_ABORT;
	PSTR pszName = (PSTR)CoTaskMemAlloc (cchName * sizeof (CHAR));
	if (!pszName) return E_OUTOFMEMORY;
	if (!GetComputerNameExA (ComputerNameDnsFullyQualified, pszName, &cchName)) {
		CoTaskMemFree (pszName);
		return HRESULT_FROM_WIN32 (GetLastError ());
	}
	*ppszHost = pszName;
	return S_OK;
}

/// <summary>Queries the host name.</summary>
///
/// <para>This is the wide-character form of IJvmSupport#get_Host.</para>
///
/// <param name="ppszHost">Receives the host name. The caller must free this string when done using CoTaskMemFree</param>
/// <returns>S_OK if successful, otherwise an error code</returns>
HRESULT COMJVM_CORE_API ComJvmGetHostW (/* [out][retval] */ PWSTR *ppszHost) {
	if (!ppszHost) return E_POINTER;
	DWORD cchName = 0;
	if (GetComputerNameExW (ComputerNameDnsFullyQualified, NULL, &cchName)) return E_ABORT;
	if (GetLastError () != ERROR_MORE_DATA) return E_ABORT;
	PWSTR pszName = (PWSTR)CoTaskMemAlloc (cchName * sizeof (WCHAR));
	if (!pszName) return E_OUTOFMEMORY;
	if (!GetComputerNameExW (ComputerNameDnsFullyQualified, pszName, &cchName)) {
		CoTaskMemFree (pszName);
		return HRESULT_FROM_WIN32 (GetLastError ());
	}
	*ppszHost = pszName;
	return S_OK;
}

/// <summary>Gets the CLSID of the default IJvmSupport object instance.</summary>
///
/// <para>This CLSID can be used with COM methods to obtain a class factory that
/// will construct instances using ComJvmCreateInstance.</para>
///
/// <param name="pClass">Receives the CLSID</param>
///	<returns>S_OK if successful, otherwise an error code</returns>
HRESULT COMJVM_CORE_API ComJvmGetCLSID(/* [out][retval] */ CLSID *pClass) {
	if (!pClass) return E_POINTER;
	*pClass = CLSID_JvmSupport;
	return S_OK;
}

/// <summary>Creates a local COM-JVM library instance.</summary>
///
/// <para>All of the exported APIs from this DLL are available as members of an
///	IJvmSupport instance. This is to support certain forms of remote invocation
///	and to provide access to environments which can create COM object instances
///	but not easily load and call into arbitrary DLLs.</para>
///
///	<param name="ppJvmSupport">Pointer to a variable that will receive the new
///	IJvmSupport instance</param>
///	<returns>S_OK if successful, otherwise an error code</returns>
HRESULT COMJVM_CORE_API ComJvmCreateInstance (/* [out][retval] */ IJvmSupport **ppJvmSupport) {
	if (!ppJvmSupport) return E_POINTER;
	*ppJvmSupport = new CJvmSupport ();
	if (!*ppJvmSupport) return E_OUTOFMEMORY;
	return S_OK;
}
