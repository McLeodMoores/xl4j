/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once
#include "Resource.h"
#include "afxwin.h"
#include "afxcmn.h"
#include "TransparentStatic.h"
#include "BlueProgress.h"
#include "CloseButton.h"
#include "MinimiseButton.h"

// CSplashScreen dialog

class CSplashScreen : public CDialog, public ISplashScreen
{
	DECLARE_DYNAMIC(CSplashScreen)
	ULONG m_lRefCount;
	enum State { CREATED, OPEN, VISIBLE, HIDDEN, CLOSED };
	CRITICAL_SECTION m_cs;
	State m_state;
	virtual bool IsSplashOpen();
	virtual void Show();
	virtual void Hide();
	virtual void HideIfSplashOpen();
public:
	CSplashScreen(CString& licenseeText, CWnd* pParent = NULL);   // standard constructor
	virtual ~CSplashScreen();
	virtual void Update(int iProgress);
	virtual void Increment();
	virtual void SetStep(int iStep);
	virtual void SetMax(int iMax);
	virtual void SetMarquee();
	virtual INT_PTR Open(HWND hwndParent);
	virtual void Close();
	virtual void CloseMT();
	virtual ULONG Release();
	virtual ULONG AddRef();
// Dialog Data
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_SPLASHWINDOW };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	virtual BOOL OnInitDialog();

	DECLARE_MESSAGE_MAP()
public:
	CTransparentStatic m_stLicensee;
	CBlueProgress m_prProgress;
	CString m_csLicenseeText;
	afx_msg HBRUSH OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor); 
	
	CCloseButton m_btClose;
	CMinimiseButton m_btMinimise;
	afx_msg void OnMouseMove(UINT nFlags, CPoint point);
	afx_msg void OnBnClickedClosebutton();
	afx_msg void OnBnClickedMinimisebutton();
 };
