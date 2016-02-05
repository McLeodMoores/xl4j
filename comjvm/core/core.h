/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

#ifndef COMJVM_CORE_API
# define COMJVM_CORE_API __declspec(dllimport) __stdcall
#endif /* ifndef COMJVM_CORE_API */

#include "core_h.h"

#ifdef __cplusplus
extern "C" {
#endif /* ifdef __cplusplus */

HRESULT COMJVM_CORE_API ComJvmGetHostA (/* [out][retval] */ PSTR *ppszHost);
HRESULT COMJVM_CORE_API ComJvmGetHostW (/* [out][retval] */ PWSTR *ppszHost);
#ifdef _UNICODE
# define ComJvmGetHost ComJvmGetHostW
#else /* ifdef _UNICODE */
# define ComJvmGetHost ComJvmGetHostA
#endif /* ifdef _UNICODE */

HRESULT COMJVM_CORE_API ComJvmGetCLSID (/* [out][retval] */ CLSID *pClass);

HRESULT COMJVM_CORE_API ComJvmCreateInstance (/* [out][retval] */ IJvmSupport **ppJvmSupport);

HRESULT COMJVM_CORE_API ComJvmCreateTemplateA (/* [in][optional] */ PCSTR pszIdentifier, /* [out][retval] */ IJvmTemplate **ppTemplate);
HRESULT COMJVM_CORE_API ComJvmCreateTemplateW (/* [in][optional] */ PCWSTR pszIdentifier, /* [out][retval] */ IJvmTemplate **ppTemplate);
#ifdef _UNICODE
# define ComJvmCreateTemplate ComJvmCreateTemplateW
#else /* ifdef _UNICODE */
# define ComJvmCreateTemplate ComJvmCreateTemplateA
#endif /* ifdef _UNICODE */

HRESULT COMJVM_CORE_API ComJvmCopyTemplate (/* [in] */ IJvmTemplate *pSource, /* [out][retval] */ IJvmTemplate **ppDest);

HRESULT COMJVM_CORE_API ComJvmConnectA (/* [in][optional] */ PCSTR pszIdentifier, /* [in][optional] */ IJvmTemplate *pTemplate, /* [out][retval] */ IJvmContainer **ppJvmContainer);
HRESULT COMJVM_CORE_API ComJvmConnectW (/* [in][optional] */ PCWSTR pszIdentifier, /* [in][optional] */ IJvmTemplate *pTemplate, /* [out][retval] */ IJvmContainer **ppJvmContainer);
#ifdef _UNICODE
# define ComJvmConnect ComJvmConnectW
#else /* ifdef _UNICODE */
# define ComJvmConnect ComJvmConnectA
#endif /* ifdef _UNICODE */

HRESULT COMJVM_CORE_API ComJvmCreateClasspathEntryA (/* [in] */ PCSTR pszLocalPath, /* [out][retval] */ IClasspathEntry **ppEntry);
HRESULT COMJVM_CORE_API ComJvmCreateClasspathEntryW (/* [in] */ PCWSTR pszLocalPath, /* [out][retval] */ IClasspathEntry **ppEntry);
#ifdef _UNICODE
# define ComJvmCreateClasspathEntry ComJvmCreateClasspathEntryW
#else /* ifdef _UNICODE */
# define ComJvmCreateClasspathEntry ComJvmCreateClasspathEntryA
#endif /* ifdef _UNICODE */
#ifdef __cplusplus
}
#endif /* ifdef __cplusplus */