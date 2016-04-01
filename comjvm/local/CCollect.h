#pragma once

#include "core_h.h"
#include "Jvm.h"
#include "JniCache.h"

class CCollect;

#include "CCollectExecutor.h"

class CCollect : public ICollect {
private:
	volatile ULONG m_lRefCount;

	/// <summary>Lock for this object.</summary>
	CRITICAL_SECTION m_cs;
	CJvm *m_pJvm;
	JniCache *m_pJniCache;
	CCollectExecutor *m_pExecutor;
	IJvmConnector *m_pConnector;
public:
	CCollect (CJvm *pJvm);
	~CCollect ();
	HRESULT STDMETHODCALLTYPE Collect (/* [in] */ SAFEARRAY *psaValidIds, /* [retval][out] */ hyper *piAllocations);
	ULONG STDMETHODCALLTYPE AddRef ();
	ULONG STDMETHODCALLTYPE Release ();
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
	IJvm * STDMETHODCALLTYPE getJvm () { return m_pJvm; }
};