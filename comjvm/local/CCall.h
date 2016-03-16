#pragma once

#include "core_h.h"
#include "Jvm.h"
#include "JniCache.h"

class CCall;

#include "CCallExecutor.h"

class CCall : public ICall {
private:
	volatile ULONG m_lRefCount;
	
	/// <summary>Lock for this object.</summary>
	CRITICAL_SECTION m_cs;
	CJvm *m_pJvm;
	JniCache *m_pJniCache;
	CCallExecutor *m_pExecutor;
	IJvmConnector *m_pConnector;
public:
//	std::list<long long> m_timings;
	CCall (CJvm *pJvm);
	~CCall ();
	HRESULT STDMETHODCALLTYPE call (/* [out] */ VARIANT *result, /* [in] */ int iFunctionNum, /* [in] */ SAFEARRAY * args);
	ULONG STDMETHODCALLTYPE AddRef ();
	ULONG STDMETHODCALLTYPE Release ();
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
	IJvm * STDMETHODCALLTYPE getJvm () { return m_pJvm; }
	JniCache * STDMETHODCALLTYPE getJniCache () { return m_pJniCache; }
};