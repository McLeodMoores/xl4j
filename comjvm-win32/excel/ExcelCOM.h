/*
* Copyright 2014-present by McLeod Moores Software Limited.
* See distribution for license.
*/

#include "stdafx.h"
#pragma once

class CExcelCOM {
	IDispatch *pExcelDisp;
	CLSID applicationClassId;
	DISPID applicationCalculateFullRebuildDispId;
	DISPID applicationCalculationStateDispId;
	void ProdExcel();
public:
	enum XlState { xlDone, xlCalculating, xlPending };
	CExcelCOM();
	~CExcelCOM();
	HRESULT CalculateFullRebuild();
	HRESULT GetCalculationState(XlState *pState);
};