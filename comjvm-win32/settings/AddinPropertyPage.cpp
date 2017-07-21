/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

// AddinPropertyPage.cpp : implementation file
//

#include "stdafx.h"
#include <comdef.h>
#include "AddinPropertyPage.h"
#include "afxdialogex.h"
#include <vector>
#include "AvailableJvms.h"
#include "utils/Debug.h"

// CAddinPropertyPage dialog

const LPCTSTR JAVASOFT = TEXT ("JavaSoft");

IMPLEMENT_DYNAMIC(CAddinPropertyPage, CPropertyPage)

CAddinPropertyPage::CAddinPropertyPage(CSettings *pSettings)
: CPropertyPage (CAddinPropertyPage::IDD), m_pSettings (pSettings)
{
	CPropertyPage::CPropertyPage ();
}

CAddinPropertyPage::~CAddinPropertyPage()
{
}

BOOL CAddinPropertyPage::OnInitDialog () {
	CPropertyPage::OnInitDialog ();
	if (m_pSettings->IsValid ()) {
		// TODO: Get rid of all these stupid string constants.
		_bstr_t bstrGCEnabled = m_pSettings->GetString (SECTION_ADDIN, TEXT ("GarbageCollection"));
		if (bstrGCEnabled.length () > 0) {
			LOGTRACE ("bstrGCEnabled.length() > 0");
			const _bstr_t ENABLED(TEXT("Enabled"));
			if (bstrGCEnabled == ENABLED) {
				m_bGarbageCollection.SetCheck (BST_CHECKED);
				LOGTRACE ("bstrGCEnabled was %s, ENABLED is %s, they were equal", (wchar_t *)bstrGCEnabled, (wchar_t *)ENABLED);
			} else {
				m_bGarbageCollection.SetCheck (BST_UNCHECKED);
				LOGTRACE ("bstrGCEnabled was %s, ENABLED is %s, they were NOT equal", (wchar_t *)bstrGCEnabled, (wchar_t *)ENABLED);
			}
		} else {
			LOGTRACE ("bstrGCEnabled.length() <= 0");
		}
		_bstr_t bstrShowToolbar = m_pSettings->GetString(SECTION_ADDIN, TEXT("ShowToolbar"));
		if (bstrShowToolbar.length () > 0) {
			LOGTRACE("bstrShowToolbar.length() > 0");
			const _bstr_t ENABLED(TEXT("Enabled"));
			if (bstrShowToolbar == ENABLED) {
				m_cbShowToolbar.SetCheck (BST_CHECKED);
			} else {
				m_cbShowToolbar.SetCheck(BST_UNCHECKED);
			}
		} else {
			LOGTRACE("bstrShowToolbar.length() <= 0, switching toolbar on");
			m_cbShowToolbar.SetCheck(BST_CHECKED);
		}
		_bstr_t bstrCheckForUpdates = m_pSettings->GetString(SECTION_ADDIN, KEY_UPGRADE_CHECK_REQUIRED);
		if (bstrCheckForUpdates.length() > 0) {
			LOGTRACE("bstrCheckForUpdates.length() > 0");
			const _bstr_t ENABLED(VALUE_UPGRADE_CHECK_REQUIRED_YES.c_str());
			if (bstrCheckForUpdates == ENABLED) {
				m_cbUpdateCheck.SetCheck(BST_CHECKED);
			} else {
				m_cbUpdateCheck.SetCheck(BST_UNCHECKED);
			}
		} else {
			// TODO: probably should take into account VALUE_UPGRADE_CHECK_REQUIRED_DEFAULT
			m_cbShowToolbar.SetCheck(BST_CHECKED);
		}
		_bstr_t bstrCppLogTarget = m_pSettings->GetString(SECTION_ADDIN, TEXT("LogTarget"));
		const _bstr_t FILE(TEXT("File"));
		if (bstrCppLogTarget.length() > 0) {
			if (bstrCppLogTarget == FILE) {
				m_rdLogFileRadio.SetCheck(BST_CHECKED);
				m_rdWinDebugRadio.SetCheck(BST_UNCHECKED);
			} else /* bstrCppLogTarget == WINDEBUG */ {
				m_rdLogFileRadio.SetCheck(BST_UNCHECKED);
				m_rdWinDebugRadio.SetCheck(BST_CHECKED);
			}
		} else { // no entry, default to WinDebug
			m_rdLogFileRadio.SetCheck(BST_UNCHECKED);
			m_rdWinDebugRadio.SetCheck(BST_CHECKED);
		}
		_bstr_t bstrCppLogLevel = m_pSettings->GetString(SECTION_ADDIN, TEXT("LogLevel"));
		const _bstr_t _ERROR(TEXT("ERROR"));
		const _bstr_t _TRACE(TEXT("TRACE"));
		const _bstr_t _NONE(TEXT("NONE"));
		if (bstrCppLogLevel.length() > 0) {
			m_cbCppLogLevel.SelectString(0, bstrCppLogLevel);
		} else {
			m_cbCppLogLevel.SelectString(0, TEXT("ERROR")); // no entry, default to ERROR
		}

	}
    return TRUE;
}

void CAddinPropertyPage::OnOK () {
	CPropertyPage::OnOK ();
	if (m_pSettings->IsValid ()) {
		if (m_bGarbageCollection.GetCheck () == BST_CHECKED) {
			m_pSettings->PutString (SECTION_ADDIN, TEXT ("GarbageCollection"), TEXT ("Enabled"));
		} else {
			m_pSettings->PutString (SECTION_ADDIN, TEXT ("GarbageCollection"), TEXT ("Disabled"));
		}
		if (m_cbShowToolbar.GetCheck() == BST_CHECKED) {
			m_pSettings->PutString (SECTION_ADDIN, TEXT("ShowToolbar"), TEXT("Enabled"));
		} else {
			m_pSettings->PutString (SECTION_ADDIN, TEXT("ShowToolbar"), TEXT("Disabled"));
		}
		if (m_cbUpdateCheck.GetCheck() == BST_CHECKED) {
			m_pSettings->PutString(SECTION_ADDIN, KEY_UPGRADE_CHECK_REQUIRED, VALUE_UPGRADE_CHECK_REQUIRED_YES);
		} else {
			m_pSettings->PutString(SECTION_ADDIN, KEY_UPGRADE_CHECK_REQUIRED, VALUE_UPGRADE_CHECK_REQUIRED_NO);
		}
		CString csLogLevel;
		m_cbCppLogLevel.GetLBText(m_cbCppLogLevel.GetCurSel(), csLogLevel);
		m_pSettings->PutString(SECTION_ADDIN, TEXT("LogLevel"), csLogLevel.GetBuffer());
		if (m_rdLogFileRadio.GetCheck() == BST_CHECKED) {
			m_pSettings->PutString(SECTION_ADDIN, TEXT("LogTarget"), TEXT("File"));
		} else {
			m_pSettings->PutString(SECTION_ADDIN, TEXT("LogTarget"), TEXT("WinDebug"));
		}
	}
}

void CAddinPropertyPage::DoDataExchange(CDataExchange* pDX)
{
	CPropertyPage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK_GARBAGE_COLLECTION, m_bGarbageCollection);
	DDX_Control(pDX, IDC_CHECK_HEAP_IN_WORKSHEET, m_cbSaveHeap);
	DDX_Control(pDX, IDC_SHOWTOOLBARCHECK, m_cbShowToolbar);
	DDX_Control(pDX, IDC_LOGLEVELCOMBO, m_cbCppLogLevel);
	DDX_Control(pDX, IDC_LOGFILERADIO, m_rdLogFileRadio);
	DDX_Control(pDX, IDC_WINDEBUGRADIO, m_rdWinDebugRadio);
	DDX_Control(pDX, IDC_UPDATECHECK, m_cbUpdateCheck);
}

BEGIN_MESSAGE_MAP(CAddinPropertyPage, CPropertyPage)
END_MESSAGE_MAP()

