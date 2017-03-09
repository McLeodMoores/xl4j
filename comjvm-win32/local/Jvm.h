/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

#include "core/AbstractJvm.h"
#include "jni/jni.h"

class CJvm : public CAbstractJvm {
private:
	DWORD m_dwJvm;
protected:
	~CJvm ();
public:
	CJvm (IJvmTemplate *pTemplate, const GUID *pguid, DWORD dwJvm);
	HRESULT Execute (JNICallbackProc pfnCallback, LPVOID lpData);
	HRESULT ExecuteAsync(JNICallbackProc pfnCallback, LPVOID lpData);
	// IJvm
	HRESULT STDMETHODCALLTYPE FlushAsyncThreads();
	HRESULT STDMETHODCALLTYPE CreateScan (
		/* [retval][out] */ IScan **ppScan);
	HRESULT STDMETHODCALLTYPE CreateCall (
		/* [retval][out] */ ICall **ppCall);
	HRESULT STDMETHODCALLTYPE CreateCollect (
		/* [retval][out] */ ICollect **ppCollect);
};