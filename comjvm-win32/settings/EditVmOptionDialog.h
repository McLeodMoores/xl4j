#pragma once
#include <windef.h>
#include "Resource.h"
#include "afxwin.h"
#include "../core/Settings.h"

// CEditVmOptionDialog dialog

class CEditVmOptionDialog : public CDialogEx
{
	DECLARE_DYNAMIC(CEditVmOptionDialog)

public:
	CEditVmOptionDialog(CString &rString, CWnd* pParent = NULL);   // standard constructor
	virtual ~CEditVmOptionDialog();

// Dialog Data
	enum { IDD = IDD_DIALOG_EDITVMOPTION };

protected:
	CString &m_rString;

	virtual BOOL OnInitDialog ();
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	virtual void OnOK ();
	DECLARE_MESSAGE_MAP()
public:
	// Edit box holding value to edit
	CEdit m_eValue;
};
