#pragma once


// CMinimiseButton

class CMinimiseButton : public CButton {
	DECLARE_DYNAMIC(CMinimiseButton)

public:
	CMinimiseButton();
	virtual ~CMinimiseButton();

protected:
	DECLARE_MESSAGE_MAP()
	bool m_bMouseOver;
public:
	virtual void DrawItem(LPDRAWITEMSTRUCT /*lpDrawItemStruct*/);
	afx_msg LRESULT OnMouseLeave(WPARAM, LPARAM);
	afx_msg void OnMouseMove(UINT nFlags, CPoint point);
};