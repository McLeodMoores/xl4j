/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

#define SETTINGS_JVM_TEMPLATE	TEXT ("JvmTemplate")

#ifdef COMJVM_SETTINGS_EXPORT
# define COMJVM_SETTINGS_API __declspec(dllexport)
#else
# define COMJVM_SETTINGS_API __declspec(dllimport)
#endif /* ifndef COMJVM_DEBUG_API */

#ifdef _UNICODE
typedef std::wstring _std_string_t;
#else /* ifdef _UNICODE */
typedef std::string _std_string_t;
#endif /* ifdef _UNICODE */

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
	virtual BOOL PutString (const _std_string_t &strKey, long lIndex, const _std_string_t &strValue) = 0;
	virtual BOOL PutString (const _std_string_t &strKey, const _std_string_t &strIndex, const _std_string_t &strValue) = 0;
	virtual BOOL DeleteString (const _std_string_t &strKey, long lIndex) = 0;
	virtual BOOL DeleteString (const _std_string_t &strKey, const _std_string_t &strIndex) = 0;
	virtual BOOL DeleteKey (const _std_string_t &strKey) = 0;
};

const _std_string_t SECTION_ADDIN = _T("Addin");
const _std_string_t KEY_UPGRADE_EARLIEST = _T("UpgradeCheckEarliest");
const _std_string_t KEY_UPGRADE_CHECK_REQUIRED = _T("CheckForUpgrades");
const _std_string_t VALUE_UPGRADE_CHECK_REQUIRED_YES = _T("Yes");
const _std_string_t VALUE_UPGRADE_CHECK_REQUIRED_NO = _T("No");
const _std_string_t VALUE_UPGRADE_CHECK_REQUIRED_DEFAULT = VALUE_UPGRADE_CHECK_REQUIRED_YES;

/// <summary>Configuration settings.</summary>
///
/// <para>Configuration may be defined in the registry or on disk alongside the DLL.</para>
class COMJVM_SETTINGS_API CSettings {
private:
	CSettingsImpl *m_pImpl;
public:
	enum LoadType { INIT_APPDATA, MOST_LOCAL };
	CSettings (const _std_string_t &strType, const _std_string_t &strIdentifier, LoadType eLoadType);
	~CSettings ();
	BOOL IsValid () const;
	const _bstr_t GetString (const _std_string_t &strKey, long lIndex) const;
	const _bstr_t GetString (const _std_string_t &strKey, const _std_string_t &strIndex) const;
    BOOL PutString (const _std_string_t &strKey, long lIndex, const _std_string_t &strValue);
	BOOL PutString (const _std_string_t &strKey, const _std_string_t &strIndex, const _std_string_t &strValue);
	BOOL DeleteString (const _std_string_t &strKey, long lIndex);
	BOOL DeleteString (const _std_string_t &strKey, const _std_string_t &strIndex);
	BOOL DeleteKey (const _std_string_t &strKey);
};