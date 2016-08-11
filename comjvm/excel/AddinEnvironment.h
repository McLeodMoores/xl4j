#include "stdafx.h"
#pragma once
#include "Jvm.h"
#include "FunctionRegistry.h"
#include "Converter.h"
#include "GarbageCollector.h"
#include "ExcelUtils.h"
#include "Progress.h"
#include "../settings/SettingsDialog.h"
#include "../core/Settings.h"
#include "../core/internal.h"
#include "../utils/FileUtils.h"
#include "Lifecycle.h"

class CAddinEnvironment {
private:
	Converter *m_pConverter;
	TypeLib *m_pTypeLib;
public:
	CAddinEnvironment ();
	~CAddinEnvironment ();
	inline Converter *GetConverter () const { return m_pConverter; }
	inline TypeLib *GetTypeLib () const { return m_pTypeLib; }
};