#include "stdafx.h"
#include "Excel.h"
#include "AddinEnvironment.h"
#include "TypeLib.h"

CAddinEnvironment::CAddinEnvironment () {
	m_pTypeLib = new TypeLib ();
	m_pConverter = new Converter (m_pTypeLib);
}

CAddinEnvironment::~CAddinEnvironment () {
	delete m_pConverter;
	delete m_pTypeLib;
}
