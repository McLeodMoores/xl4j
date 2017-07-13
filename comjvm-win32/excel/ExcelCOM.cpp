/*
* Copyright 2014-present by McLeod Moores Software Limited.
* See distribution for license.
*/

#include "stdafx.h"
#include "ExcelUtils.h"
#include "ExcelCOM.h"

CExcelCOM::CExcelCOM() {
	ProdExcel();
	HRESULT hr = CLSIDFromProgID(L"Excel.Application", &applicationClassId);
	if (FAILED(hr)) {
		LOGERROR("Error obtaining COM class ID, COM probably not initialised");
		//ExcelUtils::ErrorMessageBox(L"Error obtaining COM class ID, COM probably not initialised");
		return;
	}
	IUnknown *pUnknown;
	hr = GetActiveObject(applicationClassId, NULL, &pUnknown);
	if (FAILED(hr)) {
		LOGERROR("Error obtaining Excel Application object, COM probably not initialised");
		//ExcelUtils::ErrorMessageBox(L"Error obtaining Excel Application object, COM probably not initialised");
		return;
	}
	hr = pUnknown->QueryInterface(IID_IDispatch, (void **)&pExcelDisp);
	if (FAILED(hr)) {
		LOGERROR("Error obtaining Excel Application object dispatch interface, COM probably not initialised");
		//ExcelUtils::ErrorMessageBox(L"Error obtaining Excel Application object dispatch interface, COM probably not initialised");
		return;
	}
	pUnknown->Release();
	applicationCalculateFullRebuildDispId = 0;
	applicationCalculationStateDispId = 0;
}

CExcelCOM::~CExcelCOM() {
	if (pExcelDisp) {
		pExcelDisp->Release();
		pExcelDisp = nullptr;
	}
}

void CExcelCOM::ProdExcel() {
	HWND hWnd;
	if ((hWnd = FindWindow(L"XLMAIN", 0)) != NULL) {
		// Tell Excel to register itself with the ROT (run object table)
		SendMessage(hWnd, WM_USER + 18, 0, 0);
	}
}

HRESULT CExcelCOM::CalculateFullRebuild() {
	LOGINFO("CalculateFullRebuild");
	if (!pExcelDisp) {
		LOGERROR("Excel dispatch interface not set, cannot refresh");
		return S_FALSE;
	}
	LPOLESTR bstrCalculateFullRebuild = SysAllocString(L"CalculateFullRebuild");
	if (!applicationCalculateFullRebuildDispId) {
		HRESULT hr = pExcelDisp->GetIDsOfNames(IID_NULL, &bstrCalculateFullRebuild, 1, LOCALE_SYSTEM_DEFAULT, &applicationCalculateFullRebuildDispId);
		if (FAILED(hr)) {
			LOGERROR("Could not get dispatch ID for CalculateFullRebuild method on Excel.Application object: %s", HRESULT_TO_STR(hr));
			return hr;
		}
	}
	DISPPARAMS params;
	params.rgdispidNamedArgs = NULL;
	params.rgvarg = NULL;
	params.cArgs = 0;
	params.cNamedArgs = 0;
	LOGINFO("About to invoke on Application.CalculateFullRebuild (dispid = %d, pExcelDisp = %p)", applicationCalculateFullRebuildDispId, pExcelDisp);
	HRESULT hr = pExcelDisp->Invoke(applicationCalculateFullRebuildDispId, IID_NULL, LOCALE_SYSTEM_DEFAULT, DISPATCH_METHOD, &params, NULL, NULL, NULL);
	if (FAILED(hr)) {
		LOGERROR("Could not call Application.CalculateFullRebuild on Excel object, returned error %s", HRESULT_TO_STR(hr));
		return hr;
	}
	LOGINFO("Invocation succeeded");
	return S_OK;
}

HRESULT CExcelCOM::GetCalculationState(XlState * pState) {
	LOGINFO("CalculateFullRebuild");
	if (!pExcelDisp) {
		LOGERROR("Excel dispatch interface not set, cannot refresh");
		return S_FALSE;
	}
	LPOLESTR bstrCalculationState = SysAllocString(L"CalculationState");
	if (!applicationCalculationStateDispId) {
		HRESULT hr = pExcelDisp->GetIDsOfNames(IID_NULL, &bstrCalculationState, 1, LOCALE_SYSTEM_DEFAULT, &applicationCalculationStateDispId);
		if (FAILED(hr)) {
			LOGERROR("Could not get dispatch ID for CalculateFullRebuild method on Excel.Application object: %s", HRESULT_TO_STR(hr));
			return hr;
		}
	}
	DISPPARAMS params;
	params.rgdispidNamedArgs = NULL;
	params.rgvarg = NULL;
	params.cArgs = 0;
	params.cNamedArgs = 0;
	LOGINFO("About to invoke on Application.CalculationState (dispid = %d, pExcelDisp = %p)", applicationCalculateFullRebuildDispId, pExcelDisp);
	VARIANT vResult;
	VariantInit(&vResult);
	HRESULT hr = pExcelDisp->Invoke(applicationCalculationStateDispId, IID_NULL, LOCALE_SYSTEM_DEFAULT, DISPATCH_PROPERTYGET, &params, &vResult, NULL, NULL);
	if (FAILED(hr)) {
		LOGERROR("Could not call Application.CalculationState on Excel object, returned error %s", HRESULT_TO_STR(hr));
		return hr;
	}
	LOGINFO("Variant type = %d", V_VT(&vResult));
	if (V_VT(&vResult) == VT_I4) {
		LOGINFO("Return value was %d", V_I4(&vResult));
	}
	*pState = (XlState)V_I4(&vResult);
	LOGINFO("Invocation succeeded");
	return S_OK;
}
