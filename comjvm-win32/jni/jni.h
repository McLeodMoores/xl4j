/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

#ifndef COMJVM_JNI_API
# define COMJVM_JNI_API __declspec(dllimport) __stdcall
#endif /* ifndef COMJVM_JNI_API */

#ifdef __cplusplus
extern "C" {
#endif /* ifdef __cplusplus */

#pragma pack(8)
typedef struct tagJAVA_VM_PARAMETERSA {
	DWORD cbSize;
	PCSTR pszClasspath;
	DWORD cOptions;
	PCSTR *ppszOptions;
} JAVA_VM_PARAMETERSA, *PJAVA_VM_PARAMETERSA;

typedef struct tagJAVA_VM_PARAMETERSW {
	DWORD cbSize;
	PCWSTR pszClasspath;
	DWORD cOptions;
	PCWSTR *ppszOptions;
} JAVA_VM_PARAMETERSW, *PJAVA_VM_PARAMETERSW;
#ifdef _UNICODE
# define JAVA_VM_PARAMETERS		JAVA_VM_PARAMETERSW
# define PJAVA_VM_PARAMETERS	PJAVA_VM_PARAMETERSW
#else /* ifdef _UNICODE */
# define JAVA_VM_PARAMETERS		JAVA_VM_PARAMETERSA
# define PJAVA_VM_PARAMETERS	PJAVA_VM_PARAMETERSA
#endif /* ifdef _UNICODE */
#pragma pack()

HRESULT COMJVM_JNI_API JNICreateJavaVMW (PDWORD pdwJvmRef, PJAVA_VM_PARAMETERSW pParams);
HRESULT COMJVM_JNI_API JNICreateJavaVMA (PDWORD pdwJvmRef, PJAVA_VM_PARAMETERSA pParams);
#ifdef _UNICODE
# define JNICreateJavaVM JNICreateJavaVMW
#else /* ifdef _UNICODE */
# define JNICreateJavaVM JNICreateJavaVMA
#endif /* ifdef _UNICODE */

typedef HRESULT (APIENTRY *JNICallbackProc) (LPVOID lpUser, JNIEnv *pEnv);

HRESULT COMJVM_JNI_API JNICallback (DWORD dwJvmRef, JNICallbackProc pfnCallback, PVOID pData);

HRESULT COMJVM_JNI_API JNIDestroyJavaVM (DWORD dwJvmRef);

#ifdef __cplusplus
}
#endif /* ifdef __cplusplus */