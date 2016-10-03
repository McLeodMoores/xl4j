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

#include "../core/core_h.h"

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

const IID IID_XL4JREFERENCE = { 0x470dd302, 0x0bd5, 0x4e23, { 0x9f, 0x82, 0xa4, 0x25, 0xb7, 0x3a, 0xf0, 0xda } };
const IID IID_XL4JMULTIREFERENCE = { 0x5c20fd94, 0x3101, 0x475f, { 0x80, 0x36, 0xbe, 0xd8, 0xec, 0x47, 0xa0, 0x61 } };
const IID MYLIBID_ComJvmCore = { 0x0e07a0b8, 0x0fa3, 0x4497, { 0xbc, 0x66, 0x6d, 0x2a, 0xf2, 0xa0, 0xb9, 0xc8 } };