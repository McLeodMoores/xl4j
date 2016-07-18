#include "stdafx.h"
#include "Progress.h"
#include "ExcelUtils.h"

#pragma comment(linker,"\"/manifestdependency:type='win32' \
name='Microsoft.Windows.Common-Controls' version='6.0.0.0' \
processorArchitecture='*' publicKeyToken='6595b64144ccf1df' language='*'\"")

Progress::Progress () {
	m_lRefCount = 0;
	AddRef ();
}

Progress::~Progress () {
	LOGTRACE ("Destructor");
	Destroy();
}

void Progress::Update (int iRegistered) {
	SendMessage (m_hwndProgress, PBM_SETPOS, iRegistered, 0);
}

void Progress::Destroy () {
	DestroyWindow (m_hwndProgress);
	//ExcelUtils::UnhookExcelWindow (m_hwndParent);
}


void Progress::Open (HWND hwndParent, HINSTANCE hInst) {
	m_hwndParent = hwndParent;
	//ExcelUtils::HookExcelWindow (hwndParent);

	RECT rcClient;  // Client area of parent window.
	int cyVScroll;  // Height of scroll bar arrow.
	INITCOMMONCONTROLSEX init;
	init.dwICC = ICC_PROGRESS_CLASS;
	init.dwSize = sizeof (INITCOMMONCONTROLSEX);
	InitCommonControlsEx (&init);

	GetWindowRect (hwndParent, &rcClient);
	LOGTRACE ("Client Rect bottom=%d, left=%d, right=%d, top=%d", rcClient.bottom, rcClient.left, rcClient.right, rcClient.top);

	cyVScroll = GetSystemMetrics (SM_CYVSCROLL);
	int width = rcClient.right - rcClient.left;
	m_hwndProgress = CreateWindowEx (0, PROGRESS_CLASS, (LPTSTR)NULL,
		WS_POPUP/*CHILD*/ | WS_VISIBLE | WS_BORDER | PBS_SMOOTH | PBS_MARQUEE, rcClient.left  + (width / 6),
		((rcClient.top + rcClient.bottom) / 2) - cyVScroll,
		(width * 2) / 3, cyVScroll * 2,
		hwndParent, (HMENU)0, hInst, NULL);
}

void Progress::SetMax (int iMax) {
	SendMessage (m_hwndProgress, PBM_SETRANGE, 0, MAKELPARAM (0, iMax));
}

void Progress::SetStep (int iStep) {
	SendMessage (m_hwndProgress, PBM_SETSTEP, (WPARAM)iStep, 0);
}

void Progress::Increment () {
	SendMessage (m_hwndProgress, PBM_STEPIT, 0, MAKELPARAM (0, 0));
}

void Progress::SetMarquee () {
	SendMessage (m_hwndProgress, PBM_SETMARQUEE, 0, 0);
}

ULONG Progress::AddRef () {
	LOGTRACE ("AddRef");
	return InterlockedIncrement (&m_lRefCount);
}

ULONG Progress::Release () {
	LOGTRACE ("Release");
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}