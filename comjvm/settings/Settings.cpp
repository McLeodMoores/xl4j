#include "stdafx.h"
#include "Settings.h"
#include "AddinPropertyPage.h"
#include "ClasspathPropertyPage.h"
#include "VmOptionsPropertyPage.h"

CXl4jSettings::CXl4jSettings () {
	AFX_MANAGE_STATE (AfxGetStaticModuleState ());
}


CXl4jSettings::~CXl4jSettings () {
}

void CXl4jSettings::Open (HWND hwndParent) {
	CPropertySheet psSheet (IDS_PROPSHEET_TITLE, CWnd::FromHandle(hwndParent), 0);

	psSheet.AddPage (new CAddinPropertyPage ());
	psSheet.AddPage (new CClasspathPropertyPage ());
	psSheet.AddPage (new CVmOptionsPropertyPage ());
	psSheet.DoModal ();
}
