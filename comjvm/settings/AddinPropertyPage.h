#pragma once
#include "Resource.h"
#include "afxwin.h"

// CAddinPropertyPage dialog

class CAddinPropertyPage : public CPropertyPage
{
	DECLARE_DYNAMIC(CAddinPropertyPage)

public:
	CAddinPropertyPage();
	virtual ~CAddinPropertyPage();

// Dialog Data
	enum { IDD = IDD_PROPPAGE_ADDIN };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	// The listbox of JVMs available
	CListBox m_lbJvms;
	// Control to determine if GC is enabled
	CButton m_bGarbageCollection;
	// Control to determine whether to save heap in the worksheet file
	CButton m_cbSaveHeap;
};
