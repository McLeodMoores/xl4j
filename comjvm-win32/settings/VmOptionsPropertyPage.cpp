/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

// VmOptionsPropertyPage.cpp : implementation file
//

#include "stdafx.h"
#include "VmOptionsPropertyPage.h"
#include "afxdialogex.h"
#include "Resource.h"
#include "AddVmOptionDialog.h"
#include "EditVmOptionDialog.h"
#include "../utils/Debug.h"

// CVmOptionsPropertyPage dialog

IMPLEMENT_DYNAMIC(CVmOptionsPropertyPage, CPropertyPage)

CVmOptionsPropertyPage::CVmOptionsPropertyPage(CSettings *pSettings)
: CPropertyPage (CVmOptionsPropertyPage::IDD), m_pSettings (pSettings)
{

}

CVmOptionsPropertyPage::~CVmOptionsPropertyPage()
{
}

void CVmOptionsPropertyPage::DoDataExchange(CDataExchange* pDX)
{
	CPropertyPage::DoDataExchange (pDX);
	DDX_Control (pDX, IDC_CHECK_DEBUG, m_cbDebug);
	DDX_Control (pDX, IDC_CHECK_CHECK_JNI, m_cbCheckJni);
	DDX_Control (pDX, IDC_CHECK_MAX_HEAP, m_cbMaxHeap);
	DDX_Control (pDX, IDC_EDIT_MAX_HEAP, m_eMaxHeap);
	DDX_Control (pDX, IDC_CHECK_REMOTE_DEBUG, m_cbRemoteDebugging);
	DDX_Control (pDX, IDC_CHECK_LOGBACK, m_cbLogback);
	DDX_Control (pDX, IDC_COMBO_LOGBACK_LEVEL, m_cmLogbackLevel);
	DDX_Control (pDX, IDC_LIST_VM_OPTIONS, m_lbCustomOptions);
	DDX_Control (pDX, IDC_BUTTON_CUSTOM_ADD, m_bAdd);
	DDX_Control (pDX, IDC_BUTTON_CUSTOM_REMOVE, m_bRemove);
	DDX_Control (pDX, IDC_BUTTON_UP, m_bMoveUp);
	DDX_Control (pDX, IDC_BUTTON_DOWN, m_bMoveDown);
	DDX_Control (pDX, IDC_BUTTON_CUSTOM_EDIT, m_bEdit);
}


BEGIN_MESSAGE_MAP(CVmOptionsPropertyPage, CPropertyPage)
	ON_BN_CLICKED (IDC_BUTTON_CUSTOM_ADD, &CVmOptionsPropertyPage::OnBnClickedButtonCustomAdd)
	ON_BN_CLICKED (IDC_BUTTON_CUSTOM_EDIT, &CVmOptionsPropertyPage::OnBnClickedButtonCustomEdit)
	ON_BN_CLICKED (IDC_BUTTON_UP, &CVmOptionsPropertyPage::OnBnClickedButtonUp)
	ON_BN_CLICKED (IDC_BUTTON_DOWN, &CVmOptionsPropertyPage::OnBnClickedButtonDown)
	ON_BN_CLICKED (IDC_BUTTON_CUSTOM_REMOVE, &CVmOptionsPropertyPage::OnBnClickedButtonCustomRemove)
	ON_LBN_SELCHANGE (IDC_LIST_VM_OPTIONS, &CVmOptionsPropertyPage::OnLbnSelchangeListVmOptions)
	ON_BN_CLICKED (IDC_CHECK_MAX_HEAP, &CVmOptionsPropertyPage::OnBnClickedCheckMaxHeap)
	ON_BN_CLICKED (IDC_CHECK_LOGBACK, &CVmOptionsPropertyPage::OnBnClickedCheckLogback)
END_MESSAGE_MAP ()

BOOL CVmOptionsPropertyPage::OnInitDialog () {
	LOGTRACE ("Called");
	CPropertyPage::OnInitDialog ();
	if (m_pSettings->IsValid ()) {
		_bstr_t bstrOption;
		long i = 0;
		do {
			bstrOption = m_pSettings->GetString (TEXT ("Options"), i);
			if (bstrOption.length () > 0) {
				m_lbCustomOptions.AddString (bstrOption);
				LOGTRACE ("Adding VM option into CListBox: %s", bstrOption);
			}
			i++;
		} while (bstrOption.length () != 0);
		const wchar_t *szAutoOptions = TEXT ("Auto Options");
		_bstr_t bstrDebug = m_pSettings->GetString (szAutoOptions, TEXT ("Debug"));
		m_cbDebug.SetCheck ((bstrDebug.length () > 0) ? BST_CHECKED : BST_UNCHECKED);
		_bstr_t bstrCheckJni = m_pSettings->GetString (szAutoOptions, TEXT ("CheckJNI"));
		m_cbCheckJni.SetCheck ((bstrCheckJni.length () > 0) ? BST_CHECKED : BST_UNCHECKED);
		_bstr_t bstrMaxHeap = m_pSettings->GetString (szAutoOptions, TEXT ("MaxHeap"));
		m_cbMaxHeap.SetCheck ((bstrMaxHeap.length () > 0) ? BST_CHECKED : BST_UNCHECKED);
		if (bstrMaxHeap.length () > 0) {
			m_eMaxHeap.SetWindowTextW (bstrMaxHeap);
			m_eMaxHeap.EnableWindow (TRUE);
		}
		_bstr_t bstrRemoteDebugging = m_pSettings->GetString (szAutoOptions, TEXT ("RemoteDebugging"));
		m_cbRemoteDebugging.SetCheck ((bstrRemoteDebugging.length () > 0) ? BST_CHECKED : BST_UNCHECKED);
		_bstr_t bstrLogback = m_pSettings->GetString (szAutoOptions, TEXT ("Logback"));
		m_cbLogback.SetCheck ((bstrLogback.length () > 0) ? BST_CHECKED : BST_UNCHECKED);
		if (bstrLogback.length () > 0) {
			m_cmLogbackLevel.SelectString (0, bstrLogback);
			m_cmLogbackLevel.EnableWindow (TRUE);
		}
	}
	UpdateButtons ();
	return TRUE;
}

void CVmOptionsPropertyPage::OnOK () {
	LOGTRACE ("Called");
	const int MAX_HEAP_CHARS = 32;
	CPropertyPage::OnOK ();
	if (m_pSettings->IsValid ()) {
		m_pSettings->DeleteKey (TEXT ("Options"));
		for (int i = 0; i < m_lbCustomOptions.GetCount (); i++) {
			CString option;
			m_lbCustomOptions.GetText (i, option);
			m_pSettings->PutString (TEXT ("Options"), i, option.GetBuffer ());
			LOGTRACE ("Writing %s to VM option position %d", option, i);
		}
		const wchar_t *szAutoOptions = TEXT ("Auto Options");
		if (m_cbDebug.GetCheck () == BST_CHECKED) {
			m_pSettings->PutString (szAutoOptions, TEXT ("Debug"), TEXT ("Enabled"));
		} else {
			m_pSettings->DeleteString (szAutoOptions, TEXT ("Debug"));
		}
		if (m_cbCheckJni.GetCheck () == BST_CHECKED) {
			m_pSettings->PutString (szAutoOptions, TEXT ("CheckJNI"), TEXT ("Enabled"));
		} else {
			m_pSettings->DeleteString (szAutoOptions, TEXT ("CheckJNI"));
		}
		// Max Heap
		CString maxHeap;
		m_eMaxHeap.GetLine (0, maxHeap.GetBuffer(MAX_HEAP_CHARS), MAX_HEAP_CHARS);
		maxHeap.Trim ();
		if ((maxHeap.GetLength () > 0) && (m_cbMaxHeap.GetCheck() == BST_CHECKED)) {
			m_pSettings->PutString (szAutoOptions, TEXT ("MaxHeap"), maxHeap.GetBuffer ());
		} else {
			m_pSettings->DeleteString (szAutoOptions, TEXT ("MaxHeap")); // delete entry.
		}
		// Remote Debugging
		if (m_cbRemoteDebugging.GetCheck () == BST_CHECKED) {
			m_pSettings->PutString (szAutoOptions, TEXT ("RemoteDebugging"), TEXT ("Enabled"));
		} else {
			m_pSettings->DeleteString (szAutoOptions, TEXT ("RemoteDebugging"));
		}
		// Logback
		CString logback;
		m_cmLogbackLevel.GetLBText (m_cmLogbackLevel.GetCurSel (), logback);
		if ((logback.GetLength () > 0) && (m_cbLogback.GetCheck () == BST_CHECKED)) {
			m_pSettings->PutString (szAutoOptions, TEXT ("Logback"), logback.GetBuffer ());
		} else {
			m_pSettings->DeleteString (szAutoOptions, TEXT ("Logback"));
		}
	}
}

// CVmOptionsPropertyPage message handlers

void CVmOptionsPropertyPage::OnBnClickedButtonCustomAdd () {
	CString value;
	CAddVmOptionDialog optionDialog (value);
	if (optionDialog.DoModal () == IDOK) {
		if (m_lbCustomOptions.GetCurSel () != LB_ERR) {
			m_lbCustomOptions.InsertString (m_lbCustomOptions.GetCurSel(), value);
		} else {
			m_lbCustomOptions.AddString (value);
		}
	}
	UpdateButtons ();
}

void CVmOptionsPropertyPage::OnBnClickedButtonCustomEdit () {
	CString value;
	int curr = m_lbCustomOptions.GetCurSel ();
	if (curr != LB_ERR) {
		m_lbCustomOptions.GetText (curr, value);
		CEditVmOptionDialog optionDialog (value);
		if (optionDialog.DoModal () == IDOK) {
			m_lbCustomOptions.DeleteString (curr);
			m_lbCustomOptions.InsertString (curr, value);
		}
	}
	UpdateButtons ();
}

void CVmOptionsPropertyPage::OnBnClickedButtonCustomRemove () {
	int index = m_lbCustomOptions.GetCurSel ();
	if (index != LB_ERR) {
		m_lbCustomOptions.DeleteString (index);
		int postDeleteCount = m_lbCustomOptions.GetCount ();
		if (postDeleteCount > index) { // if we've deleted an item other than the last item, re-highlight the same index
			m_lbCustomOptions.SetCurSel (index);
		} else if (postDeleteCount > 0) { // if we've deleted the last item, highlight the new last item
			m_lbCustomOptions.SetCurSel (postDeleteCount - 1);
		}
	}
	UpdateButtons ();
}

void CVmOptionsPropertyPage::OnBnClickedButtonUp () {
	int index = m_lbCustomOptions.GetCurSel ();
	if (index != LB_ERR) {
		if (index > 0) { // we're not already at the top and we also know there's at least one other item
			int len = m_lbCustomOptions.GetTextLen (index);
			CString item;
			m_lbCustomOptions.GetText (index, item);
			m_lbCustomOptions.DeleteString (index);
			m_lbCustomOptions.InsertString (index - 1, item);
			m_lbCustomOptions.SetCurSel (index - 1); // setting selection allows user to move item several places without reselecting.
		}
	}
	UpdateButtons ();
}


void CVmOptionsPropertyPage::OnBnClickedButtonDown () {
	int index = m_lbCustomOptions.GetCurSel ();
	if (index != LB_ERR) {
		if (index < m_lbCustomOptions.GetCount () - 1) { // we're not already at the bottom and we also know there's at least one other item
			int len = m_lbCustomOptions.GetTextLen (index);
			CString item;
			m_lbCustomOptions.GetText (index, item);
			m_lbCustomOptions.DeleteString (index);
			m_lbCustomOptions.InsertString (index + 1, item);
			m_lbCustomOptions.SetCurSel (index + 1); // setting selection allows user to move item several places without reselecting.
		}
	}
	UpdateButtons ();
}

void CVmOptionsPropertyPage::UpdateButtons () {
	int index = m_lbCustomOptions.GetCurSel ();
	int size = m_lbCustomOptions.GetCount ();
	if (index == LB_ERR) {
		m_bRemove.EnableWindow (FALSE);
		m_bEdit.EnableWindow (FALSE);
		m_bMoveUp.EnableWindow (FALSE);
		m_bMoveDown.EnableWindow (FALSE);
	} else {
		m_bRemove.EnableWindow (TRUE);
		m_bEdit.EnableWindow (TRUE);
		if (size == 1) { // only one item, can't move it up or down, but can remove it
			m_bMoveUp.EnableWindow (FALSE);
			m_bMoveDown.EnableWindow (FALSE);
		} else if (index == 0) { // more than one item, first selected, can't move it up, but can down and remove
			m_bMoveUp.EnableWindow (FALSE);
			m_bMoveDown.EnableWindow (TRUE);
		} else if (index == size - 1) { // more than one item, last selected, can't move it down, but can up and remove
			m_bMoveUp.EnableWindow (TRUE);
			m_bMoveDown.EnableWindow (FALSE);
		} else { // in middle of list, can move up down and remove.
			m_bMoveUp.EnableWindow (TRUE);
			m_bMoveDown.EnableWindow (TRUE);
		}
	}
}

void CVmOptionsPropertyPage::OnLbnSelchangeListVmOptions () {
	UpdateButtons ();
}

void CVmOptionsPropertyPage::OnBnClickedCheckMaxHeap () {
	m_eMaxHeap.EnableWindow (m_cbMaxHeap.GetCheck () == BST_CHECKED);
}

void CVmOptionsPropertyPage::OnBnClickedCheckLogback () {
	m_cmLogbackLevel.EnableWindow (m_cbLogback.GetCheck () == BST_CHECKED);
}
