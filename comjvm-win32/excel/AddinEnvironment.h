#include "stdafx.h"
#pragma once
#include "Converter.h"
#include "../core/Settings.h"



class CAddinEnvironment {
private:
	enum AddinEnvState { NOT_RUNNING, STARTING, STARTED, TERMINATING };
	CRITICAL_SECTION m_csState;
	AddinEnvState m_state;
	Converter *m_pConverter;
	TypeLib *m_pTypeLib;
	CSettings *m_pSettings;
	int m_idRegisterSomeFunctions;
	int m_idSettings;
	int m_idGarbageCollect;
	int m_idViewJavaLogs;
	int m_idViewCppLogs;
	bool m_bToolbarEnabled;

	
	bool EnterStartingState();
    bool EnterStartedState();
	bool EnterTerminatingState();
	bool EnterNotRunningState();
	void AddToolbar();
	void RemoveToolbar();
	bool IsToolbarEnabled() const { return m_bToolbarEnabled; }
	HRESULT GetLogViewerPath(wchar_t *pBuffer, size_t cbSize);
	HRESULT InitFromSettings();
public:
	CAddinEnvironment ();
	~CAddinEnvironment ();
	HRESULT Start();
	HRESULT Shutdown();
	// TODO: add state checks for these.
	HRESULT GetConverter(Converter **ppConverter) const { *ppConverter = m_pConverter; return S_OK; }
	HRESULT GetTypeLib(TypeLib **ppTypeLib) const { *ppTypeLib = m_pTypeLib; return S_OK; }
	HRESULT GetSettings(CSettings **ppSettings) const {
		if (m_state == STARTED) {
			*ppSettings = m_pSettings;
			return S_OK;
		} else {
			return ERROR_INVALID_STATE;
		}
	}
	HRESULT RefreshSettings() { return InitFromSettings(); }
	HRESULT ViewLogs(const wchar_t *szFileName);
	bool IsShutdown() { return m_state == TERMINATING || m_state == NOT_RUNNING; }
};