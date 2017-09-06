/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

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
#include <jni.h>
#include <string>
//#include <xlcall.h>

#ifdef __cplusplus
extern "C" {
#endif /* ifdef __cplusplus */

#define WIDEN2(x) L ## x
#define WIDEN(x) WIDEN2(x)
#define __SHORT_FILE__ (strrchr(__FILE__, '/') ? strrchr(__FILE__, '/') + 1 : strrchr(__FILE__, '\\') ? strrchr(__FILE__, '\\') + 1 : __FILE__)

enum LOGLEVEL { LOGLEVEL_TRACE = 0, LOGLEVEL_DEBUG = 1, LOGLEVEL_INFO = 2, LOGLEVEL_WARN = 3, LOGLEVEL_ERROR = 4, LOGLEVEL_FATAL = 5, LOGLEVEL_NONE = 6 };
enum LOGTARGET { LOGTARGET_FILE, LOGTARGET_WINDEBUG };

class Debug
{
private:
	Debug ();
	~Debug ();
	// We don't export these with a COMJVM_DEBUG_API at the class level because they cause
	// issues with delay loading due to optimizations.
	const static wchar_t *LOGLEVEL_STR[];
	const static wchar_t *LOGFILENAME;
	const static LARGE_INTEGER TOOLARGE;
	static size_t m_cMaxFileNameLength;
	static size_t m_cMaxFunctionNameLength;
	volatile static LOGLEVEL m_logLevel;
	volatile static LOGTARGET m_logTarget;
	static FILE *m_fdLogFile;
	static void appendExceptionTraceMessages (
		JNIEnv&      a_jni_env,
		std::string& a_error_msg,
		jthrowable   a_exception,
		jmethodID    a_mid_throwable_getCause,
		jmethodID    a_mid_throwable_getStackTrace,
		jmethodID    a_mid_throwable_toString,
		jmethodID    a_mid_frame_toString);
public:
	static COMJVM_DEBUG_API void odprintf (LPCTSTR sFormat, ...);
	static COMJVM_DEBUG_API void PrettyLogPrintf (LOGLEVEL logLevel, const char *sFileName, int iLineNum, const char *sFunctionName, LPCTSTR sFormat, ...);
	static COMJVM_DEBUG_API HRESULT print_HRESULT (HRESULT result);
	static COMJVM_DEBUG_API void LOGTRACE_SAFEARRAY(SAFEARRAY * psa);
	static COMJVM_DEBUG_API void LOGERROR_SAFEARRAY(SAFEARRAY * psa);
	static COMJVM_DEBUG_API void LOGTRACE_VARIANT(VARIANT * pVariant);
	static COMJVM_DEBUG_API void LOGERROR_VARIANT(VARIANT * pVariant);
	static COMJVM_DEBUG_API void printException (JNIEnv *pEnv, jthrowable exception);
	static COMJVM_DEBUG_API bool CheckException (JNIEnv *pEnv);
	//static void printXLOPER (XLOPER12 *oper);
	static COMJVM_DEBUG_API void SetThreadName (DWORD dwThreadID, const char* threadName);
	static COMJVM_DEBUG_API void SetLogLevel(LOGLEVEL logLevel);
	static COMJVM_DEBUG_API LOGLEVEL GetLogLevel() { return m_logLevel; }
	static COMJVM_DEBUG_API bool CheckLogFileSize(LARGE_INTEGER tooLargeSize);
	static COMJVM_DEBUG_API void SetLogTarget(LOGTARGET logTarget);
	static COMJVM_DEBUG_API LOGTARGET GetLogTarget() { return m_logTarget; }
};

#define LOGTRACE_OFF __pragma(push_macro(LOGTRACE))
#define LOGTRACE_ON __pragme(pop_macro(LOGTRACE))

#if 0 //def _DEBUG
#define LOGTRACE(x, ...) 
#define LOGDEBUG(x, ...)
#define LOGINFO(x, ...)
#define LOGWARN(x, ...)
#define LOGERROR(x, ...)
#define LOGFATAL(x, ...)
#else
#define LOGTRACE(x, ...)\
  do { \
    if (Debug::GetLogLevel() <= LOGLEVEL_TRACE) {\
      Debug::PrettyLogPrintf(LOGLEVEL_TRACE, __SHORT_FILE__, __LINE__, __FUNCTION__, TEXT(x), __VA_ARGS__);\
    }\
  } while (0)
#endif
#define LOGDEBUG(x, ...)\
  do { \
    if (Debug::GetLogLevel() <= LOGLEVEL_DEBUG) {\
      Debug::PrettyLogPrintf(LOGLEVEL_DEBUG, __SHORT_FILE__, __LINE__, __FUNCTION__, TEXT(x), __VA_ARGS__);\
    }\
  } while (0)
#define LOGINFO(x, ...)\
  do { \
    if (Debug::GetLogLevel() <= LOGLEVEL_INFO) {\
      Debug::PrettyLogPrintf(LOGLEVEL_INFO, __SHORT_FILE__, __LINE__, __FUNCTION__, TEXT(x), __VA_ARGS__);\
    }\
  } while (0)
#define LOGWARN(x, ...)\
  do { \
    if (Debug::GetLogLevel() <= LOGLEVEL_WARN) {\
      Debug::PrettyLogPrintf(LOGLEVEL_WARN, __SHORT_FILE__, __LINE__, __FUNCTION__, TEXT(x), __VA_ARGS__);\
    }\
  } while (0)
#define LOGERROR(x, ...)\
  do { \
    if (Debug::GetLogLevel() <= LOGLEVEL_ERROR) {\
      Debug::PrettyLogPrintf(LOGLEVEL_ERROR, __SHORT_FILE__, __LINE__, __FUNCTION__, TEXT(x), __VA_ARGS__);\
    }\
  } while (0)
#define LOGFATAL(x, ...)\
  do { \
    if (Debug::GetLogLevel() <= LOGLEVEL_FATAL) {\
      Debug::PrettyLogPrintf(LOGLEVEL_FATAL, __SHORT_FILE__, __LINE__, __FUNCTION__, TEXT(x), __VA_ARGS__);\
    }\
  } while (0)
#define HRESULT_TO_STR(x) _com_error(x).ErrorMessage()
#define WIN32_TO_STR(x) _com_error(HRESULT_FROM_WIN32(x)).ErrorMessage()
#define GETLASTERROR_TO_STR() _com_error(HRESULT_FROM_WIN32(GetLastError())).ErrorMessage()
#define GETLASTERROR_TO_HRESULT() HRESULT_FROM_WIN32(GetLastError())
#define CHECK_EXCEPTION(pEnv)\
  (pEnv->ExceptionCheck() ? \
      Debug::PrettyLogPrintf(LOGLEVEL_ERROR, __SHORT_FILE__, __LINE__, __FUNCTION__, TEXT("Native-side Java Exception")), \
      Debug::printException(pEnv, pEnv->ExceptionOccurred()), \
      pEnv->ExceptionClear(), true : false) 
#ifdef __cplusplus
}
#endif /* ifdef __cplusplus */