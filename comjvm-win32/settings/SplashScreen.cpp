/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

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
	InitializeCriticalSection(&m_cs);
	EnterCriticalSection(&m_cs);
	m_csLicenseeText = csLicenseeText;
	m_lRefCount = 0;
	AddRef();
	m_state = CREATED;
	LeaveCriticalSection(&m_cs);
}

CSplashScreen::~CSplashScreen()
{
}

BOOL CSplashScreen::OnInitDialog() {
	CDialog::OnInitDialog();
	m_stLicensee.SetWindowText(m_csLicenseeText);
	SetWindowTheme(m_prProgress.GetSafeHwnd(), L" ", NULL);
	// the next two might not be necessary because of handling CtlColor in BlueProgress
	COLORREF clrBar = RGB(43, 87, 151); // the bar color
	m_prProgress.SendMessage(PBM_SETBARCOLOR, 0, (LPARAM)clrBar);
	m_prProgress.SetBarColor(RGB(43, 87, 151));
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
	//m_prProgress.SetPos(iRegistered);
	HideIfSplashOpen();
}

INT_PTR CSplashScreen::Open(HWND hwndParent) {
	EnterCriticalSection(&m_cs);
	LOGTRACE("Open");
	Create(IDD_SPLASHWINDOW, CWnd::FromHandle(hwndParent));
	SetMarquee();
	HideIfSplashOpen();
	LeaveCriticalSection(&m_cs);
	return 0;
}

void CSplashScreen::Close() {
	EnterCriticalSection(&m_cs);
	if (m_state == OPEN || m_state == HIDDEN) {
		LOGTRACE("Closed");
		DestroyWindow();
		m_state = CLOSED;
	}
	LeaveCriticalSection(&m_cs);
}

void CSplashScreen::CloseMT() {
	EnterCriticalSection(&m_cs);
	LOGTRACE("Posting WM_CLOSE message");
	// Only allow DestroyWindow to be called once as not sure if it's defined to be called multiple times.
	if (m_state == OPEN) {
		PostMessage(WM_CLOSE, 0, 0);
		m_state = CLOSED;
	}
	LeaveCriticalSection(&m_cs);
}

bool CSplashScreen::IsSplashOpen() {
	CWnd* parent = GetParent();
	HWND hwndParent = *parent;
	if (!::IsWindowEnabled(hwndParent)) { // check if another dialog is open too
		return true;
	}
	HWND hSplash = ::FindWindowExW(hwndParent, nullptr, L"MsoSplash", nullptr);
	return hSplash != nullptr;
}

void CSplashScreen::Show() {
	LOGTRACE("Show");
	m_state = OPEN;
	ShowWindow(SW_SHOW);
}

void CSplashScreen::Hide() {
	m_state = HIDDEN;
	LOGTRACE("Hide");
	ShowWindow(SW_HIDE);
}

void CSplashScreen::HideIfSplashOpen() {
	LOGTRACE("HideIfSplashOpen");
	if (IsSplashOpen()) {
		Hide();
	} else {
		Show();
	}
}
void CSplashScreen::SetMax(int iMax) {
	//m_prProgress.SetRange32(0, iMax);
}

void CSplashScreen::SetStep(int iStep) {
	//m_prProgress.SetStep(iStep);
}

void CSplashScreen::Increment() {
	EnterCriticalSection(&m_cs);
	LOGTRACE("Increment");
	//m_prProgress.StepIt();
	HideIfSplashOpen();
	LeaveCriticalSection(&m_cs);
}

void CSplashScreen::SetMarquee() {
	m_prProgress.SetMarquee(TRUE, 30); // number is millis between updates: 50 ~ 20Hz
	//m_prProgress.StepIt();
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
