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
	VARIANT *m_pResult;
	int m_iFunctionNum;
	SAFEARRAY *m_pArgs;
	bool m_bAsync;
	HANDLE m_hSemaphore;
	HRESULT m_hRunResult;
	~CCallExecutor ();
public:
	CCallExecutor (CCall *pOwner, JniCache *pJniCache);
	void SetArguments (VARIANT *result, int iFunctionNum, SAFEARRAY * args);
	void SetAsynchronous(bool bAsync) { m_bAsync = bAsync; }
	HRESULT Run (JNIEnv *pEnv);
	HRESULT Wait ();
#if 1
	HRESULT CCallExecutor::Wait(int timeoutMillis, bool *timedOut);
#endif // 1

	void AddRef ();
	void Release ();
};