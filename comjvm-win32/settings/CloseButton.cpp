// CloseButton.cpp : implementation file
// This is for a transparent close button drawn using GDI+ primitives with roll-over and click colour change.
// It is used on the splash screen.

#include "stdafx.h"
#include "Resource.h"
#include "CloseButton.h"
#include <objidl.h>
#include <gdiplus.h>
#include "utils/Debug.h"
using namespace Gdiplus;
#pragma comment (lib,"Gdiplus.lib")

// CCloseButton

IMPLEMENT_DYNAMIC(CCloseButton, CButton)

CCloseButton::CCloseButton()
{
}

CCloseButton::~CCloseButton()
{
}


BEGIN_MESSAGE_MAP(CCloseButton, CButton)
	ON_MESSAGE(WM_MOUSELEAVE, OnMouseLeave)
	ON_WM_MOUSEMOVE()
END_MESSAGE_MAP()

LRESULT CCloseButton::OnMouseLeave(WPARAM wParam, LPARAM lParam) {
	LOGTRACE("Leave");
	m_bMouseOver = false;
	Invalidate();
	UpdateWindow();
	return 0;
}

// CCloseButton message handlers

void CCloseButton::DrawItem(LPDRAWITEMSTRUCT lpDrawItemStruct) {
	ASSERT(lpDrawItemStruct->CtlType == ODT_BUTTON);
	Graphics gr(lpDrawItemStruct->hDC);
	gr.Clear(Color::Transparent);
	const REAL DEFAULT_DPI = 96.;
	double offsetX = (gr.GetDpiX() / DEFAULT_DPI) * 2.;
	double offsetY = (gr.GetDpiY() / DEFAULT_DPI) * 2.;
	double limitX = (gr.GetDpiX() / DEFAULT_DPI) * 12.;
	double limitY = (gr.GetDpiY() / DEFAULT_DPI) * 12.;
	Pen pen(Color::White);
	if (m_bMouseOver) {
		pen.SetColor(Color::LightBlue);
	} else if (lpDrawItemStruct->itemState & ODS_SELECTED) {
		pen.SetColor(Color::Black);
	}
	gr.DrawLine(&pen, Point(offsetX, offsetY), Point(limitX, limitY));
	gr.DrawLine(&pen, Point(offsetX, limitY), Point(limitX, offsetY));
}


void CCloseButton::OnMouseMove(UINT nFlags, CPoint point) {
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
