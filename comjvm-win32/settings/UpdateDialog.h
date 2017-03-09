/*
* Copyright 2014-present by McLeod Moores Software Limited.
* See distribution for license.
*/

#pragma once
#include "afxwin.h"
#include "afxcmn.h"
#include "resource.h"
#include "UpdateDialogInterface.h"
// CUpdateDialog dialog

class CUpdateDialog : public CDialogEx, public IUpdateDialog {
	DECLARE_DYNAMIC(CUpdateDialog)

public:
	CUpdateDialog(CWnd * pParent, CSettings *pSettings, wchar_t * szUpdateText, wchar_t * szURL);
	// standard constructor
	virtual ~CUpdateDialog();

	// Dialog Data
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_UPGRADEDIALOG };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	CSettings *m_pSettings;
	wchar_t *m_szUpgradeText;
	wchar_t *m_szURL;
	virtual BOOL OnInitDialog();
	virtual INT_PTR Open(HWND hwndParent);
	afx_msg void OnBnClickedOk();
	afx_msg void OnNMClickSyslink1(NMHDR *pNMHDR, LRESULT *pResult);
	// Go away for 60 days
	CButton m_cbGoAway60;
	// Check for upgrades
	CButton m_cbCheckForUpgrades;
	// Upgrade text and link
	CLinkCtrl m_slUpgradeLink;
};
#pragma once
