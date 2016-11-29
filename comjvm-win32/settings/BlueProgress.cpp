// BlueProgress.cpp : implementation file
//

#include "stdafx.h"
#include "resource.h"
#include "BlueProgress.h"


// CBlueProgress

IMPLEMENT_DYNAMIC(CBlueProgress, CProgressCtrl)

CBlueProgress::CBlueProgress()
{

}

CBlueProgress::~CBlueProgress()
{
}


BEGIN_MESSAGE_MAP(CBlueProgress, CProgressCtrl)
	ON_WM_CTLCOLOR()
END_MESSAGE_MAP()



// CBlueProgress message handlers



HBRUSH CBlueProgress::OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor)
{
	HBRUSH hbr = CProgressCtrl::OnCtlColor(pDC, pWnd, nCtlColor);
	if (pWnd->GetDlgCtrlID() == IDC_PROGRESS1) {
		COLORREF blue = RGB(43, 87, 151);
		pDC->SetDCBrushColor(blue);
		pDC->SetDCPenColor(blue);
		return hbr;
	}
	return hbr;
}
