#pragma once
#include "Resource.h"
#include "afxwin.h"

// CClasspathPropertyPage dialog

class CClasspathPropertyPage : public CPropertyPage
{
	DECLARE_DYNAMIC(CClasspathPropertyPage)

public:
	CClasspathPropertyPage();
	virtual ~CClasspathPropertyPage();

// Dialog Data
	enum { IDD = IDD_PROPPAGE_CLASSPATH };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	// Control to hold list of classpath entries
	CListBox m_lbClasspaths;
	// Control to hold button to add entries to classpath
	CButton m_bAdd;
	// Control to hold button that removes classpath entries
	CButton m_bRemove;
	// Button control to move selected entry up the list
	CButton m_bUp;
	// Button to move selected classpath entry down the list
	CButton m_bDown;
};
