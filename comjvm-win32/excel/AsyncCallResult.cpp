/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

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
	LPXLOPER12 result = (LPXLOPER12) calloc (1, sizeof(XLOPER12));
	if (!result) {
		hr = E_OUTOFMEMORY;
		goto error;
	}
	LPXLOPER12 handle = (LPXLOPER12) calloc (1, sizeof(XLOPER12));
	if (!handle) {
		hr = E_OUTOFMEMORY;
		goto error;
	}
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
	XLOPER12 returnResult; // this return value isn't actually used.
	result->xltype |= xlbitDLLFree;
	handle->xltype |= xlbitDLLFree;
	LOGTRACE("Handle = %p, size = %d", handle->val.bigdata.h.hdata, sizeof(handle->val.bigdata.h.hdata));
	LOGTRACE("BigData.cbData = %d", handle->val.bigdata.cbData);
	int retVal = Excel12(xlAsyncReturn, &returnResult, 2, handle, result);
	LOGTRACE("retVal from xlAsyncReturn was %d", retVal);
	if (retVal == xlretSuccess) {
		LOGTRACE("xlAsyncReturn was good");
		hr = S_OK;
	} else if (retVal == xlretInvAsynchronousContext) {
		LOGTRACE("xlAsyncReturn returned xlretInvAsynchronousContext, indicating an invalid handle was passed.");
		hr = E_FAIL;
	} else {
		LOGTRACE("xlAsyncReturn returned unexpected errpr code %d.", retVal);
		hr = E_FAIL;
	}
error:
	//if (result) free(result);
	//if (handle) free(handle);
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
