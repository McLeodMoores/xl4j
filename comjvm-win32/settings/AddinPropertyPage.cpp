// AddinPropertyPage.cpp : implementation file
//

#include "stdafx.h"
#include <comdef.h>
#include "AddinPropertyPage.h"
#include "afxdialogex.h"
#include <vector>
#include "AvailableJvms.h"
#include "../utils/Debug.h"

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
	//std::vector<LPTSTR> versions;
	//CAvailableJvms *pJvms = new CAvailableJvms ();
	//HRESULT hr = pJvms->Search (JAVASOFT, versions);
	//if (FAILED (hr)) {
	//	_com_error err (hr);
	//	LOGERROR ("Problem looking for JVMs: %s", err.ErrorMessage ());
	//}
	//for (std::vector<LPTSTR>::iterator it = versions.begin (); it != versions.end (); ++it) {
	//	LOGTRACE ("Found JVM %s", *it);
	//	m_lbJvms.AddString (*it);
	//	free (*it);
	//}
	//LOGTRACE ("Finished scanning JVMs");
	if (m_pSettings->IsValid ()) {
		//_bstr_t bstrJvmVersion = m_pSettings->GetString (TEXT ("Jvm"), TEXT ("Version"));
		//if (bstrJvmVersion.length () > 0) {
		//	m_lbJvms.SelectString (-1, bstrJvmVersion);
		//}
		//LOGTRACE ("Getting Addin/GC");
		_bstr_t bstrGCEnabled = m_pSettings->GetString (TEXT ("Addin"), TEXT ("GarbageCollection"));
		//LOGTRACE ("Got it. %p", bstrGCEnabled.GetAddress ());
		if (bstrGCEnabled.length () > 0) {
			LOGTRACE ("bstrGCEnabled.length() > 0");
			const _bstr_t ENABLED(TEXT("Enabled"));
			if (bstrGCEnabled == ENABLED) {
				m_bGarbageCollection.SetCheck (BST_CHECKED);
				LOGTRACE ("bstrGCEnabled was %s, ENABLED is %s, they were equal", bstrGCEnabled, ENABLED);
			} else {
				m_bGarbageCollection.SetCheck (BST_UNCHECKED);
				LOGTRACE ("bstrGCEnabled was %s, ENABLED is %s, they were NOT equal", bstrGCEnabled, ENABLED);
			}
		} else {
			LOGTRACE ("bstrGCEnabled.length() <= 0");
		}
		_bstr_t bstrShowToolbar = m_pSettings->GetString(TEXT("Addin"), TEXT("ShowToolbar"));
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
		_bstr_t bstrLogViewerPath = m_pSettings->GetString(TEXT("Addin"), TEXT("LogViewer"));
		if (bstrLogViewerPath.length() > 0) {
			m_edLogViewer.SetWindowTextW(bstrLogViewerPath);
		} else {
			m_edLogViewer.SetWindowTextW(TEXT("NOTEPAD.EXE"));
		}
		_bstr_t bstrCppLogOutput = m_pSettings->GetString(TEXT("Addin"), TEXT("LogOutput"));
		const _bstr_t ENABLED(TEXT("File"));
		if (bstrCppLogOutput.length() > 0) {
			m_rdLogFileRadio.SetCheck(BST_CHECKED);
			m_rdWinDebugRadio.SetCheck(BST_UNCHECKED);
		} else {
			m_rdLogFileRadio.SetCheck(BST_UNCHECKED);
			m_rdWinDebugRadio.SetCheck(BST_CHECKED);
		}
		_bstr_t bstrCppLogLevel = m_pSettings->GetString(TEXT("Addin"), TEXT("LogLevel"));
		const _bstr_t _ERROR(TEXT("ERROR"));
		const _bstr_t _TRACE(TEXT("TRACE"));
		const _bstr_t _NONE(TEXT("NONE"));
		if (bstrCppLogLevel.length() > 0) {
			m_cbCppLogLevel.SelectString(0, bstrCppLogLevel);
		} else {
			m_cbCppLogLevel.SelectString(0, TEXT("NONE"));
		}

	}
    return TRUE;
}

void CAddinPropertyPage::OnOK () {
	CPropertyPage::OnOK ();
	if (m_pSettings->IsValid ()) {
		if (m_bGarbageCollection.GetCheck () == BST_CHECKED) {
			m_pSettings->PutString (TEXT ("Addin"), TEXT ("GarbageCollection"), TEXT ("Enabled"));
		} else {
			m_pSettings->PutString (TEXT ("Addin"), TEXT ("GarbageCollection"), TEXT ("Disabled"));
		}
		if (m_cbShowToolbar.GetCheck() == BST_CHECKED) {
			m_pSettings->PutString (TEXT("Addin"), TEXT("ShowToolbar"), TEXT("Enabled"));
		} else {
			m_pSettings->PutString (TEXT("Addin"), TEXT("ShowToolbar"), TEXT("Disabled"));
		}
		CString csLogViewerPath;
		m_edLogViewer.GetWindowTextW(csLogViewerPath);
		m_pSettings->PutString(TEXT("Addin"), TEXT("LogViewer"), csLogViewerPath.GetBuffer());
		CString csLogLevel;
		m_cbCppLogLevel.GetLBText(m_cbCppLogLevel.GetCurSel(), csLogLevel);
		m_pSettings->PutString(TEXT("Addin"), TEXT("LogLevel"), csLogLevel.GetBuffer());
		if (m_rdLogFileRadio.GetState() == BST_PUSHED) {
			m_pSettings->PutString(TEXT("Addin"), TEXT("LogOutput"), TEXT("File"));
		} else {
			m_pSettings->PutString(TEXT("Addin"), TEXT("LogOutput"), TEXT("WinDbg"));
		}
	}
}

void CAddinPropertyPage::DoDataExchange(CDataExchange* pDX)
{
	CPropertyPage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK_GARBAGE_COLLECTION, m_bGarbageCollection);
	DDX_Control(pDX, IDC_CHECK_HEAP_IN_WORKSHEET, m_cbSaveHeap);
	DDX_Control(pDX, IDC_LOGVIEWEREDIT, m_edLogViewer);
	DDX_Control(pDX, IDC_LOGVIEWERBROWSE, m_btBrowseLogVIewer);
	DDX_Control(pDX, IDC_SHOWTOOLBARCHECK, m_cbShowToolbar);
	DDX_Control(pDX, IDC_LOGLEVELCOMBO, m_cbCppLogLevel);
	DDX_Control(pDX, IDC_LOGFILERADIO, m_rdLogFileRadio);
	DDX_Control(pDX, IDC_WINDEBUGRADIO, m_rdWinDebugRadio);
}

BEGIN_MESSAGE_MAP(CAddinPropertyPage, CPropertyPage)
	ON_BN_CLICKED(IDC_LOGVIEWERBROWSE, &CAddinPropertyPage::OnBnClickedLogviewerbrowse)
END_MESSAGE_MAP()

void CAddinPropertyPage::OnBnClickedLogviewerbrowse()
{
	CFileDialog fileDialog(TRUE, _T("exe"), _T("*.exe"), OFN_PATHMUSTEXIST, _T("Executable Programs (*.exe)|*.exe|All Files (*.*)|*.*||"));
	if (fileDialog.DoModal() == IDOK) {
		POSITION pos = fileDialog.GetStartPosition();
    	if (pos != NULL) {
	    	CString nextPathName = fileDialog.GetNextPathName(pos);
			m_edLogViewer.SetWindowTextW(nextPathName);
		}
	}
}
