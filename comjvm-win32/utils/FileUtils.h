/*
* Copyright 2014-present by McLeod Moores Software Limited.
* See distribution for license.
*/

/*
* File utilities.  These are semi-duplicated ini AddinFileUtils in the Excel project to handle delay-loading.
*/

#pragma once

#ifdef COMJVM_FILEUTILS_EXPORT
# define COMJVM_FILEUTILS_API __declspec(dllexport)
#else
# define COMJVM_FILEUTILS_API __declspec(dllimport)
#endif /* ifndef COMJVM_FILEUTILS_API */

#include <strsafe.h>
#include "jni.h"
#include <string>

class COMJVM_FILEUTILS_API FileUtils {
private:
	FileUtils();
public:
	// KEEP IN SYNC WITH ADDINFILEUTILS IN EXCEL PROJECT
	static HRESULT GetAddinAbsolutePath(wchar_t *szFullPath, size_t cFullPath, const wchar_t *szFileName);
	static HRESULT GetAddinDirectory(wchar_t *pszDirectory, size_t cDirectory);
	static HRESULT GetDirectoryFromFullPath(wchar_t *szDirectory, size_t cDirectory, const wchar_t *szFullPath);
	static HRESULT GetDllFileName(wchar_t *pszFilename, size_t cFilename);
	static HRESULT GetTemporaryFileName(const wchar_t *pszLeafFilename, wchar_t *pszBuffer, size_t cBuffer);
	static bool FileExists(const wchar_t *szPath);
	static HRESULT FileSize(const wchar_t *szPath, PLARGE_INTEGER pSize);
};