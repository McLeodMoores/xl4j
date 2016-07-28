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
	
	DECLARE_MESSAGE_MAP()
public:

	void SetSelectedJvm (LPCTSTR szValue) {
		m_lbJvms.SelectString (-1, szValue);
	}
};
