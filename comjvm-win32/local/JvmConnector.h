/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

#include "../core/core_h.h"
#include "core/InstanceHolder.h"

/// <summary>Factory for creating CJvmConnector instances.</summary>
class CJvmConnectorFactory : public IClassFactory {
private:
	friend class CInstanceHolder<CJvmConnectorFactory>;
	static CInstanceHolder<CJvmConnectorFactory> s_oInstance;
	volatile ULONG m_lRefCount;
	CJvmConnectorFactory ();
	~CJvmConnectorFactory ();
public:
	static CJvmConnectorFactory *Instance ();
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
    ULONG STDMETHODCALLTYPE AddRef ();
    ULONG STDMETHODCALLTYPE Release ();
	// IClassFactory
	HRESULT STDMETHODCALLTYPE CreateInstance (
	  /* [in] */ IUnknown *pUnkOuter,
	  /* [in] */ REFIID riid,
	  /* [out] */ void **ppvObject);
	HRESULT STDMETHODCALLTYPE LockServer (
	  /* [in] */ BOOL fLock);
};

/// <summary>Implementation of IJvmSupport.</summary>
///
/// <para>Instances of this are created by the ComJvmCreateLocal function or a
/// CJvmConnectorFactory.</para>
class CJvmConnector : public IJvmConnector {
private:
	volatile ULONG m_lRefCount;
	~CJvmConnector ();
public:
	CJvmConnector ();
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
    ULONG STDMETHODCALLTYPE AddRef ();
    ULONG STDMETHODCALLTYPE Release ();
	// IJvmConnector
    HRESULT STDMETHODCALLTYPE Lock ();
    HRESULT STDMETHODCALLTYPE FindJvm ( 
        /* [in] */ long lIndex,
        /* [optional][in] */ BSTR bstrLogicalIdentifier,
        /* [retval][out] */ IJvm **ppJvm);
    HRESULT STDMETHODCALLTYPE CreateJvm ( 
        /* [in] */ IJvmTemplate *pTemplate,
        /* [optional][in] */ BSTR bstrLogicalIdentifier,
        /* [retval][out] */ IJvm **ppJvm);
    HRESULT STDMETHODCALLTYPE Unlock ();
};