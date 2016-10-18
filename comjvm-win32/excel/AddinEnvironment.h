#include "stdafx.h"
#pragma once
#include "Converter.h"
#include "../core/Settings.h"

class CAddinEnvironment {
private:
	Converter *m_pConverter;
	TypeLib *m_pTypeLib;
	CSettings *m_pSettings;

	bool m_bToolbarEnabled;
	void InitFromSettings();
public:
	CAddinEnvironment ();
	~CAddinEnvironment ();
	Converter *GetConverter () const { return m_pConverter; }
	TypeLib *GetTypeLib () const { return m_pTypeLib; }
	CSettings *GetSettings () const { return m_pSettings; }
	HRESULT GetLogViewerPath(wchar_t *pBuffer, size_t cbSize);
	bool IsToolbarEnabled() const { return m_bToolbarEnabled; }
	void RefreshSettings() { InitFromSettings(); }
	void AddToolbar();
	void RemoveToolbar();
};