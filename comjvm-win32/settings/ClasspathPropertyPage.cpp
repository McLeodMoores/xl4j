/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

// ClasspathPropertyPage.cpp : implementation file
//

#include "stdafx.h"
#include <comdef.h>
#include "ClasspathPropertyPage.h"
#include "afxdialogex.h"
#include "../utils/Debug.h"

// CClasspathPropertyPage dialog

IMPLEMENT_DYNAMIC(CClasspathPropertyPage, CPropertyPage)

CClasspathPropertyPage::CClasspathPropertyPage (CSettings *pSettings)
: CPropertyPage (CClasspathPropertyPage::IDD), m_pSettings (pSettings)
{
	CPropertyPage::CPropertyPage ();
}

CClasspathPropertyPage::~CClasspathPropertyPage()
{
}

BOOL CClasspathPropertyPage::OnInitDialog () {
	LOGTRACE ("Called");
	CPropertyPage::OnInitDialog ();
	if (m_pSettings->IsValid ()) {
		_bstr_t bstrClasspathEntry;
		long i = 0;
		do {
			bstrClasspathEntry = m_pSettings->GetString (TEXT ("Classpath"), i);
			if (bstrClasspathEntry.length () > 0) {
				m_lbClasspaths.AddString (bstrClasspathEntry);
				LOGTRACE ("Adding classpath into CListBox: %s", bstrClasspathEntry);
			}
			i++;
		} while (bstrClasspathEntry.length () != 0);
	}
	UpdateButtons ();
	return TRUE;
}

void CClasspathPropertyPage::OnOK () {
	LOGTRACE ("Called");
	CPropertyPage::OnOK ();
	if (m_pSettings->IsValid ()) {
		m_pSettings->DeleteKey (TEXT ("Classpath"));
		for (int i = 0; i < m_lbClasspaths.GetCount (); i++) {
			CString classpath;
			m_lbClasspaths.GetText (i, classpath);
			m_pSettings->PutString (TEXT("Classpath"), i, classpath.GetBuffer());
			LOGTRACE ("Writing %s to classpath position %d", classpath, i);
		}
	}
}

void CClasspathPropertyPage::DoDataExchange(CDataExchange* pDX)
{
	CPropertyPage::DoDataExchange (pDX);
	DDX_Control (pDX, IDC_LIST_CLASSPATHS, m_lbClasspaths);
	DDX_Control (pDX, IDC_BUTTON_ADD, m_bAdd);
	DDX_Control (pDX, IDC_BUTTON_REMOVE, m_bRemove);
	DDX_Control (pDX, IDC_BUTTON_UP, m_bUp);
	DDX_Control (pDX, IDC_BUTTON_DOWN, m_bDown);
}


BEGIN_MESSAGE_MAP(CClasspathPropertyPage, CPropertyPage)
	ON_BN_CLICKED (IDC_BUTTON_ADD, &CClasspathPropertyPage::OnBnClickedButtonAdd)
	ON_BN_CLICKED (IDC_BUTTON_REMOVE, &CClasspathPropertyPage::OnBnClickedButtonRemove)
	ON_BN_CLICKED (IDC_BUTTON_UP, &CClasspathPropertyPage::OnBnClickedButtonUp)
	ON_BN_CLICKED (IDC_BUTTON_DOWN, &CClasspathPropertyPage::OnBnClickedButtonDown)
	ON_LBN_SELCHANGE (IDC_LIST_CLASSPATHS, &CClasspathPropertyPage::OnLbnSelchangeListClasspaths)
	ON_BN_CLICKED (IDC_BUTTON_ADD_FOLDER, &CClasspathPropertyPage::OnBnClickedButtonAddFolder)
END_MESSAGE_MAP ()


// CClasspathPropertyPage message handlers


void CClasspathPropertyPage::OnBnClickedButtonAdd () {
	CFileDialog fileDialog (TRUE, _T("jar"), _T("*.jar"), OFN_PATHMUSTEXIST | OFN_ALLOWMULTISELECT, _T("Java Archives (*.jar)|*.jar|All Files (*.*)|*.*||"));
	if (fileDialog.DoModal () == IDOK) {
		POSITION pos = fileDialog.GetStartPosition ();
		if (m_lbClasspaths.GetCurSel () != LB_ERR) {
			while (pos != NULL) {
				CString nextPathName = fileDialog.GetNextPathName (pos);
				if (m_lbClasspaths.FindStringExact (0, nextPathName) == LB_ERR) {
					m_lbClasspaths.InsertString (m_lbClasspaths.GetCurSel (), nextPathName);
				}
			}
		} else {
			while (pos != NULL) {
				CString nextPathName = fileDialog.GetNextPathName (pos);
				if (m_lbClasspaths.FindStringExact (0, nextPathName) == LB_ERR) {
					m_lbClasspaths.AddString (nextPathName);
				}
			}
		}
	}
	UpdateButtons ();
}

void CClasspathPropertyPage::OnBnClickedButtonAddFolder () {
	CFolderPickerDialog folderDialog (NULL, OFN_PATHMUSTEXIST | OFN_ALLOWMULTISELECT, this, sizeof OPENFILENAME, 0);
	if (folderDialog.DoModal () == IDOK) {
		POSITION pos = folderDialog.GetStartPosition ();
		if (m_lbClasspaths.GetCurSel () != LB_ERR) {
			while (pos != NULL) {
				CString nextPathName = folderDialog.GetNextPathName (pos);
				if (m_lbClasspaths.FindStringExact (0, nextPathName) == LB_ERR) {
					m_lbClasspaths.InsertString (m_lbClasspaths.GetCurSel (), nextPathName);
				}
			}
		} else {
			while (pos != NULL) {
				CString nextPathName = folderDialog.GetNextPathName (pos);
				if (m_lbClasspaths.FindStringExact (0, nextPathName) == LB_ERR) {
					m_lbClasspaths.AddString (nextPathName);
				}
			}
		}
	}
	UpdateButtons ();
}


void CClasspathPropertyPage::OnBnClickedButtonRemove () {
	int index = m_lbClasspaths.GetCurSel ();
	if (index != LB_ERR) {
		m_lbClasspaths.DeleteString (index);
		int postDeleteCount = m_lbClasspaths.GetCount ();
		if (postDeleteCount > index) { // if we've deleted an item other than the last item, re-highlight the same index
			m_lbClasspaths.SetCurSel (index);
		} else if (postDeleteCount > 0) { // if we've deleted the last item, highlight the new last item
			m_lbClasspaths.SetCurSel (postDeleteCount - 1);
		}
	}
	UpdateButtons ();
}

void CClasspathPropertyPage::OnBnClickedButtonUp () {
	int index = m_lbClasspaths.GetCurSel ();
	if (index != LB_ERR) {
		if (index > 0) { // we're not already at the top and we also know there's at least one other item
			int len = m_lbClasspaths.GetTextLen (index);
			CString item;
			m_lbClasspaths.GetText (index, item);
			m_lbClasspaths.DeleteString (index);
			m_lbClasspaths.InsertString (index - 1, item);
			m_lbClasspaths.SetCurSel (index - 1); // setting selection allows user to move item several places without reselecting.
		}
	}
	UpdateButtons ();
}


void CClasspathPropertyPage::OnBnClickedButtonDown () {
	int index = m_lbClasspaths.GetCurSel ();
	if (index != LB_ERR) {
		if (index < m_lbClasspaths.GetCount() - 1) { // we're not already at the bottom and we also know there's at least one other item
			int len = m_lbClasspaths.GetTextLen (index);
			CString item;
			m_lbClasspaths.GetText (index, item);
			m_lbClasspaths.DeleteString (index);
			m_lbClasspaths.InsertString (index + 1, item);
			m_lbClasspaths.SetCurSel (index + 1); // setting selection allows user to move item several places without reselecting.
		}
	}
	UpdateButtons ();
}


void CClasspathPropertyPage::OnLbnSelchangeListClasspaths () {
	UpdateButtons ();
}

void CClasspathPropertyPage::UpdateButtons () {
	int index = m_lbClasspaths.GetCurSel ();
	int size = m_lbClasspaths.GetCount ();
	if (index == LB_ERR) {
		m_bRemove.EnableWindow (FALSE);
		m_bUp.EnableWindow (FALSE);
		m_bDown.EnableWindow (FALSE);
	} else {
		m_bRemove.EnableWindow (TRUE);
		if (size == 1) { // only one item, can't move it up or down, but can remove it
			m_bUp.EnableWindow (FALSE);
			m_bDown.EnableWindow (FALSE);
		} else if (index == 0) { // more than one item, first selected, can't move it up, but can down and remove
			m_bUp.EnableWindow (FALSE);
			m_bDown.EnableWindow (TRUE);
		} else if (index == size - 1) { // more than one item, last selected, can't move it down, but can up and remove
			m_bUp.EnableWindow (TRUE);
			m_bDown.EnableWindow (FALSE);
		} else { // in middle of list, can move up down and remove.
			m_bUp.EnableWindow (TRUE);
			m_bDown.EnableWindow (TRUE);
		}
	}
}


