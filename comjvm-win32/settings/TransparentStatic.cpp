/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

// TransparentStatic.cpp : implementation file
//

#include "stdafx.h"
#include "resource.h"
#include "TransparentStatic.h"


// CTransparentStatic

IMPLEMENT_DYNAMIC(CTransparentStatic, CStatic)

CTransparentStatic::CTransparentStatic()
{

}

CTransparentStatic::~CTransparentStatic()
{
}


BEGIN_MESSAGE_MAP(CTransparentStatic, CStatic)
	ON_WM_CTLCOLOR()
END_MESSAGE_MAP()



// CTransparentStatic message handlers




HBRUSH CTransparentStatic::OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor)
{
	HBRUSH hbr = CStatic::OnCtlColor(pDC, pWnd, nCtlColor);
	if (nCtlColor == CTLCOLOR_STATIC && pWnd->GetDlgCtrlID() == IDC_LICENSETEXT)
	{
		COLORREF white = RGB(255, 255, 255);
		pDC->SetTextColor(white);
		pDC->SetBkMode(TRANSPARENT);
		return static_cast<HBRUSH>(GetStockObject(NULL_BRUSH));
	}
	return static_cast<HBRUSH>(GetStockObject(NULL_BRUSH));
}
