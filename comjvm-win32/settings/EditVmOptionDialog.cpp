/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

// EditVmOptionDialog.cpp : implementation file
//

#include "stdafx.h"
#include "EditVmOptionDialog.h"
#include "afxdialogex.h"
#include "../utils/Debug.h"

// CEditVmOptionDialog dialog

IMPLEMENT_DYNAMIC(CEditVmOptionDialog, CDialogEx)

CEditVmOptionDialog::CEditVmOptionDialog(CString &rString, CWnd* pParent /*=NULL*/)
: CDialogEx (CEditVmOptionDialog::IDD, pParent), m_rString (rString)
{

}

CEditVmOptionDialog::~CEditVmOptionDialog()
{
}

BOOL CEditVmOptionDialog::OnInitDialog () {
	LOGTRACE ("Called");
	CDialog::OnInitDialog ();
	m_eValue.SetWindowTextW (m_rString);
	return TRUE;
}

void CEditVmOptionDialog::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange (pDX);
	DDX_Control (pDX, IDC_EDIT_VALUE, m_eValue);
}


BEGIN_MESSAGE_MAP(CEditVmOptionDialog, CDialogEx)
END_MESSAGE_MAP ()


// CEditVmOptionDialog message handlers
void CEditVmOptionDialog::OnOK () {
	CDialog::OnOK ();
	m_eValue.GetWindowTextW (m_rString);
}
