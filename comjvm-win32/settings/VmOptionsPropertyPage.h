#pragma once
#include "Resource.h"
#include "afxwin.h"

#include "../core/Settings.h"

// CVmOptionsPropertyPage dialog

class CVmOptionsPropertyPage : public CPropertyPage
{
	DECLARE_DYNAMIC(CVmOptionsPropertyPage)

public:
	CVmOptionsPropertyPage(CSettings *pSettings);
	virtual ~CVmOptionsPropertyPage();

// Dialog Data
	enum { IDD = IDD_PROPPAGE_VM_OPTIONS };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	virtual BOOL OnInitDialog ();
	virtual void OnOK ();
	virtual void UpdateButtons ();
	CSettings *m_pSettings;
	DECLARE_MESSAGE_MAP()
public:
	// Checkbox to determine if debug flag enabled
	CButton m_cbDebug;
	// Checkbox to determine if JNI chacks are enabled
	CButton m_cbCheckJni;
	// Checkbox to determine if a maximum heap option should be specified
	CButton m_cbMaxHeap;
	// Edit control to determine the maximum heap space
	CEdit m_eMaxHeap;
	// Checkbox to determine if remote JSWP debugging on port 8000 is enabled
	CButton m_cbRemoteDebugging;
	// Checkbox to determine whether logback logging is enabled
	CButton m_cbLogback;
	// Combobox to determine logback level
	CComboBox m_cmLogbackLevel;
	// Listbox containing custom VM options
	CListBox m_lbCustomOptions;
	// Button to add new custom option
	CButton m_bAdd;
	// Button to remove currently selected VM option
	CButton m_bRemove;
	// Button to move item up list
	CButton m_bMoveUp;
	// Button to move item down list
	CButton m_bMoveDown;
	// Button to edit currently selected item
	CButton m_bEdit;
	afx_msg void OnBnClickedButtonCustomAdd ();
	

	
	afx_msg void OnBnClickedButtonCustomEdit ();
	afx_msg void OnBnClickedButtonUp ();
	afx_msg void OnBnClickedButtonDown ();
	afx_msg void OnBnClickedButtonCustomRemove ();
	afx_msg void OnLbnSelchangeListVmOptions ();
	afx_msg void OnBnClickedCheckMaxHeap ();
	afx_msg void OnBnClickedCheckLogback ();
};
