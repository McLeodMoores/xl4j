/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

#define SETTINGS_JVM_TEMPLATE	TEXT ("JvmTemplate")

class CSettings;

/// <summary>Underlying implementation of CSettings.</summary>
///
/// <para>Configuration may be fetched from a number of locations, the owner delegates
/// to an appropriate sub-class for wherever the configuration was found.</para>
class CSettingsImpl {
protected:
	friend class CSettings;
public:
	CSettingsImpl ();
	virtual ~CSettingsImpl ();
	virtual const _bstr_t GetString (const _std_string_t &strKey, long lIndex) const = 0;
	virtual const _bstr_t GetString (const _std_string_t &strKey, const _std_string_t &strIndex) const = 0;
};

/// <summary>Configuration settings.</summary>
///
/// <para>Configuration may be defined in the registry or on disk alongside the DLL.</para>
class CSettings {
private:
	CSettingsImpl *m_pImpl;
public:
	CSettings (const _std_string_t &strType, const _std_string_t &strIdentifier);
	~CSettings ();
	BOOL IsValid () const;
	const _bstr_t GetString (const _std_string_t &strKey, long lIndex) const;
	const _bstr_t GetString (const _std_string_t &strKey, const _std_string_t &strIndex) const;
};