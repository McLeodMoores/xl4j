/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

class CJniSequenceExecutor;

#include "JniSequence.h"

class CJniSequenceExecutor {
private:
	volatile long m_lRefCount;
	CJniSequence *m_pOwner;
	long m_cArgs;
	VARIANT *m_pArgs;
	long m_cResults;
	VARIANT *m_pResults;
	HANDLE m_hSemaphore;
	HRESULT m_hRunResult;
	~CJniSequenceExecutor ();
	void append_exception_trace_messages (
		JNIEnv&      a_jni_env,
		std::wstring& a_error_msg,
		jthrowable   a_exception,
		jmethodID    a_mid_throwable_getCause,
		jmethodID    a_mid_throwable_getStackTrace,
		jmethodID    a_mid_throwable_toString,
		jmethodID    a_mid_frame_toString);
public:
	CJniSequenceExecutor (CJniSequence *pOwner, long cArgs, VARIANT *pArgs, long cResults, VARIANT *pResults);
	HRESULT Run (JNIEnv *pEnv);
	HRESULT Wait ();
	void AddRef ();
	void Release ();
};

