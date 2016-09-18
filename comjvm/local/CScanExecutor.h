#pragma once

class CScanExecutor;

#include "CScan.h"

const IID FUNCTIONINFO_IID = { 0xdff6d900, 0xb72f, 0x4f06, { 0xa1, 0xad, 0x04, 0x66, 0xad, 0x25, 0xc3, 0x52 } };
const IID ComJvmCore_LIBID = { 0x0e07a0b8, 0x0fa3, 0x4497, { 0xbc, 0x66, 0x6d, 0x2a, 0xf2, 0xa0, 0xb9, 0xc8 } };

class CScanExecutor {
private:
	volatile long m_lRefCount;
	CScan *m_pOwner;
	SAFEARRAY **m_pResults;
	HANDLE m_hSemaphore;
	HRESULT m_hRunResult;
	~CScanExecutor ();
	void allocSAFEARRAY_BSTR (SAFEARRAY **ppsa, size_t cElem);
	void freeBSTR (BSTR pStr);
	HRESULT storeBSTR (JNIEnv *pEnv, jstring jsStr, BSTR *result);
public:
	CScanExecutor (CScan *pOwner, SAFEARRAY **pResults);
	HRESULT Run (JNIEnv *pEnv);
	HRESULT Wait ();
	void AddRef ();
	void Release ();
};