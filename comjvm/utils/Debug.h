/*
 * Debugging API
 */

#pragma once

#ifdef COMJVM_DEBUG_EXPORT
# define COMJVM_DEBUG_API __declspec(dllexport)
#else
# define COMJVM_DEBUG_API __declspec(dllimport)
#endif /* ifndef COMJVM_DEBUG_API */

#include <strsafe.h>
#include "jni.h"
#include <string>

#ifdef __cplusplus
extern "C" {
#endif /* ifdef __cplusplus */

class COMJVM_DEBUG_API Debug
{
private:
	Debug ();
	~Debug ();
	static void appendExceptionTraceMessages (
		JNIEnv&      a_jni_env,
		std::string& a_error_msg,
		jthrowable   a_exception,
		jmethodID    a_mid_throwable_getCause,
		jmethodID    a_mid_throwable_getStackTrace,
		jmethodID    a_mid_throwable_toString,
		jmethodID    a_mid_frame_toString);
public:
	static void odprintf (LPCTSTR sFormat, ...);
	static HRESULT print_HRESULT (HRESULT result); 
	static void printException (JNIEnv *pEnv, jthrowable exception);
};

#ifdef _DEBUG
#define TRACE(x, ...) do { Debug::odprintf(TEXT(x) TEXT("\n"), __VA_ARGS__); } while (0)
#else
#define TRACE(x, ...) 
#endif

#ifdef __cplusplus
}
#endif /* ifdef __cplusplus */