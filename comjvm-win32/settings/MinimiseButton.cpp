// MinimiseButton.cpp : implementation file
// This is for a transparent minimise button drawn using GDI+ primitives with roll-over and click colour change.
// It is used on the splash screen.

#include "stdafx.h"
#include "Resource.h"
#include "MinimiseButton.h"
#include <objidl.h>
#include <gdiplus.h>
#include "utils/Debug.h"
using namespace Gdiplus;
#pragma comment (lib,"Gdiplus.lib")

// CMinimiseButton

IMPLEMENT_DYNAMIC(CMinimiseButton, CButton)

CMinimiseButton::CMinimiseButton() {
}

CMinimiseButton::~CMinimiseButton() {
}


BEGIN_MESSAGE_MAP(CMinimiseButton, CButton)
	ON_MESSAGE(WM_MOUSELEAVE, OnMouseLeave)
	ON_WM_MOUSEMOVE()
END_MESSAGE_MAP()

LRESULT CMinimiseButton::OnMouseLeave(WPARAM wParam, LPARAM lParam) {
	LOGTRACE("Leave");
	m_bMouseOver = false;
	Invalidate();
	UpdateWindow();
	return 0;
}

// CMinimiseButton message handlers

void CMinimiseButton::DrawItem(LPDRAWITEMSTRUCT lpDrawItemStruct) {
	ASSERT(lpDrawItemStruct->CtlType == ODT_BUTTON);
	Graphics gr(lpDrawItemStruct->hDC);
	gr.Clear(Color::Transparent);
	const REAL DEFAULT_DPI = 96.;
	double offsetX = (gr.GetDpiX() / DEFAULT_DPI) * 2.;
	double offsetY = (gr.GetDpiY() / DEFAULT_DPI) * 7.;
	double limitX = (gr.GetDpiX() / DEFAULT_DPI) * 12.;
	Pen pen(Color::White);
	if (m_bMouseOver) {
		pen.SetColor(Color::LightBlue);
	} else if (lpDrawItemStruct->itemState & ODS_SELECTED) {
		pen.SetColor(Color::Black);
	}
	gr.DrawLine(&pen, Point(offsetX, offsetY), Point(limitX, offsetY));
}


void CMinimiseButton::OnMouseMove(UINT nFlags, CPoint point) {
	if (!m_bMouseOver) {
		TRACKMOUSEEVENT tme;
		tme.cbSize = sizeof(TRACKMOUSEEVENT);
		tme.dwFlags = TME_LEAVE;
		tme.hwndTrack = this->m_hWnd;

		if (::_TrackMouseEvent(&tme)) {
			m_bMouseOver = TRUE;
			Invalidate();
			UpdateWindow();
		}
	} else {
		/*Invalidate();
		UpdateWindow();*/
	}
	CButton::OnMouseMove(nFlags, point);
}
