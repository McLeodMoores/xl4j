#pragma once

#include "core_h.h"
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
	//HRESULT call (/* [out] */ XL4JOPER12 *result, /* [in] */ int iFunctionNum, /* [in] */ SAFEARRAY * args);
	HRESULT STDMETHODCALLTYPE scan (/* [retval][out] */ SAFEARRAY * *result);
	ULONG STDMETHODCALLTYPE AddRef ();
	ULONG STDMETHODCALLTYPE Release ();
	//HANDLE STDMETHODCALLTYPE BeginExecution ();
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
	//void STDMETHODCALLTYPE EndExecution (HANDLE hSemaphore);
	IJvm * STDMETHODCALLTYPE getJvm () { return m_pJvm; }
};