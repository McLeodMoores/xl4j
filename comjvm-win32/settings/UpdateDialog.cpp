/*
* Copyright 2014-present by McLeod Moores Software Limited.
* See distribution for license.
*/

// UpdateDialog.cpp : implementation file
//

#include "stdafx.h"
#include "UpdateDialog.h"
#include "afxdialogex.h"
#include "utils/Debug.h"
#include "utils/DateUtils.h"


// CUpdateDialog dialog

IMPLEMENT_DYNAMIC(CUpdateDialog, CDialogEx)

CUpdateDialog::CUpdateDialog(CWnd* pParent /*=NULL*/,
	CSettings *pSettings,
	wchar_t *szUpgradeText,
	wchar_t *szURL)
	: CDialogEx(IDD_UPGRADEDIALOG, pParent) {
	m_pSettings = pSettings;
	m_szUpgradeText = szUpgradeText;
	m_szURL = szURL;
}

CUpdateDialog::~CUpdateDialog() {
}

void CUpdateDialog::DoDataExchange(CDataExchange* pDX) {
	CDialogEx::DoDataExchange(pDX);
	//DDX_Control(pDX, IDC_SYSLINK1, m_syslink);
	DDX_Control(pDX, IDC_GOAWAY60_CHECK, m_cbGoAway60);
	DDX_Control(pDX, IDC_CHECKFORUPGRADES_CHECK, m_cbCheckForUpgrades);
	DDX_Control(pDX, IDC_SYSLINK1, m_slUpgradeLink);
}


BEGIN_MESSAGE_MAP(CUpdateDialog, CDialogEx)
	//	ON_WM_INITMENU()
	ON_BN_CLICKED(IDOK, &CUpdateDialog::OnBnClickedOk)
	ON_NOTIFY(NM_CLICK, IDC_SYSLINK1, &CUpdateDialog::OnNMClickSyslink1)
END_MESSAGE_MAP()


// CUpdateDialog message handlers


BOOL CUpdateDialog::OnInitDialog() {
	CDialogEx::OnInitDialog();
	m_slUpgradeLink.SetWindowTextW((LPCTSTR)m_szUpgradeText);
	m_cbCheckForUpgrades.SetCheck(BST_CHECKED);
	return TRUE;  // return TRUE unless you set the focus to a control
				  // EXCEPTION: OCX Property Pages should return FALSE
}

INT_PTR CUpdateDialog::Open(HWND hwndParent) {
	return DoModal();
}


void CUpdateDialog::OnBnClickedOk() {
	int daysToGoAway;
	if (m_cbGoAway60.GetCheck() == BST_CHECKED) {
		daysToGoAway = 60;
	} else {
		daysToGoAway = 1;
	}
	SYSTEMTIME now, later;
	GetSystemTime(&now);
	DateUtils::AddDays(now, daysToGoAway, &later);
	size_t cchDate;
	HRESULT hr;
	if (FAILED(hr = DateUtils::DateToStr(later, nullptr, &cchDate))) {
		LOGERROR("DateToStr error: %s", HRESULT_TO_STR(hr));
		goto exit;
	}
	wchar_t *szDate = (wchar_t *)calloc(cchDate, sizeof(wchar_t));
	if (!szDate) {
		LOGERROR("calloc failed");
		goto exit;
	}
	if (FAILED(hr = DateUtils::DateToStr(later, szDate, &cchDate))) {
		LOGERROR("DateToStr error: %s", HRESULT_TO_STR(hr));
		goto exit;
	}
	m_pSettings->PutString(SECTION_ADDIN, KEY_UPGRADE_EARLIEST, szDate);
	if (m_cbCheckForUpgrades.GetCheck() == BST_CHECKED) {
		m_pSettings->PutString(SECTION_ADDIN, KEY_UPGRADE_CHECK_REQUIRED, VALUE_UPGRADE_CHECK_REQUIRED_YES);
	} else {
		m_pSettings->PutString(SECTION_ADDIN, KEY_UPGRADE_CHECK_REQUIRED, VALUE_UPGRADE_CHECK_REQUIRED_NO);
	}
exit:
	CDialogEx::OnOK();
}


void CUpdateDialog::OnNMClickSyslink1(NMHDR *pNMHDR, LRESULT *pResult) {
	PNMLINK pNMLink = (PNMLINK)pNMHDR;
	// if more than one link, we need to strcmp the szUrl
	ShellExecuteW(NULL, L"open", pNMLink->item.szUrl, NULL, NULL, SW_SHOWNORMAL);
	*pResult = 0;
}
