/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once
#include "afxwin.h"
#include "afxcmn.h"
#include "resource.h"
#include "LicenseInfoInterface.h"
// CLicenseInfo dialog

class CLicenseInfo : public CDialogEx, public ILicenseInfo
{
	DECLARE_DYNAMIC(CLicenseInfo)

public:
	CLicenseInfo(CWnd * pParent, wchar_t * szAppName, wchar_t * licenseeText, wchar_t * licenseText);
	// standard constructor
	virtual ~CLicenseInfo();

// Dialog Data
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_LICENSEINFO };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	
	DECLARE_MESSAGE_MAP()
public:
	CStatic m_title;
	CStatic m_licensee;
	CRichEditCtrl m_licenseText;
	CFont m_font;
	wchar_t *m_szAppName;
	wchar_t *m_szLicenseeText;
	wchar_t *m_szLicenseText;
	virtual BOOL OnInitDialog();
	virtual INT_PTR Open(HWND hwndParent);
	afx_msg void OnBnClickedOk();
	afx_msg void OnNMClickSyslink1(NMHDR *pNMHDR, LRESULT *pResult);
};
