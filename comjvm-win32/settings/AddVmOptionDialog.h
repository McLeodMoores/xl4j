#pragma once
#include <windef.h>
#include "Resource.h"
#include "afxwin.h"
#include "../core/Settings.h"

// CAddVmOptionDialog dialog

class CAddVmOptionDialog : public CDialogEx
{
	DECLARE_DYNAMIC(CAddVmOptionDialog)

public:
	CAddVmOptionDialog(CString &rString, CWnd* pParent = NULL);   // standard constructor
	virtual ~CAddVmOptionDialog();

// Dialog Data
	enum { IDD = IDD_DIALOG_ADDVMOPTION };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	virtual void OnOK ();

	CString &m_rString;
	DECLARE_MESSAGE_MAP()
public:
	// The edit box for editing the value to be added
	CEdit m_eValue;
};
