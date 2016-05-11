#include "stdafx.h"
#include "SettingsDialog.h"
#include "SettingsDialogImpl.h"

HRESULT CSettingsDialogFactory::Create (CSettings *pSettings, ISettingsDialog **ppDialog) {
	*ppDialog = new CSettingsDialogImpl (pSettings);
	return S_OK;
}