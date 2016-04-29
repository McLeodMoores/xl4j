#include "stdafx.h"
#include "SettingsDialog.h"
#include "AddinPropertyPage.h"
#include "ClasspathPropertyPage.h"
#include "VmOptionsPropertyPage.h"

CSettingsDialog::CSettingsDialog (CSettings *pSettings)  : m_pSettings(pSettings) {
	AFX_MANAGE_STATE (AfxGetStaticModuleState ());
	init ();
}

CSettingsDialog::~CSettingsDialog () {
	if (!m_pAddinPropertyPage) { delete m_pAddinPropertyPage; }
	if (!m_pClasspathPropertyPage) { delete m_pClasspathPropertyPage; }
	if (!m_pVmOptionsPropertyPage) { delete m_pVmOptionsPropertyPage; }
}

void CSettingsDialog::init () {
	m_pAddinPropertyPage = new CAddinPropertyPage ();
	m_pClasspathPropertyPage = new CClasspathPropertyPage ();
	m_pVmOptionsPropertyPage = new CVmOptionsPropertyPage ();
}

void CSettingsDialog::Open (HWND hwndParent) {
	CPropertySheet psSheet (IDS_PROPSHEET_TITLE, CWnd::FromHandle(hwndParent), 0);
	psSheet.AddPage (m_pAddinPropertyPage);
	psSheet.AddPage (m_pClasspathPropertyPage);
	psSheet.AddPage (m_pVmOptionsPropertyPage);
	psSheet.DoModal ();
}
