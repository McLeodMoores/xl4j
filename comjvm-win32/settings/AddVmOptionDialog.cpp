/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

// AddVmOptionDialog.cpp : implementation file
//

#include "stdafx.h"
#include "AddVmOptionDialog.h"
#include "afxdialogex.h"
#include "../utils/Debug.h"


// CAddVmOptionDialog dialog

IMPLEMENT_DYNAMIC (CAddVmOptionDialog, CDialogEx)

CAddVmOptionDialog::CAddVmOptionDialog (CString &rString, CWnd* pParent /*=NULL*/)
: CDialogEx (CAddVmOptionDialog::IDD, pParent), m_rString (rString)
{

}

CAddVmOptionDialog::~CAddVmOptionDialog()
{
}

void CAddVmOptionDialog::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange (pDX);
	DDX_Control (pDX, IDC_EDIT_VALUE, m_eValue);
}


BEGIN_MESSAGE_MAP(CAddVmOptionDialog, CDialogEx)
END_MESSAGE_MAP ()


// CAddVmOptionDialog message handlers

void CAddVmOptionDialog::OnOK () {
	CDialog::OnOK ();
	m_eValue.GetWindowTextW (m_rString);
}