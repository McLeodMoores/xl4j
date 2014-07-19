/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

#include "core_h.h"

/// <summary>Implementation of IClasspath.</summary>
///
/// <para>Each classpath instance has a logical "owner" - typically the identifier
/// of the JVM that it is being created for. This is used to scope the cache files
/// to avoid collisions between different applications.</para>
class CClasspath : public IClasspath {
private:
	volatile ULONG m_lRefCount;
	mutable CRITICAL_SECTION m_cs;
	const _std_string_t m_strOwner;
	const _std_string_t m_strHost;
	std::list<const _std_string_t> m_astrPath;
	~CClasspath ();
public:
	CClasspath (const _std_string_t &strOwner);
	std::list<const _std_string_t> GetPathComponents () const;
	PTSTR GetPath () const;
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
    ULONG STDMETHODCALLTYPE AddRef ();
    ULONG STDMETHODCALLTYPE Release ();
	// IClasspath
    HRESULT STDMETHODCALLTYPE AddFolder ( 
        /* [in] */ IClassFolder *pFolder);
    HRESULT STDMETHODCALLTYPE AddJar ( 
        /* [in] */ IJarFile *pJar);
	/* [propget] */ HRESULT STDMETHODCALLTYPE get_LocalPath (
		/* [retval][in] */ BSTR *pbstrPath);
};