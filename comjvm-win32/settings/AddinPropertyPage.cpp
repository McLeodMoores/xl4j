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
	}
    return TRUE;
}

void CAddinPropertyPage::OnOK () {
	CPropertyPage::OnOK ();
	if (m_pSettings->IsValid ()) {
		if (m_bGarbageCollection.GetCheck () == BST_CHECKED) {
			LOGTRACE ("GetCheck was BST_CHECKED");
			m_pSettings->PutString (TEXT ("Addin"), TEXT ("GarbageCollection"), TEXT ("Enabled"));
		} else {
			LOGTRACE ("GetCheck was NOT BST_CHECKED");
			m_pSettings->PutString (TEXT ("Addin"), TEXT ("GarbageCollection"), TEXT ("Disabled"));
		}
	}
}

void CAddinPropertyPage::DoDataExchange(CDataExchange* pDX)
{
	CPropertyPage::DoDataExchange (pDX);
	DDX_Control (pDX, IDC_LIST_JVM, m_lbJvms);
	DDX_Control (pDX, IDC_CHECK_GARBAGE_COLLECTION, m_bGarbageCollection);
	DDX_Control (pDX, IDC_CHECK_HEAP_IN_WORKSHEET, m_cbSaveHeap);
}

BEGIN_MESSAGE_MAP(CAddinPropertyPage, CPropertyPage)
END_MESSAGE_MAP()


// CAddinPropertyPage message handlers
