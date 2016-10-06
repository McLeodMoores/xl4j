/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

#include "core_h.h"

class CJvmConnectorFactory {
private:
	CLSID m_clsid;
	_bstr_t m_bstrModule;
public:
	CJvmConnectorFactory (REFCLSID clsid, const _bstr_t &bstrModule);
	CJvmConnectorFactory (REFCLSID clsid);
	CJvmConnectorFactory ();
	CJvmConnectorFactory (const CJvmConnectorFactory &ref);
	~CJvmConnectorFactory ();
	HRESULT Create (IJvmConnector **ppConnector) const;
};

class hash_bstr {
public:
	static const size_t bucket_size = 4;
	size_t operator () (const _bstr_t &bstr) const {
		PCTSTR psz = (PCTSTR)bstr;
		size_t h = 0;
		while (*psz) {
			h = (h * 31) + *(psz++);
		}
		return h;
	}
	bool operator () (const _bstr_t &bstrA, const _bstr_t &bstrB) const {
		return bstrA < bstrB;
	}
};

typedef std::unordered_map<_bstr_t, CJvmConnectorFactory, hash_bstr> CJvmConnectorFactoryCache;

/// <summary>Manages the available IJvmConnector implementations.</summary>
class CJvmConnectors {
private:
	CRITICAL_SECTION m_cs;
	CJvmConnectorFactoryCache m_cache;
	HRESULT FindLocalDLLs ();
public:
	CJvmConnectors ();
	~CJvmConnectors ();
	HRESULT Find (const _bstr_t &bstrName, IJvmConnector **ppConnector);
};
