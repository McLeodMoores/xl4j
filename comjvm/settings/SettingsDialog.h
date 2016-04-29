#pragma once
#include "..\core\Settings.h"

class CSettingsDialog {
private:
	CAddinPropertyPage *m_pAddinPropertyPage;
	CClasspathPropertyPage *m_pClasspathPropertyPage;
	CVmOptionsPropertyPage *m_pVmOptionsPropertyPage;
	CSettings *m_pSettings;
	void init ();
public:
	CSettingsDialog (CSettings *pSettings);
	~CSettingsDialog ();
	void Open (HWND hwndParent);
};

