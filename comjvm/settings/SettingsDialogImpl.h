#pragma once
#include <string>
#include <comdef.h>
#include "..\core\Settings.h"
#include "SettingsDialog.h"
#include "AddinPropertyPage.h"
#include "ClasspathPropertyPage.h"
#include "VmOptionsPropertyPage.h"

class CSettingsDialogImpl : public ISettingsDialog {
private:
	CAddinPropertyPage *m_pAddinPropertyPage;
	CClasspathPropertyPage *m_pClasspathPropertyPage;
	CVmOptionsPropertyPage *m_pVmOptionsPropertyPage;
	CSettings *m_pSettings;
	void init ();
public:
	CSettingsDialogImpl (CSettings *pSettings);
	~CSettingsDialogImpl ();
	virtual void Open (HWND hwndParent);
};

