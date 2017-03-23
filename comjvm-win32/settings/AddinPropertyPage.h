/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once
#include <windef.h>
#include "Resource.h"
#include "afxwin.h"
#include "../core/Settings.h"

// CAddinPropertyPage dialog

class CAddinPropertyPage : public CPropertyPage
{
	DECLARE_DYNAMIC(CAddinPropertyPage)

	CSettings *m_pSettings;
public:
	CAddinPropertyPage (CSettings *pSettings);
	virtual ~CAddinPropertyPage();

// Dialog Data
	enum { IDD = IDD_PROPPAGE_ADDIN };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	virtual BOOL OnInitDialog ();
	virtual void OnOK ();

	// The listbox of JVMs available
	CListBox m_lbJvms;
	// Control to determine if GC is enabled
	CButton m_bGarbageCollection;
	// Control to determine whether to save heap in the worksheet file
	CButton m_cbSaveHeap;
	// Control to determine if check for updates is enabled
	CButton m_cbUpdateEnabled;
	DECLARE_MESSAGE_MAP()
public:

	void SetSelectedJvm (LPCTSTR szValue) {
		m_lbJvms.SelectString (-1, szValue);
	}
	// Checkbox used to enable/disable whether or not a toolbar is shown.
	CButton m_cbShowToolbar;
	// Log Level for C++ components
	CComboBox m_cbCppLogLevel;
	// Radio button for choosing a file for C++ logs
	CButton m_rdLogFileRadio;
	// Radio button for selecting window debugging output for C++ debugging.
	CButton m_rdWinDebugRadio;
};
