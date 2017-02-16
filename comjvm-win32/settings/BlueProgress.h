/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

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


