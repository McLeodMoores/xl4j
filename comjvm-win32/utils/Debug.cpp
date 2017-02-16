/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "Debug.h"

Debug::Debug ()
{
}


Debug::~Debug ()
{
}

void Debug::odprintf (LPCTSTR sFormat, ...)
{
	va_list argptr;
	va_start (argptr, sFormat);
	//static FILE *logFile = _tfopen (_T ("xl4j.log"), _T ("w"));
	TCHAR buffer[2000];
	HRESULT hr = StringCbVPrintf (buffer, sizeof (buffer), sFormat, argptr);
	if (STRSAFE_E_INSUFFICIENT_BUFFER == hr || S_OK == hr) {
		OutputDebugString (buffer);
		//_ftprintf (logFile, buffer);
		//fflush (logFile);
	} else {
		OutputDebugString (_T ("StringCbVPrintf error."));
		//_ftprintf (logFile, _T ("StringCbVPrintf error."));
		//fflush (logFile);
	}
}

size_t Debug::m_cMaxFileNameLength = 0;
size_t Debug::m_cMaxFunctionNameLength = 0;
LOGLEVEL Debug::m_logLevel = LOGLEVEL_TRACE;
LOGTARGET Debug::m_logTarget = LOGTARGET_WINDEBUG;
FILE *Debug::m_fdLogFile = nullptr;
const wchar_t *Debug::LOGLEVEL_STR[] = { L"TRACE", L"DEBUG", L"INFO ", L"WARN ", L"ERROR", L"FATAL", L"NONE " };

void Debug::PrettyLogPrintf (LOGLEVEL logLevel, const char *sFileName, int iLineNum, const char *sFunctionName, LPCTSTR sFormat, ...) {
	va_list argptr;
	va_start (argptr, sFormat);
	if ((logLevel > LOGLEVEL_NONE) || (logLevel < LOGLEVEL_TRACE)) {
		OutputDebugString(L"PrettyLogPrintf: logLevel invalid so not printing");
		return;
	}
	m_cMaxFileNameLength = max (m_cMaxFileNameLength, strnlen (sFileName, FILENAME_MAX));
	m_cMaxFunctionNameLength = max (m_cMaxFunctionNameLength, strnlen (sFunctionName, FILENAME_MAX));
	const int LINE_MAX = 2000;
	wchar_t buffer[LINE_MAX];
	HRESULT hr = StringCbVPrintf (buffer, sizeof (buffer), sFormat, argptr);
	if (STRSAFE_E_INSUFFICIENT_BUFFER == hr || S_OK == hr) {
		wchar_t formatBuffer[LINE_MAX];
		hr = StringCbPrintf (formatBuffer, sizeof (formatBuffer), TEXT ("%%s %%-%dS %%%dd %%-%dS %%s\n"), m_cMaxFileNameLength, 5, m_cMaxFunctionNameLength);
		if (STRSAFE_E_INSUFFICIENT_BUFFER == hr || S_OK == hr) {
			wchar_t finalBuffer[LINE_MAX];
			HRESULT hr = StringCbPrintf (finalBuffer, sizeof (finalBuffer), formatBuffer, LOGLEVEL_STR[logLevel], sFileName, iLineNum, sFunctionName, buffer);
			if (STRSAFE_E_INSUFFICIENT_BUFFER == hr || S_OK == hr) {
				if (Debug::m_logTarget == LOGTARGET_WINDEBUG) {
					OutputDebugString(finalBuffer);
				} else {
					if (m_fdLogFile) {
						_ftprintf(m_fdLogFile, finalBuffer);
						fflush(m_fdLogFile);
					}
				}
				return;
			}
		}
	}
	OutputDebugString (_T ("StringCbVPrintf error."));
}

HRESULT Debug::print_HRESULT (HRESULT hResult) {
	_com_error error (hResult);
	odprintf (TEXT ("HRESULT error was %s\n"), error.ErrorMessage ());
	return hResult;
}

void Debug::LOGTRACE_SAFEARRAY(SAFEARRAY *psa) {
	LOGTRACE("VT_ARRAY(%dD)", SafeArrayGetDim(psa));
	long elems = 1;
	for (unsigned int i = 1; i <= SafeArrayGetDim(psa); i++) {
		long lowerBound;
		SafeArrayGetLBound(psa, i, &lowerBound);
		long upperBound;
		SafeArrayGetUBound(psa, i, &upperBound);
		LOGTRACE("  dim %d (%d -> %d) (%d elems)", i, lowerBound, upperBound, upperBound - lowerBound);
		elems *= ((upperBound - lowerBound) + 1);
	}
	VARIANT *pArray;
	HRESULT hr = SafeArrayAccessData(psa, (PVOID *)&pArray);
	if (FAILED(hr)) {
		_com_error err(hr);
		LOGERROR("SafeArrayAccessData failed: %s", err.ErrorMessage());
		return;
	}
	if (elems < 25) {
		for (int j = 0; j < elems; j++) {
			LOGTRACE_VARIANT(pArray++);
		}
	}
	SafeArrayUnaccessData(psa);
}
void Debug::LOGTRACE_VARIANT(VARIANT *pVariant) {
	switch (pVariant->vt) {
	case VT_R8:
		LOGTRACE("VT_R8(%f)", V_R8(pVariant));
		break;
	case VT_UI8:
		LOGTRACE("VT_UI8(%llu)", V_UI8(pVariant));
		break;
	case VT_BSTR:
		LOGTRACE("VT_BSTR(%s)", V_BSTR(pVariant));
		break;
	case VT_BOOL:
		if (V_BOOL(pVariant)) {
			LOGTRACE("VT_BOOL(VARIANT_TRUE)");
		} else {
			LOGTRACE("VT_BOOL(VARIANT_FALSE)");
		}
		break;
	case VT_RECORD:
	{
		// Find the type of the record by comparing the GUID with known GUIDs for Local and Multi
		IID guid;
		HRESULT hr = V_RECORDINFO(pVariant)->GetGuid(&guid);
		if (FAILED(hr)) {
			_com_error err(hr);
			LOGERROR("VT_RECORD(recordinfo->GetGuid returned: %s)", err.ErrorMessage());
		}
		
		LOGTRACE("VT_RECORD(%x-%x-%x-%x)", guid.Data1, guid.Data2, guid.Data3, guid.Data4);
			//if (guid == IID_XL4JMULTIREFERENCE) { // if the IRecordInfo type is an XL4JMULTIREFERENCE
			//	XL4JMULTIREFERENCE *pMultiRef = static_cast<XL4JMULTIREFERENCE *>V_RECORD(pVariant);
			//	SAFEARRAY *psa = pMultiRef->refs;
			//	long cRanges;
			//	SafeArrayGetUBound(psa, 1, &cRanges);
			//	cRanges++; // size = upper bound + 1
			//	XL4JREFERENCE *pRef;
			//	SafeArrayAccessData(psa, (PVOID*)(&pRef)); // access raw array ptr
			//											   // Create a Java XLMultiReference from the array elements and the sheet id
			//	jobject joResult = pJniCache->XLMultiReference_of(pEnv, pMultiRef->idSheet, pRef, cRanges);
			//	SafeArrayUnaccessData(psa);
			//	return joResult;
			//} else if (guid == IID_XL4JREFERENCE) { // if the IRecordInfo type is an XL4JREFERENCE
			//	XL4JREFERENCE *pRef = static_cast<XL4JREFERENCE *>(V_RECORD(pVariant));
			//	// Create a Java XLLocalReference from it
			//	return pJniCache->XLLocalReference_of(pEnv, pRef);
			//} else {
			//	LOGERROR("unrecognised RECORDINFO guid %x", guid);
			//	return NULL;
			//}
	} break;
	case VT_UI1: // UI1 encodes an error number, convert to an XLError object
		LOGTRACE("VT_UI1(%d)", V_UI1(pVariant));
		break;
	case VT_ARRAY:
	{
		SAFEARRAY *psa = V_ARRAY(pVariant);
		LOGTRACE_SAFEARRAY(psa);
	} break;
	case VT_NULL:
		LOGTRACE("VT_NULL");
		break;
	case VT_EMPTY:
		LOGTRACE("VT_EMPTY");
		break;
	case VT_INT:
		LOGTRACE("VT_INT(%d)", V_INT(pVariant));
		break;
	case VT_INT_PTR:
		LOGTRACE("VT_INT_PTR(%p)", V_INT_PTR(pVariant));
		break;
	default:
		LOGERROR("Unrecognised VARIANT type %d", pVariant->vt);
		break;
	}
}

void Debug::appendExceptionTraceMessages (
	JNIEnv&      a_jni_env,
	std::string& a_error_msg,
	jthrowable   a_exception,
	jmethodID    a_mid_throwable_getCause,
	jmethodID    a_mid_throwable_getStackTrace,
	jmethodID    a_mid_throwable_toString,
	jmethodID    a_mid_frame_toString) {
	// Get the array of StackTraceElements.
	jobjectArray frames =
		(jobjectArray)a_jni_env.CallObjectMethod (
		a_exception,
		a_mid_throwable_getStackTrace);
	jsize frames_length = a_jni_env.GetArrayLength (frames);

	// Add Throwable.toString() before descending
	// stack trace messages.
	if (0 != frames) {
		jstring msg_obj =
			(jstring)a_jni_env.CallObjectMethod (a_exception,
			a_mid_throwable_toString);
		const char* msg_str = a_jni_env.GetStringUTFChars (msg_obj, 0);

		// If this is not the top-of-the-trace then
		// this is a cause.
		if (!a_error_msg.empty ()) {
			a_error_msg += "\nCaused by: ";
			a_error_msg += msg_str;
		} else {
			a_error_msg = msg_str;
		}

		a_jni_env.ReleaseStringUTFChars (msg_obj, msg_str);
		a_jni_env.DeleteLocalRef (msg_obj);
	}

	// Append stack trace messages if there are any.
	if (frames_length > 0) {
		jsize i = 0;
		for (i = 0; i < frames_length; i++) {
			// Get the string returned from the 'toString()'
			// method of the next frame and append it to
			// the error message.
			jobject frame = a_jni_env.GetObjectArrayElement (frames, i);
			jstring msg_obj =
				(jstring)a_jni_env.CallObjectMethod (frame,
				a_mid_frame_toString);

			const char* msg_str = a_jni_env.GetStringUTFChars (msg_obj, 0);

			a_error_msg += "\n    ";
			a_error_msg += msg_str;

			a_jni_env.ReleaseStringUTFChars (msg_obj, msg_str);
			a_jni_env.DeleteLocalRef (msg_obj);
			a_jni_env.DeleteLocalRef (frame);
		}
	}

	// If 'a_exception' has a cause then append the
	// stack trace messages from the cause.
	if (0 != frames) {
		jthrowable cause =
			(jthrowable)a_jni_env.CallObjectMethod (
			a_exception,
			a_mid_throwable_getCause);
		if (0 != cause) {
			appendExceptionTraceMessages (a_jni_env,
				a_error_msg,
				cause,
				a_mid_throwable_getCause,
				a_mid_throwable_getStackTrace,
				a_mid_throwable_toString,
				a_mid_frame_toString);
		}
	}
}

void Debug::printException (JNIEnv *pEnv, jthrowable exception) {
	jclass throwable_class = pEnv->FindClass ("java/lang/Throwable");
	jmethodID mid_throwable_getCause =
		pEnv->GetMethodID (throwable_class,
		"getCause",
		"()Ljava/lang/Throwable;");
	jmethodID mid_throwable_getStackTrace =
		pEnv->GetMethodID (throwable_class,
		"getStackTrace",
		"()[Ljava/lang/StackTraceElement;");
	jmethodID mid_throwable_toString =
		pEnv->GetMethodID (throwable_class,
		"toString",
		"()Ljava/lang/String;");

	jclass frame_class = pEnv->FindClass ("java/lang/StackTraceElement");
	jmethodID mid_frame_toString =
		pEnv->GetMethodID (frame_class,
		"toString",
		"()Ljava/lang/String;");
	std::string error_msg; // Could use ostringstream instead.
	appendExceptionTraceMessages (*pEnv,
		error_msg,
		exception,
		mid_throwable_getCause,
		mid_throwable_getStackTrace,
		mid_throwable_toString,
		mid_frame_toString);
	OutputDebugStringA (error_msg.c_str());
}

void Debug::SetLogTarget(LOGTARGET logTarget) {
	if (logTarget == LOGTARGET_FILE) {
		wchar_t buffer[MAX_PATH];
		HRESULT hr = FileUtils::GetTemporaryFileName(TEXT("xl4j-cpp.log"), buffer, MAX_PATH);
		if (SUCCEEDED(hr)) {
			OutputDebugStringW(TEXT("Full log path is:"));
			OutputDebugStringW(buffer);
			if (!_wfopen_s(&m_fdLogFile, buffer, _T("w"))) {
				OutputDebugStringW(TEXT("Could not open log file"));
				m_fdLogFile = nullptr;
			}
		} else {
			OutputDebugString(TEXT("Error getting temporary filename:"));
			_com_error err(hr);
			OutputDebugString(err.ErrorMessage());
			m_fdLogFile = nullptr;
		}
	} else /* if (logTarget == LOGTARGET_WINDEBUG) */ {
		if (m_fdLogFile) {
			fclose(m_fdLogFile);
		}
		m_fdLogFile = nullptr;
	}
	m_logTarget = logTarget;
}

//
// Usage: SetThreadName ((DWORD)-1, "MainThread");
//
const DWORD MS_VC_EXCEPTION = 0x406D1388;
#pragma pack(push,8)
typedef struct tagTHREADNAME_INFO {
	DWORD dwType; // Must be 0x1000.
	LPCSTR szName; // Pointer to name (in user addr space).
	DWORD dwThreadID; // Thread ID (-1=caller thread).
	DWORD dwFlags; // Reserved for future use, must be zero.
} THREADNAME_INFO;
#pragma pack(pop)
void Debug::SetThreadName (DWORD dwThreadID, const char* threadName) {
	THREADNAME_INFO info;
	info.dwType = 0x1000;
	info.szName = threadName;
	info.dwThreadID = dwThreadID;
	info.dwFlags = 0;
#pragma warning(push)
#pragma warning(disable: 6320 6322)
	__try {
		RaiseException (MS_VC_EXCEPTION, 0, sizeof (info) / sizeof (ULONG_PTR), (ULONG_PTR*)&info);
	} __except (EXCEPTION_EXECUTE_HANDLER) {
	}
#pragma warning(pop)
}

//void Debug::printXLOPER (XLOPER12 *oper) {
//	switch (oper->xltype) {
//	case xltypeStr: {
//		LOGTRACE ("XLOPER12: xltypeStr: %s", oper->val.str);
//	} break;
//	case xltypeNum: {
//		LOGTRACE ("XLOPER12: xltypeNum: %f", oper->val.num);
//	} break;
//	case xltypeNil: {
//		LOGTRACE ("XLOPER12: xltypeNil");
//	} break;
//	case xltypeRef: {
//		LOGTRACE ("XLOPER12: xltypeRef: sheetId=%d", oper->val.mref.idSheet);
//		for (int i = 0; i < oper->val.mref.lpmref->count; i++) {
//			LOGTRACE ("  rwFirst=%d,rwLast=%d,colFirst=%d,colLast=%d",
//				oper->val.mref.lpmref->reftbl[i].rwFirst,
//				oper->val.mref.lpmref->reftbl[i].rwLast,
//				oper->val.mref.lpmref->reftbl[i].colFirst,
//				oper->val.mref.lpmref->reftbl[i].colLast);
//		}
//	} break;
//	case xltypeMissing: {
//		LOGTRACE ("XLOPER12: xltypeMissing");
//	} break;
//	case xltypeSRef: {
//		LOGTRACE ("XLOPER12: cltypeSRef: rwFirst=%d,rwLast=%d,colFirst=%d,colLast=%d",
//				oper->val.sref.ref.rwFirst,
//				oper->val.sref.ref.rwLast,
//				oper->val.sref.ref.colFirst,
//				oper->val.sref.ref.colLast);
//	} break;
//	case xltypeInt: {
//		LOGTRACE ("XLOPER12: xltypeInt: %d", oper->val.w);
//	} break;
//	case xltypeErr: {
//		LOGTRACE ("XLOPER12: xltypeErr: %d", oper->val.err);
//	} break;
//	case xltypeBool: {
//		if (oper->val.xbool == FALSE) {
//			LOGTRACE ("XLOPER12: xltypeBool: FALSE");
//		} else {
//			LOGTRACE ("XLOPER12: xltypeBool: TRUE");
//		}
//	} break;
//	case xltypeBigData: {
//		LOGTRACE ("XLOPER12: xltypeBigData");
//	} break;
//	case xltypeMulti: {
//		RW cRows = oper->val.array.rows;
//		COL cCols = oper->val.array.columns;
//		LOGTRACE ("XLOPER12: xltypeMulti: cols=%d, rows=%d", cCols, cRows);
//		XLOPER12 *pXLOPER = oper->val.array.lparray;
//		for (RW j = 0; j < cRows; j++) {
//			for (COL i = 0; i < cCols; i++) {
//				printXLOPER (pXLOPER++);
//			}
//		}
//	} break;
//	default: {
//		LOGTRACE ("XLOPER12: Unrecognised XLOPER12 type %d", oper->xltype);
//	}
//
//	}
//}