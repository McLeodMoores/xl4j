#include "stdafx.h"
#include "AsyncCallResult.h"
#include "Excel.h"

CAsyncCallResult::CAsyncCallResult(CAddinEnvironment *pAddinEnvironment) {
	m_pAddinEnvironment = pAddinEnvironment;
	AddRef();
}

CAsyncCallResult::~CAsyncCallResult() {
}

const IID CAsyncCallResult::IID_IAsyncCallResult = {
	0xb8c4990d, 0xd0e0, 0x4b53,{ 0xa3, 0x57, 0xec, 0x7e, 0xde, 0x1c, 0x85, 0xfe }
};

HRESULT CAsyncCallResult::Complete(VARIANT vAsyncHandle, VARIANT *pvResult) {
	LOGTRACE("Callback happened result is %p", pvResult);
	Converter *pConverter;
	HRESULT hr;
	LPXLOPER12 result = TempMissing12();
	LPXLOPER12 handle = TempMissing12();
	if (SUCCEEDED(hr = m_pAddinEnvironment->GetConverter(&pConverter))) {
		hr = pConverter->convert(pvResult, result);
		if (FAILED(hr)) {
			LOGERROR("Async result handler: Result conversion failed");
			goto error;
		}
		hr = pConverter->convert(&vAsyncHandle, handle);
		if (FAILED(hr)) {
			LOGERROR("Async result handler: handle conversion failed");
			goto error;
		}
	} else {
		LOGERROR("Could not get valid converter");
		goto error;
	}
	VariantClear(pvResult); // free COM data structures recursively.  This only works because we use IRecordInfo::SetField.
	LOGTRACE("Async result handler: conversion complete, returning value (type=%d) to Excel", result->xltype);
	XLOPER12 returnResult;
	LOGTRACE("Handle = %p", handle->val.bigdata.h.hdata);
	LOGTRACE("BigData.cbData = %d", handle->val.bigdata.cbData);
	Excel12f(xlAsyncReturn, &returnResult, 2, handle, result);
	if (returnResult.val.xbool) {
		return S_OK;
	} else {
		LOGERROR("xlAsyncReturn returned FALSE, indicating an error occurred.");
		return E_FAIL;
	}
error:
	return hr;
}

HRESULT CAsyncCallResult::QueryInterface(REFIID riid, void ** ppvObject) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*> (this);
	} else if (riid == IID_IAsyncCallResult) {
		*ppvObject = static_cast<IAsyncCallResult*> (this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef();
	return S_OK;
}

ULONG CAsyncCallResult::AddRef(void) {
	return InterlockedIncrement(&m_lRefCount);
}

ULONG CAsyncCallResult::Release(void) {
	ULONG lResult = InterlockedDecrement(&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}
