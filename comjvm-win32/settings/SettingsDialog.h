/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once
#include <string>
#include <comdef.h>
#include "..\core\Settings.h"
#include "SettingsDialog.h"
#include "AddinPropertyPage.h"
#include "ClasspathPropertyPage.h"
#include "VmOptionsPropertyPage.h"

class CSettingsDialog : public ISettingsDialog {
private:
	CAddinPropertyPage *m_pAddinPropertyPage;
	CClasspathPropertyPage *m_pClasspathPropertyPage;
	CVmOptionsPropertyPage *m_pVmOptionsPropertyPage;
	CSettings *m_pSettings;
	void init ();
public:
	CSettingsDialog (CSettings *pSettings);
	~CSettingsDialog ();
	virtual INT_PTR Open (HWND hwndParent);
};

