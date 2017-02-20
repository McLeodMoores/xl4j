/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#pragma once

/// <summary>Locally cached JAR and class files.</summary>
class CJarAndClassCache {
private:
	_std_string_t m_strHost;
	_std_string_t m_strHostPath;
	_std_string_t m_strLocalPath;
	long m_lFileCount;
	long m_lFileSize;
	MIDL_uhyper m_uTimestamp;
public:
	CJarAndClassCache (const _std_string_t &strOwner, const _std_string_t &strHost, const _std_string_t &strHostPath);
	~CJarAndClassCache ();
	HRESULT Clear ();
	HRESULT Flush ();
	/// <summary>Gets the number of files cached.</summary>
	long get_FileCount () const { return m_lFileCount; }
	/// <summary>Sets the number of files in this cache.</summary>
	void set_FileCount (long lFileCount) { m_lFileCount = lFileCount; }
	/// <summary>Gets the total size of the cached file(s).</summary>
	long get_FileSize () const { return m_lFileSize; }
	/// <summary>Sets the total size of the cached file(s).</summary>
	void set_FileSize (long lFileSize) { m_lFileSize = lFileSize; }
	/// <summary>Gets the timestamp information.</summary>
	MIDL_uhyper get_Timestamp () const { return m_uTimestamp; }
	/// <summary>Sets the timestamp information.</summary>
	void set_Timestamp (MIDL_uhyper uTimestamp) { m_uTimestamp = uTimestamp; }
	/// <summary>Gets the local path where the cached file(s) reside.</summary>
	_std_string_t get_LocalPath () const { return m_strLocalPath; }
};
