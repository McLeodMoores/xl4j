// SplashScreen.cpp : implementation file
//

#include "stdafx.h"
#include "SplashScreenInterface.h"
#include "SplashScreen.h"
#include "afxdialogex.h"
#include "../utils/Debug.h"


// CSplashScreen dialog

IMPLEMENT_DYNAMIC(CSplashScreen, CDialog)

CSplashScreen::CSplashScreen(CString& csLicenseeText, CWnd* pParent /*=NULL*/)
	: CDialog(IDD_SPLASHWINDOW, pParent)
{
	m_csLicenseeText = csLicenseeText;
	m_lRefCount = 0;
	AddRef();
}

CSplashScreen::~CSplashScreen()
{
}

BOOL CSplashScreen::OnInitDialog() {
	CDialog::OnInitDialog();
	m_stLicensee.SetWindowText(m_csLicenseeText);
	return FALSE; // no focus given to control in dialog
}

void CSplashScreen::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LICENSETEXT, m_stLicensee);
	DDX_Control(pDX, IDC_PROGRESS1, m_prProgress);
}


BEGIN_MESSAGE_MAP(CSplashScreen, CDialog)
	ON_WM_CTLCOLOR()
END_MESSAGE_MAP()


// CSplashScreen message handlers

HBRUSH CSplashScreen::OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor)
{
	HBRUSH hbr = CDialog::OnCtlColor(pDC, pWnd, nCtlColor);
    if (nCtlColor == CTLCOLOR_STATIC && pWnd->GetDlgCtrlID() == IDC_LICENSETEXT)
	{
		COLORREF white = RGB(255, 255, 255);
		pDC->SetTextColor(white);
		pDC->SetBkMode(TRANSPARENT);
		return static_cast<HBRUSH>(GetStockObject(NULL_BRUSH));
	}
	return hbr;
}


void CSplashScreen::Update(int iRegistered) {
	LOGTRACE("Update");
	m_prProgress.SetPos(iRegistered);
	HideIfSplashOpen();
}

//void CSplashScreen::Open(HWND hwndParent, HINSTANCE hInst) {
//	m_hwndParent = hwndParent;
//	//ExcelUtils::HookExcelWindow (hwndParent);
//
//	RECT rcClient;  // Client area of parent window.
//	int cyVScroll;  // Height of scroll bar arrow.
//	INITCOMMONCONTROLSEX init;
//	init.dwICC = ICC_PROGRESS_CLASS;
//	init.dwSize = sizeof(INITCOMMONCONTROLSEX);
//	InitCommonControlsEx(&init);
//
//	GetWindowRect(hwndParent, &rcClient);
//	LOGTRACE("Client Rect bottom=%d, left=%d, right=%d, top=%d", rcClient.bottom, rcClient.left, rcClient.right, rcClient.top);
//
//	cyVScroll = GetSystemMetrics(SM_CYVSCROLL);
//	int width = rcClient.right - rcClient.left;
//	m_hwndProgress = CreateWindowEx(0, PROGRESS_CLASS, (LPTSTR)NULL,
//		WS_POPUP/*CHILD*/ | /*WS_VISIBLE | */ WS_BORDER | PBS_SMOOTH | PBS_MARQUEE, rcClient.left + (width / 6),
//		((rcClient.top + rcClient.bottom) / 2) - cyVScroll,
//		(width * 2) / 3, cyVScroll * 2,
//		hwndParent, (HMENU)0, hInst, NULL);
//	HideIfSplashOpen();
//}
INT_PTR CSplashScreen::Open(HWND hwndParent) {
	LOGTRACE("Open");
	Create(IDD_SPLASHWINDOW, CWnd::FromHandle(hwndParent));
	SetMarquee();
	HideIfSplashOpen();
	return 0;
}

void CSplashScreen::Close() {
	LOGTRACE("Closed");
	DestroyWindow();
	//EndDialog(IDOK);
}

bool CSplashScreen::IsSplashOpen() {
	CWnd* parent = GetParent();
	HWND hwndParent = *parent;
	HWND hSplash = ::FindWindowExW(hwndParent, nullptr, L"MsoSplash", nullptr);
	return hSplash != nullptr;
}

void CSplashScreen::Show() {
	LOGTRACE("Show");
	ShowWindow(SW_SHOW);
}

void CSplashScreen::Hide() {
	LOGTRACE("Hide");
	ShowWindow(SW_HIDE);
}

void CSplashScreen::HideIfSplashOpen() {
	LOGTRACE("HideIfSplashOpen");
	if (IsSplashOpen()) {
		Hide();
	}
	else {
		Show();
	}
}
void CSplashScreen::SetMax(int iMax) {
	m_prProgress.SetRange32(0, iMax);
}

void CSplashScreen::SetStep(int iStep) {
	m_prProgress.SetStep(iStep);
}

void CSplashScreen::Increment() {
	LOGTRACE("Increment");
	m_prProgress.StepIt();
	HideIfSplashOpen();
}

void CSplashScreen::SetMarquee() {
	m_prProgress.SetMarquee(TRUE, 50); // number is millis between updates: 50 ~ 20Hz
	m_prProgress.StepIt();
	HideIfSplashOpen();
}

ULONG CSplashScreen::AddRef() {
	return InterlockedIncrement(&m_lRefCount);
}

ULONG CSplashScreen::Release() {
	ULONG lResult = InterlockedDecrement(&m_lRefCount);
	if (!lResult) {
		delete this;
	}
	return lResult;
}
