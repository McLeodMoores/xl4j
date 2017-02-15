/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

/// @file

#include "stdafx.h"
#include "local.h"
#include "Classpath.h"
#include "DirectoryWriter.h"
#include "FileWriter.h"
#include "JvmConnector.h"

/// <summary>Creates an IClasspath instance for building a local classpath.</summary>
///
/// <para>The JVM created by this library will be in-process so any classpath entries must be
/// local. If remote IClasspathEntry instances are used (for example, files that might be local
/// to the JVM consumer but are not immediately accessible as a local path for the JVM
/// implementation) then the files may be copied to a local area.</para>
///
/// <para>Note that the JVM identifier is used to scope locally cached classpath entries; it is
/// not necessary for that JVM to actually exist at this point.</para>
///
/// <param name="pszOwner">Identifier of the JVM this instance is being created for</param>
/// <param name="ppClasspath">Receives the classpath instance</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT COMJVM_LOCAL_API ComJvmCreateClasspathA (/* [in] */ PCSTR pszOwner, /* [out][retval] */ IClasspath **ppClasspath) {
	if (!pszOwner) return E_POINTER;
	if (!ppClasspath) return E_POINTER;
	try {
		*ppClasspath = new CClasspath (_tstring (pszOwner));
		return S_OK;
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}

/// <summary>Creates an IClasspath instance for building a local classpath.</summary>
///
/// <para>The JVM created by this library will be in-process so any classpath entries must be
/// local. If remote IClasspathEntry instances are used (for example, files that might be local
/// to the JVM consumer but are not immediately accessible as a local path for the JVM
/// implementation) then the files may be copied to a local area.</para>
///
/// <para>Note that the JVM identifier is used to scope locally cached classpath entries; it is
/// not necessary for that JVM to actually exist at this point.</para>
///
/// <param name="pszOwner">Identifier of the JVM this instance is being created for</param>
/// <param name="ppClasspath">Receives the classpath instance</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT COMJVM_LOCAL_API ComJvmCreateClasspathW (/* [in] */ PCWSTR pszOwner, /* [out][retval] */ IClasspath **ppClasspath) {
	if (!pszOwner) return E_POINTER;
	if (!ppClasspath) return E_POINTER;
	*ppClasspath = new CClasspath (pszOwner);
	if (!*ppClasspath) return E_OUTOFMEMORY;
	return S_OK;
}

static HRESULT CreateDirectoryImplA (PCSTR pszPath);

static HRESULT CreateParentDirectoryA (PCSTR pszPath) {
	size_t cch = strlen (pszPath);
	while (cch > 0) {
		cch--;
		if (pszPath[cch] == '\\') {
			PSTR pszParent = new CHAR[cch + 1];
			CopyMemory (pszParent, pszPath, cch * sizeof (CHAR));
			pszParent[cch] = 0;
			HRESULT hr = CreateDirectoryImplA (pszParent);
			delete pszParent;
			return hr;
		}
	}
	return E_INVALIDARG;
}

static HRESULT CreateDirectoryImplA (PCSTR pszPath) {
	if (CreateDirectoryA (pszPath, NULL)) return S_OK;
	DWORD dwError = GetLastError ();
	if (dwError == ERROR_PATH_NOT_FOUND) {
		HRESULT hr = CreateParentDirectoryA (pszPath);
		if (FAILED (hr)) return hr;
		if (CreateDirectoryA (pszPath, NULL)) return S_OK;
		dwError = GetLastError ();
	}
	if (dwError == ERROR_ALREADY_EXISTS) {
		DWORD dwAttr = GetFileAttributesA (pszPath);
		if ((dwAttr != INVALID_FILE_ATTRIBUTES) && (dwAttr & FILE_ATTRIBUTE_DIRECTORY)) return S_FALSE;
	}
	return HRESULT_FROM_WIN32 (dwError);
}

static HRESULT CreateDirectoryImplW (PCWSTR pszPath);

static HRESULT CreateParentDirectoryW (PCWSTR pszPath) {
	size_t cch = wcslen (pszPath);
	while (cch > 0) {
		cch--;
		if (pszPath[cch] == '\\') {
			PWSTR pszParent = new WCHAR[cch + 1];
			CopyMemory (pszParent, pszPath, cch * sizeof (WCHAR));
			pszParent[cch] = 0;
			HRESULT hr = CreateDirectoryImplW (pszParent);
			delete pszParent;
			return hr;
		}
	}
	return E_INVALIDARG;
}

static HRESULT CreateDirectoryImplW (PCWSTR pszPath) {
	if (CreateDirectoryW (pszPath, NULL)) return S_OK;
	DWORD dwError = GetLastError ();
	if (dwError == ERROR_PATH_NOT_FOUND) {
		HRESULT hr = CreateParentDirectoryW (pszPath);
		if (FAILED (hr)) return hr;
		if (CreateDirectoryW (pszPath, NULL)) return S_OK;
		dwError = GetLastError ();
	}
	if (dwError == ERROR_ALREADY_EXISTS) {
		DWORD dwAttr = GetFileAttributesW (pszPath);
		if ((dwAttr != INVALID_FILE_ATTRIBUTES) && (dwAttr & FILE_ATTRIBUTE_DIRECTORY)) return S_FALSE;
	}
	return HRESULT_FROM_WIN32 (dwError);
}

/// <summary>Creates an IDirectoryWriter instance.</summary>
///
/// <para>This is the ANSI form.</para>
///
/// <param name="pszPath">Base folder of the target filesystem area</param>
/// <param name="ppWriter">Receives the new instance</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT COMJVM_LOCAL_API ComJvmCreateDirectoryWriterA (
	/* [in] */ PCSTR pszPath,
	/* [out][retval] */ IDirectoryWriter **ppWriter
	) {
	if (!pszPath) return E_POINTER;
	if (!ppWriter) return E_POINTER;
	try {
		HRESULT hr = CreateDirectoryImplA (pszPath);
		if (FAILED (hr)) return hr;
		*ppWriter = new CDirectoryWriter (_tstring (pszPath));
		return S_OK;
	} catch (_com_error &e) {
		return e.Error ();
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}

/// <summary>Creates an IDirectoryWriter instance.</summary>
///
/// <para>This is the wide-character form.</para>
///
/// <param name="pszPath">Base folder of the target filesystem area</param>
/// <param name="ppWriter">Receives the new instance</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT COMJVM_LOCAL_API ComJvmCreateDirectoryWriterW (
	/* [in] */ PCWSTR pszPath,
	/* [out][retval] */ IDirectoryWriter **ppWriter
	) {
	if (!pszPath) return E_POINTER;
	if (!ppWriter) return E_POINTER;
	try {
		HRESULT hr = CreateDirectoryImplW (pszPath);
		if (FAILED (hr)) return hr;
		*ppWriter = new CDirectoryWriter (_tstring (pszPath));
		return S_OK;
	} catch (_com_error &e) {
		return e.Error ();
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}

/// <summary>Creates an IFileWriter instance.</summary>
///
/// <para>This is the ANSI form.</para>
///
/// <param name="pszPath">Target file to write to</param>
/// <param name="ppWriter">Receives the new instance</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT COMJVM_LOCAL_API ComJvmCreateFileWriterA (
	/* [in] */ PCSTR pszPath,
	/* [out][retval] */ IFileWriter **ppWriter
	) {
	if (!pszPath) return E_POINTER;
	if (!ppWriter) return E_POINTER;
	HANDLE hFile = CreateFileA (pszPath, GENERIC_WRITE, 0, NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
	while (hFile == INVALID_HANDLE_VALUE) {
		DWORD dwError = GetLastError ();
		if (dwError == ERROR_PATH_NOT_FOUND) {
			HRESULT hr = CreateParentDirectoryA (pszPath);
			if (FAILED (hr)) return hr;
			hFile = CreateFileA (pszPath, GENERIC_WRITE, 0, NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
			if (hFile == INVALID_HANDLE_VALUE) {
				dwError = GetLastError ();
			} else {
				break;
			}
		}
		return HRESULT_FROM_WIN32 (dwError);
	}
	try {
		*ppWriter = new CFileWriter (hFile);
		return S_OK;
	} catch (std::bad_alloc) {
		CloseHandle (hFile);
		return E_OUTOFMEMORY;
	}
}

/// <summary>Creates an IFileWriter instance.</summary>
///
/// <para>This is the wide-character form.</para>
///
/// <param name="pszPath">Target file to write to</param>
/// <param name="ppWriter">Receives the new instance</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT COMJVM_LOCAL_API ComJvmCreateFileWriterW (
	/* [in] */ PCWSTR pszPath,
	/* [out][retval] */ IFileWriter **ppWriter
	) {
	if (!pszPath) return E_POINTER;
	if (!ppWriter) return E_POINTER;
	HANDLE hFile = CreateFileW (pszPath, GENERIC_WRITE, 0, NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
	while (hFile == INVALID_HANDLE_VALUE) {
		DWORD dwError = GetLastError ();
		if (dwError == ERROR_PATH_NOT_FOUND) {
			HRESULT hr = CreateParentDirectoryW (pszPath);
			if (FAILED (hr)) return hr;
			hFile = CreateFileW (pszPath, GENERIC_WRITE, 0, NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
			if (hFile == INVALID_HANDLE_VALUE) {
				dwError = GetLastError ();
			} else {
				break;
			}
		}
		return HRESULT_FROM_WIN32 (dwError);
	}
	try {
		*ppWriter = new CFileWriter (hFile);
		return S_OK;
	} catch (std::bad_alloc) {
		CloseHandle (hFile);
		return E_OUTOFMEMORY;
	}
}

/// <summary>Creates a connector to an in-process JVM.</summary>
///
/// <para>This is only a partial implementation of the full IJvmConnector
/// contract as it is only possible to load a single JVM into any given
/// process.</para>
///
/// <param name="ppConnector">Receives the connector instance</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT COMJVM_LOCAL_API ComJvmCreateLocalConnector (/* [out][retval] */ IJvmConnector **ppConnector) {
	if (!ppConnector) return E_POINTER;
	try {
		*ppConnector = new CJvmConnector ();
		return S_OK;
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}
