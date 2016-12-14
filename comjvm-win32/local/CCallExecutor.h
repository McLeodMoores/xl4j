#pragma once

class CCallExecutor;

#include "CCall.h"

const IID XL4JOPER12_IID = {
	0x053798d7,
	0xeef0,
	0x4ac5,
	{
		0x8e,
		0xb8,
		0x4d,
		0x51,
		0x5e,
		0x7c,
		0x5d,
		0xb5
	}
};

class CCallExecutor {
private:
	volatile long m_lRefCount;
	CCall *m_pOwner;
	JniCache *m_pJniCache;
	VARIANT m_asyncResult;
	VARIANT *m_pResult;
	int m_iFunctionNum;
	SAFEARRAY *m_pArgs;
	VARIANT m_vAsyncHandle;
	IAsyncCallResult *m_pAsyncHandler;
	HANDLE m_hSemaphore;
	HRESULT m_hRunResult;
	~CCallExecutor ();
public:
	CCallExecutor (CCall *pOwner, JniCache *pJniCache);
	void SetArguments (VARIANT *result, int iFunctionNum, SAFEARRAY * args);
	void SetAsynchronous(IAsyncCallResult *pAsyncHandler, VARIANT vAsyncHandle) { 
		m_vAsyncHandle = vAsyncHandle; 
		m_pAsyncHandler = pAsyncHandler; 
	}
	VARIANT GetAsyncrhonousHandle() { return m_vAsyncHandle; }
	IAsyncCallResult *GetAsynchronousHandler() { return m_pAsyncHandler; }
	VARIANT *GetResult() { return m_pResult; }
	HRESULT Run (JNIEnv *pEnv);
	HRESULT Wait ();
#if 1
	HRESULT CCallExecutor::Wait(int timeoutMillis, bool *timedOut);
#endif // 1

	void AddRef ();
	void Release ();
};