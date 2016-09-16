#pragma once

#include "../core/core_h.h"
#include "Jvm.h"

class CScan;

#include "CScanExecutor.h"

class CScan : public IScan {
private:
	volatile ULONG m_lRefCount;

	/// <summary>Lock for this object.</summary>
	CRITICAL_SECTION m_cs;
	CJvm *m_pJvm;
	IJvmConnector *m_pConnector;
public:
	CScan (CJvm *pJvm);
	~CScan ();
	HRESULT STDMETHODCALLTYPE Scan (/* [retval][out] */ SAFEARRAY * *result);
	ULONG STDMETHODCALLTYPE AddRef ();
	ULONG STDMETHODCALLTYPE Release ();
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
	IJvm * STDMETHODCALLTYPE GetJvm () { return m_pJvm; }
};