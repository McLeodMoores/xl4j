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
//
//const IID ComJvmCore_LIBID = {
//	0x0e07a0b8,
//	0x0fa3,
//	0x4497,
//	{
//		0xbc,
//		0x66,
//		0x6d,
//		0x2a,
//		0xf2,
//		0xa0,
//		0xb9,
//		0xc8
//	}
//};

class CCallExecutor {
private:
	volatile long m_lRefCount;
	CCall *m_pOwner;
	XL4JOPER12 *m_pResult;
	int m_iFunctionNum;
	SAFEARRAY *m_pArgs;
	HANDLE m_hSemaphore;
	HRESULT m_hRunResult;
	~CCallExecutor ();
	void allocSAFEARRAY_BSTR (SAFEARRAY **ppsa, size_t cElem);
	void freeBSTR (BSTR pStr);
	HRESULT storeBSTR (JNIEnv *pEnv, jstring jsStr, BSTR *result);
	HRESULT storeXCHAR (JNIEnv *pEnv, jstring jsStr, XCHAR **result);
	void allocMREF (XL4JMREF12 **result, jsize elems);
	void allocArray (XL4JOPER12 **result, jsize rows, jsize cols);
	XL4JOPER12 convert (JNIEnv *pEnv, jobject joXLValue);
	jobject convert (JNIEnv *pEnv, XL4JOPER12 *oper);
public:
	CCallExecutor (CCall *pOwner, XL4JOPER12 *result, int iFunctionNum, SAFEARRAY * args);
	HRESULT Run (JNIEnv *pEnv);
	HRESULT Wait ();
	void AddRef ();
	void Release ();
};