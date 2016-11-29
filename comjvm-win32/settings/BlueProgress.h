#pragma once


// CBlueProgress

class CBlueProgress : public CProgressCtrl
{
	DECLARE_DYNAMIC(CBlueProgress)

public:
	CBlueProgress();
	virtual ~CBlueProgress();

protected:
	DECLARE_MESSAGE_MAP()
public:
	afx_msg HBRUSH OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor);
};


