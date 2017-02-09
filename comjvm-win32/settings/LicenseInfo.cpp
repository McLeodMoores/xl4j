// LicenseInfo.cpp : implementation file
//

#include "stdafx.h"
#include "LicenseInfo.h"
#include "afxdialogex.h"
#include "../utils/Debug.h"


// CLicenseInfo dialog

IMPLEMENT_DYNAMIC(CLicenseInfo, CDialogEx)

CLicenseInfo::CLicenseInfo(CWnd* pParent /*=NULL*/, 
	                       wchar_t *szAppName, 
	                       wchar_t *szLicenseeText, 
	                       wchar_t *szLicenseText)
	: CDialogEx(IDD_LICENSEINFO, pParent)
{
	m_szAppName = szAppName;
	m_szLicenseeText = szLicenseeText;
	m_szLicenseText = szLicenseText;
}

CLicenseInfo::~CLicenseInfo()
{
}

void CLicenseInfo::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_NAME, m_title);
	DDX_Control(pDX, IDC_LICENSETEXT, m_licensee);
	DDX_Control(pDX, IDC_RICHEDITLICENSE, m_licenseText);
	//DDX_Control(pDX, IDC_SYSLINK1, m_syslink);
}


BEGIN_MESSAGE_MAP(CLicenseInfo, CDialogEx)
//	ON_WM_INITMENU()
ON_BN_CLICKED(IDOK, &CLicenseInfo::OnBnClickedOk)
ON_NOTIFY(NM_CLICK, IDC_SYSLINK1, &CLicenseInfo::OnNMClickSyslink1)
END_MESSAGE_MAP()


// CLicenseInfo message handlers


BOOL CLicenseInfo::OnInitDialog() {
	CDialogEx::OnInitDialog();
	LOGFONT logFont;
	logFont.lfHeight = 320;
	logFont.lfWidth = 0;
	logFont.lfEscapement = 0;
	logFont.lfOrientation = 0;
	logFont.lfWeight = FW_HEAVY;
	logFont.lfItalic = FALSE;
	logFont.lfUnderline = FALSE;
	logFont.lfStrikeOut = FALSE;
	logFont.lfCharSet = DEFAULT_CHARSET;
	logFont.lfOutPrecision = OUT_DEFAULT_PRECIS;
	logFont.lfClipPrecision = CLIP_DEFAULT_PRECIS;
	logFont.lfQuality = DEFAULT_QUALITY;
	logFont.lfPitchAndFamily = FF_SWISS;
	lstrcpy(logFont.lfFaceName, _T("Arial"));
	m_font.CreatePointFontIndirect(&logFont);
	m_title.SetFont(&m_font);
	m_title.SetWindowTextW((LPCTSTR)m_szAppName);
	m_licensee.SetWindowTextW((LPCTSTR)m_szLicenseeText);
	m_licenseText.SetWindowTextW((LPCTSTR)m_szLicenseText);
	return TRUE;  // return TRUE unless you set the focus to a control
				  // EXCEPTION: OCX Property Pages should return FALSE
}

INT_PTR CLicenseInfo::Open(HWND hwndParent) {
	return DoModal();
}


void CLicenseInfo::OnBnClickedOk() {
	CDialogEx::OnOK();
}


void CLicenseInfo::OnNMClickSyslink1(NMHDR *pNMHDR, LRESULT *pResult) {
	PNMLINK pNMLink = (PNMLINK)pNMHDR;
	// if more than one link, we need to strcmp the szUrl
	ShellExecuteW(NULL, L"open", pNMLink->item.szUrl, NULL, NULL, SW_SHOWNORMAL);
	*pResult = 0;
}
