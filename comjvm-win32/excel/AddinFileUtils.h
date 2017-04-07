/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

/*
 * A local set of file utilities to avoid chicken-and-egg problem during loading of delay loaded DLLs in Lifecycle.cpp
 */

#pragma once

#ifdef COMJVM_ADDINFILEUTILS_EXPORT
# define COMJVM_ADDINFILEUTILS_API __declspec(dllexport)
#else
# define COMJVM_ADDINFILEUTILS_API __declspec(dllimport)
#endif /* ifndef COMJVM_ADDINFILEUTILS_API */

#include <strsafe.h>
#include "jni.h"
#include <string>

class COMJVM_ADDINFILEUTILS_API AddinFileUtils {
private:
	AddinFileUtils ();
	// KEEP THIS LOCAL SUBSET IN SYNC WITH FILEUTILS IN UTILS PROJECT
	// THESE ARE PRIVATE TO SUGGEST YOU USE utils/FileUtils instead.
	static HRESULT GetAddinDirectory(wchar_t *pszDirectory, size_t cDirectory);
	static HRESULT GetDirectoryFromFullPath(wchar_t *szDirectory, size_t cDirectory, const wchar_t *szFullPath);
	static HRESULT GetDllFileName(wchar_t *pszFilename, size_t cFilename);
public:
	static HRESULT GetAddinAbsolutePath (wchar_t *szFullPath, size_t cFullPath, const wchar_t *szFileName);
};
