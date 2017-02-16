/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

class CCollectExecutor;

#include "CCollect.h"

class CCollectExecutor {
private:
	volatile long m_lRefCount;
	CCollect *m_pOwner;
	JniCache *m_pJniCache;
	hyper *m_piAllocations; // 64-bit int
	SAFEARRAY *m_psaValidIds;
	HANDLE m_hSemaphore;
	HRESULT m_hRunResult;
	
	~CCollectExecutor ();
public:
	CCollectExecutor (CCollect *pOwner, JniCache *pJniCache);
	void SetArguments (SAFEARRAY * psaValidIds, hyper *piAllocations);
	HRESULT Run (JNIEnv *pEnv);
	HRESULT Wait ();
	void AddRef ();
	void Release ();
};