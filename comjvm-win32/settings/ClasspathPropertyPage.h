#pragma once
#include <windef.h>
#include "Resource.h"
#include "afxwin.h"
#include "../core/Settings.h"

// CClasspathPropertyPage dialog

class CClasspathPropertyPage : public CPropertyPage
{
	DECLARE_DYNAMIC(CClasspathPropertyPage)

	CSettings *m_pSettings;
public:
	CClasspathPropertyPage(CSettings *pSettings);
	virtual ~CClasspathPropertyPage();

// Dialog Data
	enum { IDD = IDD_PROPPAGE_CLASSPATH };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	virtual BOOL OnInitDialog ();
	virtual void OnOK ();

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

	DECLARE_MESSAGE_MAP ()
public:
	
	afx_msg void OnBnClickedButtonAdd ();
	afx_msg void OnBnClickedButtonRemove ();
	afx_msg void OnBnClickedButtonUp ();
	afx_msg void OnBnClickedButtonDown ();
	afx_msg void OnLbnSelchangeListClasspaths ();
	void UpdateButtons ();
	afx_msg void OnBnClickedButtonAddFolder ();
};
