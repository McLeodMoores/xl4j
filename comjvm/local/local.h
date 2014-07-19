/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

#ifndef COMJVM_LOCAL_API
# define COMJVM_LOCAL_API __declspec(dllimport) __stdcall
#endif /* ifndef COMJVM_LOCAL_API */

#include "core_h.h"

#ifdef __cplusplus
extern "C" {
#endif /* ifdef __cplusplus */

HRESULT COMJVM_LOCAL_API ComJvmCreateClasspathA (/* [in] */ PCSTR pszOwner, /* [out][retval] */ IClasspath **ppClasspath);
HRESULT COMJVM_LOCAL_API ComJvmCreateClasspathW (/* [in] */ PCWSTR pszOwner, /* [out][retval] */ IClasspath **ppClasspath);
#ifdef _UNICODE
# define ComJvmCreateClasspath ComJvmCreateClasspathW
#else /* ifdef _UNICODE */
# define ComJvmCreateClasspath ComJvmCreateClasspathA
#endif /* ifdef _UNICODE */

HRESULT COMJVM_LOCAL_API ComJvmCreateDirectoryWriterA (/* [in] */ PCSTR pszPath, /* [out][retval] */ IDirectoryWriter **ppWriter);
HRESULT COMJVM_LOCAL_API ComJvmCreateDirectoryWriterW (/* [in] */ PCWSTR pszPath, /* [out][retval] */ IDirectoryWriter **ppWriter);
#ifdef _UNICODE
#define ComJvmCreateDirectoryWriter ComJvmCreateDirectoryWriterW
#else /* ifdef _UNICODE */
#define ComJvmCreateDirectoryWriter ComJvmCreateDirectoryWriterA
#endif /* ifdef _UNICODE */

HRESULT COMJVM_LOCAL_API ComJvmCreateFileWriterA (/* [in] */ PCSTR pszPath, /* [out][retval] */ IFileWriter **ppWriter);
HRESULT COMJVM_LOCAL_API ComJvmCreateFileWriterW (/* [in] */ PCWSTR pszPath, /* [out][retval] */ IFileWriter **ppWriter);
#ifdef _UNICODE
#define ComJvmCreateFileWriter ComJvmCreateFileWriterW
#else /* ifdef _UNICODE */
#define ComJvmCreateFileWriter ComJvmCreateFileWriterA
#endif /* ifdef _UNICODE */

HRESULT COMJVM_LOCAL_API ComJvmCreateLocalConnector (/* [out][retval] */ IJvmConnector **ppConnector);

#ifdef __cplusplus
}
#endif /* ifdef __cplusplus */