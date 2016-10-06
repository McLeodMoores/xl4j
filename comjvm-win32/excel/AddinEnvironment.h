#include "stdafx.h"
#pragma once
#include "Converter.h"
#include "../core/Settings.h"

class CAddinEnvironment {
private:
	Converter *m_pConverter;
	TypeLib *m_pTypeLib;
	CSettings *m_pSettings;
public:
	CAddinEnvironment ();
	~CAddinEnvironment ();
	Converter *GetConverter () const { return m_pConverter; }
	TypeLib *GetTypeLib () const { return m_pTypeLib; }
	CSettings *GetSettings () const { return m_pSettings; }
};