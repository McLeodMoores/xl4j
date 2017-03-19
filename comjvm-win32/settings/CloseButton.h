#pragma once


// CCloseButton

class CCloseButton : public CButton
{
	DECLARE_DYNAMIC(CCloseButton)

public:
	CCloseButton();
	virtual ~CCloseButton();

protected:
	DECLARE_MESSAGE_MAP()
	bool m_bMouseOver;
public:
	virtual void DrawItem(LPDRAWITEMSTRUCT /*lpDrawItemStruct*/);
	afx_msg LRESULT OnMouseLeave(WPARAM, LPARAM);
	afx_msg void OnMouseMove(UINT nFlags, CPoint point);
	afx_msg void OnBnClickedClosebutton();
};


