#pragma once
#include "Resource.h"
#include "afxwin.h"

// CVmOptionsPropertyPage dialog

class CVmOptionsPropertyPage : public CPropertyPage
{
	DECLARE_DYNAMIC(CVmOptionsPropertyPage)

public:
	CVmOptionsPropertyPage();
	virtual ~CVmOptionsPropertyPage();

// Dialog Data
	enum { IDD = IDD_PROPPAGE_VM_OPTIONS };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

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
};
