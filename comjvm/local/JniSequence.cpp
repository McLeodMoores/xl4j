/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "JniSequence.h"
#include "Internal.h"

CJniSequence::CJniSequence (CJvm *pJvm)
	: m_lRefCount (1), m_pJvm (pJvm), m_cValue (0), m_cExecuting (0), m_cArgument (0), m_cResult (0) {
	IncrementActiveObjectCount ();
	pJvm->AddRef ();
	InitializeCriticalSection (&m_cs);
}

CJniSequence::~CJniSequence () {
	assert (m_lRefCount == 0);
	size_t c = m_ahSemaphore.size (), i;
	for (i= 0; i< c; i++) {
		CloseHandle (m_ahSemaphore[i]);
	}
	DeleteCriticalSection (&m_cs);
	m_pJvm->Release ();
	DecrementActiveObjectCount ();
}

/// <summary>Add an operation to the run queue.</summary>
///
/// <para>The caller must hold the critical section.</para>
///
/// <param name="operation">Operation to add</param>
/// <returns>S_OK if successful, an error code otherwise.</returns>
HRESULT CJniSequence::AddOperation (JniOperation operation) {
	try {
		m_aOperation.push_back (operation);
		return S_OK;
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}

/// <summary>Adds an operation to the run queue with a single parameter.</summary>
///
/// <para>The caller must hold the critical section.</para>
///
/// <param name="operation">Operation to add</param>
/// <param name="lParam">Parameter index</param>
/// <returns>S_OK if successful, an error code otherwise.</returns>
HRESULT CJniSequence::AddOperation (JniOperation operation, long lParam) {
	bool bOperation = false;
	try {
		m_aOperation.push_back (operation);
		bOperation = true;
		m_aParam.push_back (lParam);
		return S_OK;
	} catch (std::bad_alloc) {
		if (bOperation) m_aOperation.pop_back ();
		return E_OUTOFMEMORY;
	}
}

/// <summary>Adds an operation to the run queue with two parameters.</summary>
///
/// <para>The caller must hold the critical section.</para>
///
/// <param name="operation">Operation to add</param>
/// <param name="lParam1">First parameter index</param>
/// <param name="lParam2">Second parameter index</param>
/// <returns>S_OK if successful, an error code otherwise.</returns>
HRESULT CJniSequence::AddOperation (JniOperation operation, long lParam1, long lParam2) {
	bool bOperation = false;
	bool bParam = false;
	try {
		m_aOperation.push_back (operation);
		bOperation = true;
		m_aParam.push_back (lParam1);
		bParam = true;
		m_aParam.push_back (lParam2);
		return S_OK;
	} catch (std::bad_alloc) {
		if (bOperation) m_aOperation.pop_back ();
		if (bParam) m_aParam.pop_back ();
		return E_OUTOFMEMORY;
	}
}

/// <summary>Adds an operation to the run queue with three parameters.</summary>
///
/// <para>The caller must hold the critical section.</para>
///
/// <param name="operation">Operation to add</param>
/// <param name="lParam1">First parameter index</param>
/// <param name="lParam2">Second parameter index</param>
/// <param name="lParam3">Third parameter index</param>
/// <returns>S_OK if successful, an error code otherwise.</returns>
HRESULT CJniSequence::AddOperation (JniOperation operation, long lParam1, long lParam2, long lParam3) {
	bool bOperation = false;
	int cParam = 0;
	try {
		m_aOperation.push_back (operation);
		bOperation = true;
		m_aParam.push_back (lParam1);
		cParam++;
		m_aParam.push_back (lParam2);
		cParam++;
		m_aParam.push_back (lParam3);
		return S_OK;
	} catch (std::bad_alloc) {
		if (bOperation) m_aOperation.pop_back ();
		while (cParam-- > 0) m_aParam.pop_back ();
		return E_OUTOFMEMORY;
	}
}

/// <summary>Adds an operation to the run queue with four parameters.</summary>
///
/// <para>The caller must hold the critical section.</para>
///
/// <param name="operation">Operation to add</param>
/// <param name="lParam1">First parameter index</param>
/// <param name="lParam2">Second parameter index</param>
/// <param name="lParam3">Third parameter index</param>
/// <param name="lParam4">Fourth parameter index</param>
/// <returns>S_OK if successful, an error code otherwise.</returns>
HRESULT CJniSequence::AddOperation (JniOperation operation, long lParam1, long lParam2, long lParam3, long lParam4) {
	bool bOperation = false;
	int cParam = 0;
	try {
		m_aOperation.push_back (operation);
		bOperation = true;
		m_aParam.push_back (lParam1);
		cParam++;
		m_aParam.push_back (lParam2);
		cParam++;
		m_aParam.push_back (lParam3);
		cParam++;
		m_aParam.push_back (lParam4);
		return S_OK;
	} catch (std::bad_alloc) {
		if (bOperation) m_aOperation.pop_back ();
		while (cParam-- > 0) m_aParam.pop_back ();
		return E_OUTOFMEMORY;
	}
}

/// <summary>Adds an operation to the run queue with five parameters.</summary>
///
/// <para>The caller must hold the critical section.</para>
///
/// <param name="operation">Operation to add</param>
/// <param name="lParam1">First parameter index</param>
/// <param name="lParam2">Second parameter index</param>
/// <param name="lParam3">Third parameter index</param>
/// <param name="lParam4">Fourth parameter index</param>
/// <param name="lParam5">Fifth parameter index</param>
/// <returns>S_OK if successful, an error code otherwise.</returns>
HRESULT CJniSequence::AddOperation (JniOperation operation, long lParam1, long lParam2, long lParam3, long lParam4, long lParam5) {
	bool bOperation = false;
	int cParam = 0;
	try {
		m_aOperation.push_back (operation);
		bOperation = true;
		m_aParam.push_back (lParam1);
		cParam++;
		m_aParam.push_back (lParam2);
		cParam++;
		m_aParam.push_back (lParam3);
		cParam++;
		m_aParam.push_back (lParam4);
		cParam++;
		m_aParam.push_back (lParam5);
		return S_OK;
	} catch (std::bad_alloc) {
		if (bOperation) m_aOperation.pop_back ();
		while (cParam-- > 0) m_aParam.pop_back ();
		return E_OUTOFMEMORY;
	}
}

/// <summary>Loads a constant into the value buffer.</summary>
///
/// <para>The caller must hold the critical section.</para>
///
/// <param name="value">Constant to load</param>
/// <param name="plRef">Receives the loaded value index</param>
/// <returns>S_OK if successful, an error code otherwise</returns>
HRESULT CJniSequence::LoadConstant (CJniValue &value, long *plRef) {
	HRESULT hr = value.load (m_aConstant);
	if (SUCCEEDED (hr)) {
		hr = AddOperation (JniOperation::io_LoadConstant);
		if (SUCCEEDED (hr)) {
			*plRef = m_cValue++;
		}
	}
	return hr;
}

/// <summary>Marks the start of an execution</summary>
///
/// <returns>Notification semaphore</returns>
HANDLE CJniSequence::BeginExecution () {
	EnterCriticalSection (&m_cs);
	m_cExecuting++;
	HANDLE hSemaphore;
	if (m_ahSemaphore.size () > 0) {
		hSemaphore = m_ahSemaphore[m_ahSemaphore.size () - 1];
		m_ahSemaphore.pop_back ();
	} else {
		hSemaphore = CreateSemaphore (NULL, 0, 1, NULL);
	}
	LeaveCriticalSection (&m_cs);
	return hSemaphore;
}

/// <summary>Marks the end of an execution</summary>
///
/// <param name="hSemaphore">Notification semaphore returned by BeginExecution</param>
void CJniSequence::EndExecution (HANDLE hSemaphore) {
	EnterCriticalSection (&m_cs);
	m_cExecuting--;
	try {
		m_ahSemaphore.push_back (hSemaphore);
	} catch (std::bad_alloc) {
		CloseHandle (hSemaphore);
	}
	LeaveCriticalSection (&m_cs);
}

HRESULT STDMETHODCALLTYPE CJniSequence::QueryInterface (
	/* [in] */ REFIID riid,
	/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject
	) {
	if (!ppvObject) return E_POINTER;
	if (riid == IID_IUnknown) {
		*ppvObject = static_cast<IUnknown*> (this);
	} else if (riid == IID_IJniSequence) {
		*ppvObject = static_cast<IJniSequence*> (this);
	} else {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
	AddRef ();
	return S_OK;
}

ULONG STDMETHODCALLTYPE CJniSequence::AddRef () {
	return InterlockedIncrement (&m_lRefCount);
}

ULONG STDMETHODCALLTYPE CJniSequence::Release () {
	ULONG lResult = InterlockedDecrement (&m_lRefCount);
	if (!lResult) delete this;
	return lResult;
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CJniSequence::get_Arguments ( 
    /* [retval][out] */ long *pcArgs
	) {
	if (!pcArgs) return E_POINTER;
	EnterCriticalSection (&m_cs);
	*pcArgs = m_cArgument;
	LeaveCriticalSection (&m_cs);
	return S_OK;
}

/* [propget] */ HRESULT STDMETHODCALLTYPE CJniSequence::get_Results ( 
    /* [retval][out] */ long *pcResults
	) {
	if (!pcResults) return E_POINTER;
	EnterCriticalSection (&m_cs);
	*pcResults = (long)m_cResult;
	LeaveCriticalSection (&m_cs);
	return S_OK;
}

static HRESULT APIENTRY _Execute (LPVOID lpData, JNIEnv *pEnv) {
	CJniSequenceExecutor *pExecutor = (CJniSequenceExecutor*)lpData;
	HRESULT hr = pExecutor->Run (pEnv);
	pExecutor->Release ();
	return hr;
}

HRESULT STDMETHODCALLTYPE CJniSequence::Execute ( 
    /* [in] */ long cArgs,
    /* [size_is][in] */ VARIANT *aArgs,
    /* [in] */ long cResults,
    /* [size_is][out] */ VARIANT *aResults
	) {
	if (cArgs && !aArgs) return E_POINTER;
	if (cResults && !aResults) return E_POINTER;
	HRESULT hr;
	try {
		long l;
		for (l = 0; l < cResults; l++) {
			VariantClear (aResults + l);
		}
		CJniSequenceExecutor *pExecutor = new CJniSequenceExecutor (this, cArgs, aArgs, cResults, aResults); // RC1
		pExecutor->AddRef (); // RC2
		hr = m_pJvm->Execute (_Execute, pExecutor);
		if (SUCCEEDED (hr)) {
			// The executor will release RC2
			hr = pExecutor->Wait ();
		} else {
			// Release RC2
			pExecutor->Release ();
		}
		// Release RC1
		pExecutor->Release ();
	} catch (std::bad_alloc) {
		hr = E_OUTOFMEMORY;
	}
	return hr;
}

#define __JNI_OPERATION \
	HRESULT hr; \
	EnterCriticalSection (&m_cs); \
	if (m_cExecuting) { \
		hr = E_NOT_VALID_STATE; \
	} else
#define __RETURN_HR \
	LeaveCriticalSection (&m_cs); \
	return hr

#define JNI_METHOD_IMPL_V(_name_) \
HRESULT STDMETHODCALLTYPE CJniSequence::_name_ () { \
	__JNI_OPERATION { \
		hr = AddOperation (JniOperation::_name_); \
	} \
	__RETURN_HR; \
}

#define JNI_METHOD_IMPL_0(_name_, _result_) \
HRESULT STDMETHODCALLTYPE CJniSequence::_name_ ( \
    /* [out] */ long *_result_ \
	) { \
	if (!_result_) return E_POINTER; \
	__JNI_OPERATION { \
		hr = AddOperation (JniOperation::_name_); \
		if (SUCCEEDED (hr)) { \
			*_result_ = m_cValue++; \
		} \
	} \
	__RETURN_HR; \
}

#define JNI_METHOD_IMPL_V1(_name_, _arg1_) \
HRESULT STDMETHODCALLTYPE CJniSequence::_name_ ( \
	/* [in] */ long _arg1_) { \
	__JNI_OPERATION { \
		hr = AddOperation (JniOperation::_name_, _arg1_); \
	} \
	__RETURN_HR; \
}

#define JNI_METHOD_IMPL_1(_name_, _arg1_, _result_) \
HRESULT STDMETHODCALLTYPE CJniSequence::_name_ ( \
	/* [in] */ long _arg1_, \
    /* [out] */ long *_result_ \
	) { \
	if (!_result_) return E_POINTER; \
	__JNI_OPERATION { \
		hr = AddOperation (JniOperation::_name_, _arg1_); \
		if (SUCCEEDED (hr)) { \
			*_result_ = m_cValue++; \
		} \
	} \
	__RETURN_HR; \
}

#define JNI_METHOD_IMPL_V2(_name_, _arg1_, _arg2_) \
HRESULT STDMETHODCALLTYPE CJniSequence::_name_ ( \
	/* [in] */ long _arg1_, \
	/* [in] */ long _arg2_) { \
	__JNI_OPERATION { \
		hr = AddOperation (JniOperation::_name_, _arg1_, _arg2_); \
	} \
	__RETURN_HR; \
}

#define JNI_METHOD_IMPL_2(_name_, _arg1_, _arg2_, _result_) \
HRESULT STDMETHODCALLTYPE CJniSequence::_name_ ( \
	/* [in] */ long _arg1_, \
	/* [in] */ long _arg2_, \
    /* [out] */ long *_result_ \
	) { \
	if (!_result_) return E_POINTER; \
	__JNI_OPERATION { \
		hr = AddOperation (JniOperation::_name_, _arg1_, _arg2_); \
		if (SUCCEEDED (hr)) { \
			*_result_ = m_cValue++; \
		} \
	} \
	__RETURN_HR; \
}

#define JNI_METHOD_IMPL_V3(_name_, _arg1_, _arg2_, _arg3_) \
HRESULT STDMETHODCALLTYPE CJniSequence::_name_ ( \
	/* [in] */ long _arg1_, \
	/* [in] */ long _arg2_, \
	/* [in] */ long _arg3_) { \
	__JNI_OPERATION { \
		hr = AddOperation (JniOperation::_name_, _arg1_, _arg2_, _arg3_); \
	} \
	__RETURN_HR; \
}

#define JNI_METHOD_IMPL_3(_name_, _arg1_, _arg2_, _arg3_, _result_) \
HRESULT STDMETHODCALLTYPE CJniSequence::_name_ ( \
	/* [in] */ long _arg1_, \
	/* [in] */ long _arg2_, \
	/* [in] */ long _arg3_, \
    /* [out] */ long *_result_ \
	) { \
	if (!_result_) return E_POINTER; \
	__JNI_OPERATION { \
		hr = AddOperation (JniOperation::_name_, _arg1_, _arg2_, _arg3_); \
		if (SUCCEEDED (hr)) { \
			*_result_ = m_cValue++; \
		} \
	} \
	__RETURN_HR; \
}

#define JNI_METHOD_IMPL_V4(_name_, _arg1_, _arg2_, _arg3_, _arg4_) \
HRESULT STDMETHODCALLTYPE CJniSequence::_name_ ( \
	/* [in] */ long _arg1_, \
	/* [in] */ long _arg2_, \
	/* [in] */ long _arg3_, \
	/* [in] */ long _arg4_) { \
	__JNI_OPERATION { \
		hr = AddOperation (JniOperation::_name_, _arg1_, _arg2_, _arg3_, _arg4_); \
	} \
	__RETURN_HR; \
}

#define JNI_METHOD_IMPL_4(_name_, _arg1_, _arg2_, _arg3_, _arg4_, _result_) \
HRESULT STDMETHODCALLTYPE CJniSequence::_name_ ( \
	/* [in] */ long _arg1_, \
	/* [in] */ long _arg2_, \
	/* [in] */ long _arg3_, \
	/* [in] */ long _arg4_, \
    /* [out] */ long *_result_ \
	) { \
	if (!_result_) return E_POINTER; \
	__JNI_OPERATION { \
		hr = AddOperation (JniOperation::_name_, _arg1_, _arg2_, _arg3_, _arg4_); \
		if (SUCCEEDED (hr)) { \
			*_result_ = m_cValue++; \
		} \
	} \
	__RETURN_HR; \
}

#define JNI_METHOD_IMPL_V5(_name_, _arg1_, _arg2_, _arg3_, _arg4_, _arg5_) \
HRESULT STDMETHODCALLTYPE CJniSequence::_name_ ( \
	/* [in] */ long _arg1_, \
	/* [in] */ long _arg2_, \
	/* [in] */ long _arg3_, \
	/* [in] */ long _arg4_, \
	/* [in] */ long _arg5_) { \
	__JNI_OPERATION { \
		hr = AddOperation (JniOperation::_name_, _arg1_, _arg2_, _arg3_, _arg4_, _arg5_); \
	} \
	__RETURN_HR; \
}

HRESULT STDMETHODCALLTYPE CJniSequence::Argument ( 
    /* [retval][out] */ long *plValueRef
	) {
	if (!plValueRef) return E_POINTER;
	__JNI_OPERATION {
		hr = AddOperation (JniOperation::io_LoadArgument);
		if (SUCCEEDED (hr)) {
			m_cArgument++;
			*plValueRef = m_cValue++;
		}
	}
	__RETURN_HR;
}

HRESULT STDMETHODCALLTYPE CJniSequence::Result ( 
    /* [in] */ long lValueRef
	) {
	__JNI_OPERATION {
		if ((lValueRef < 0) || (lValueRef >= (long)m_cValue)) {
			hr = E_INVALIDARG;
		} else {
			hr = AddOperation (JniOperation::io_StoreResult, lValueRef);
			if (SUCCEEDED (hr)) {
				m_cResult++;
			}
		}
	}
	__RETURN_HR;
}

HRESULT STDMETHODCALLTYPE CJniSequence::StringConstant (
    /* [in] */ BSTR bstr,
    /* [retval][out] */ long *plValueRef
	) {
	if (!bstr) return E_POINTER;
	if (!plValueRef) return E_POINTER;
	__JNI_OPERATION {
		BSTR bstrCopy = SysAllocStringLen (bstr, SysStringLen (bstr));
		if (bstrCopy) {
			CJniValue constant (bstrCopy);
			hr = LoadConstant (constant, plValueRef);
		} else {
			hr = E_OUTOFMEMORY;
		}
	}
	__RETURN_HR;
}

HRESULT STDMETHODCALLTYPE CJniSequence::ByteConstant (
    /* [in] */ byte nValue,
    /* [retval][out] */ long *plValueRef
	) {
	if (!plValueRef) return E_POINTER;
	__JNI_OPERATION {
		CJniValue constant ((jbyte)nValue);
		hr = LoadConstant (constant, plValueRef);
	}
	__RETURN_HR;
}

HRESULT STDMETHODCALLTYPE CJniSequence::ShortConstant ( 
    /* [in] */ short nValue,
    /* [retval][out] */ long *plValueRef
	) {
	if (!plValueRef) return E_POINTER;
	__JNI_OPERATION {
		CJniValue constant ((jshort)nValue);
		hr = LoadConstant (constant, plValueRef);
	}
	__RETURN_HR;
}

HRESULT STDMETHODCALLTYPE CJniSequence::IntConstant ( 
    /* [in] */ long nValue,
    /* [retval][out] */ long *plValueRef
	) {
	if (!plValueRef) return E_POINTER;
	__JNI_OPERATION {
		CJniValue constant (nValue);
		hr = LoadConstant (constant, plValueRef);
	}
	__RETURN_HR;
}

HRESULT STDMETHODCALLTYPE CJniSequence::LongConstant (
    /* [in] */ hyper nValue,
    /* [retval][out] */ long *plValueRef
	) {
	if (!plValueRef) return E_POINTER;
	__JNI_OPERATION {
		CJniValue constant (nValue);
		hr = LoadConstant (constant, plValueRef);
	}
	__RETURN_HR;
}

HRESULT STDMETHODCALLTYPE CJniSequence::FloatConstant (
    /* [in] */ float fValue,
    /* [retval][out] */ long *plValueRef
	) {
	if (!plValueRef) return E_POINTER;
	__JNI_OPERATION {
		CJniValue constant (fValue);
		hr = LoadConstant (constant, plValueRef);
	}
	__RETURN_HR;
}

HRESULT STDMETHODCALLTYPE CJniSequence::DoubleConstant (
    /* [in] */ double dValue,
    /* [retval][out] */ long *plValueRef
	) {
	if (!plValueRef) return E_POINTER;
	__JNI_OPERATION {
		CJniValue constant (dValue);
		hr = LoadConstant (constant, plValueRef);
	}
	__RETURN_HR;
}

HRESULT STDMETHODCALLTYPE CJniSequence::BooleanConstant ( 
    /* [in] */ BOOL fValue,
    /* [retval][out] */ long *plValueRef
	) {
	if (!plValueRef) return E_POINTER;
	__JNI_OPERATION {
		CJniValue constant;
		constant.put_jboolean (fValue ? JNI_TRUE : JNI_FALSE);
		hr = LoadConstant (constant, plValueRef);
	}
	__RETURN_HR;
}

JNI_METHOD_IMPL_0 (jni_GetVersion, plValueRef)
JNI_METHOD_IMPL_4 (jni_DefineClass, lNameRef, lLoaderRef, lBufRef, lLenRef, plClassRef)
JNI_METHOD_IMPL_1 (jni_FindClass, lNameRef, plClassRef)
JNI_METHOD_IMPL_1 (jni_FromReflectedMethod, lMethodRef, plMethodIDRef)
JNI_METHOD_IMPL_1 (jni_FromReflectedField, lFieldRef, plFieldIDRef)
JNI_METHOD_IMPL_3 (jni_ToReflectedMethod, lClsRef, lMethodIDRef, lIsStaticRef, plObjectRef)
JNI_METHOD_IMPL_1 (jni_GetSuperclass, lSubRef, plClassRef)
JNI_METHOD_IMPL_2 (jni_IsAssignableFrom, lSubRef, lSupRef, plBooleanRef)
JNI_METHOD_IMPL_3 (jni_ToReflectedField, lClsRef, lFieldIDRef, lIsStaticRef, plObjectRef)
JNI_METHOD_IMPL_1 (jni_Throw, lObjRef, plIntRef)
JNI_METHOD_IMPL_2 (jni_ThrowNew, lClassRef, lMsgRef, plIntRef)
JNI_METHOD_IMPL_0 (jni_ExceptionOccurred, plThrowableRef)
JNI_METHOD_IMPL_V (jni_ExceptionDescribe)
JNI_METHOD_IMPL_V (jni_ExceptionClear)
JNI_METHOD_IMPL_V1 (jni_FatalError, lMsgRef)
JNI_METHOD_IMPL_1 (jni_PushLocalFrame, lCapacityRef, plIntRef)
JNI_METHOD_IMPL_1 (jni_PopLocalFrame, lResultRef, plObjectRef)
JNI_METHOD_IMPL_1 (jni_NewGlobalRef, lObjRef, plObjectRef)
JNI_METHOD_IMPL_V1 (jni_DeleteGlobalRef, lGrefRef)
JNI_METHOD_IMPL_V1 (jni_DeleteLocalRef, lObjRef)
JNI_METHOD_IMPL_2 (jni_IsSameObject, lObj1Ref, lObj2Ref, plBooleanRef)
JNI_METHOD_IMPL_1 (jni_NewLocalRef, lRefRef, plObjectRef)
JNI_METHOD_IMPL_1 (jni_EnsureLocalCapacity, lCapacityRet, plIntRef)
JNI_METHOD_IMPL_1 (jni_AllocObject, lClassRef, plObjectRef)

HRESULT STDMETHODCALLTYPE CJniSequence::jni_NewObject ( 
    /* [in] */ long lClassRef,
    /* [in] */ long lMethodIDRef,
    /* [in] */ long cArgs,
    /* [size_is][in] */ long *alArgRefs,
    /* [retval][out] */ long *plObjectRef
	) {
	if (cArgs && !alArgRefs) return E_POINTER;
	if (!plObjectRef) return E_POINTER;
	__JNI_OPERATION {
		hr = AddOperation (JniOperation::jni_NewObject, lClassRef, lMethodIDRef, cArgs);
		if (SUCCEEDED (hr)) {
			long cParam = 0;
			try {
				do {
					m_aParam.push_back (alArgRefs[cParam]);
					cParam++;
				} while (cParam < cArgs);
				*plObjectRef = m_cValue++;
			} catch (std::bad_alloc) {
				while (cParam-- > 0) m_aParam.pop_back ();
				hr = E_OUTOFMEMORY;
			}
		}
	}
	__RETURN_HR;
}

JNI_METHOD_IMPL_1 (jni_GetObjectClass, lObjRef, plClassRef)
JNI_METHOD_IMPL_2 (jni_IsInstanceOf, lObjRef, lClassRef, plBooleanRef)
JNI_METHOD_IMPL_3 (jni_GetMethodID, lClassRef, lNameRef, lSigRef, plMethodIDRef)

HRESULT STDMETHODCALLTYPE CJniSequence::jni_CallMethod ( 
	/* [in] */ long lType,
    /* [in] */ long lObjRef,
    /* [in] */ long lMethodIDRef,
    /* [in] */ long cArgs,
    /* [size_is][in] */ long *alArgRefs,
    /* [retval][out] */ long *plResultRef
	) {
	if (cArgs && !alArgRefs) return E_POINTER;
	if (!plResultRef) return E_POINTER;
	__JNI_OPERATION {
		hr = AddOperation (JniOperation::jni_CallMethod, lType, lObjRef, lMethodIDRef);
		if (SUCCEEDED (hr)) {
			long cParam = 0;
			try {
				do {
					m_aParam.push_back (alArgRefs[cParam]);
					cParam++;
				} while (cParam < cArgs);
				*plResultRef = m_cValue++;
			} catch (std::bad_alloc) {
				while (cParam-- > 0) m_aParam.pop_back ();
				hr = E_OUTOFMEMORY;
			}
		}
	}
	__RETURN_HR;
}

HRESULT STDMETHODCALLTYPE CJniSequence::jni_CallNonVirtualMethod ( 
	/* [in] */ long lType,
    /* [in] */ long lObjRef,
    /* [in] */ long lClassRef,
    /* [in] */ long lMethodIDRef,
    /* [in] */ long cArgs,
    /* [size_is][in] */ long *alArgRefs,
    /* [retval][out] */ long *plResultRef
	) {
	if (cArgs && !alArgRefs) return E_POINTER;
	if (!plResultRef) return E_POINTER;
	__JNI_OPERATION {
		hr = AddOperation (JniOperation::jni_CallNonVirtualMethod, lType, lObjRef, lClassRef, lMethodIDRef);
		if (SUCCEEDED (hr)) {
			long cParam = 0;
			try {
				do {
					m_aParam.push_back (alArgRefs[cParam]);
					cParam++;
				} while (cParam < cArgs);
				*plResultRef = m_cValue++;
			} catch (std::bad_alloc) {
				while (cParam-- > 0) m_aParam.pop_back ();
				hr = E_OUTOFMEMORY;
			}
		}
	}
	__RETURN_HR;
}

JNI_METHOD_IMPL_3 (jni_GetFieldID, lClassRef, lNameRef, lSigRef, plFieldIDRef)
JNI_METHOD_IMPL_3 (jni_GetField, lClassRef, lNameRef, lSigRef, plFieldIDRef)
JNI_METHOD_IMPL_V4 (jni_SetField, lType, lObjRef, lFieldIDRef, lValueRef)
JNI_METHOD_IMPL_3 (jni_GetStaticMethodID, lClassRef, lNameRef, lSigRef, plMethodIDRef)

HRESULT STDMETHODCALLTYPE CJniSequence::jni_CallStaticMethod ( 
	/* [in] */ long lType,
    /* [in] */ long lClassRef,
    /* [in] */ long lMethodIDRef,
    /* [in] */ long cArgs,
    /* [size_is][in] */ long *alArgsRef,
    /* [retval][out] */ long *plResultRef
	) {
	if (cArgs && !alArgsRef) return E_POINTER;
	if (!plResultRef) return E_POINTER;
	__JNI_OPERATION {
		hr = AddOperation (JniOperation::jni_CallStaticMethod, lType, lClassRef, lMethodIDRef);
		if (SUCCEEDED (hr)) {
			long cParam = 0;
			try {
				do {
					m_aParam.push_back (alArgsRef[cParam]);
					cParam++;
				} while (cParam < cArgs);
				*plResultRef = m_cValue++;
			} catch (std::bad_alloc) {
				while (cParam-- > 0) m_aParam.pop_back ();
				hr = E_OUTOFMEMORY;
			}
		}
	}
	__RETURN_HR;
}

JNI_METHOD_IMPL_3 (jni_GetStaticFieldID, lClassRef, lNameRef, lSigRef, plFieldIDRef)
JNI_METHOD_IMPL_3 (jni_GetStaticField, lType, lClassRef, lFieldIDRef, plValueRef)
JNI_METHOD_IMPL_V4 (jni_SetStaticField, lType, lClassRef, lFieldIDRef, lValueRef)
JNI_METHOD_IMPL_2 (jni_NewString, lUnicodeRef, lSizeRef, plStringRef)
JNI_METHOD_IMPL_1 (jni_GetStringLength, lStrRef, plSizeRef)

HRESULT STDMETHODCALLTYPE CJniSequence::jni_GetStringChars ( 
    /* [in] */ long lStrRef,
    /* [optional][out] */ long *plIsCopyRef,
    /* [retval][out] */ long *plCharRef
	) {
	if (!plCharRef) return E_POINTER;
	__JNI_OPERATION {
		hr = AddOperation (JniOperation::jni_GetStringChars, lStrRef, plIsCopyRef ? m_cValue : -1);
		if (SUCCEEDED (hr)) {
			if (plIsCopyRef) *plIsCopyRef = m_cValue++;
			*plCharRef = m_cValue++;
		}
	}
	__RETURN_HR;
}

JNI_METHOD_IMPL_V2 (jni_ReleaseStringChars, lStrRef, lCharsRef)
JNI_METHOD_IMPL_1 (jni_NewStringUTF, lUtfRef, plStringRef)
JNI_METHOD_IMPL_1 (jni_GetStringUTFLength, lStrRef, plSizeRef)

HRESULT STDMETHODCALLTYPE CJniSequence::jni_GetStringUTFChars ( 
    /* [in] */ long lStrRef,
    /* [optional][out] */ long *plIsCopyRef,
    /* [retval][out] */ long *plCharRef
	) {
	if (!plCharRef) return E_POINTER;
	__JNI_OPERATION {
		hr = AddOperation (JniOperation::jni_GetStringUTFChars, lStrRef, plIsCopyRef ? m_cValue : -1);
		if (SUCCEEDED (hr)) {
			if (plIsCopyRef) *plIsCopyRef = m_cValue++;
			*plCharRef = m_cValue++;
		}
	}
	__RETURN_HR;
}

JNI_METHOD_IMPL_V2 (jni_ReleaseStringUTFChars, lStrRef, lCharsRef)
JNI_METHOD_IMPL_1 (jni_GetArrayLength, lArrayRef, plSizeRef)
JNI_METHOD_IMPL_3 (jni_NewObjectArray, lLenRef, lClassRef, lInitRef, plObjectArrayRef)
JNI_METHOD_IMPL_2 (jni_GetObjectArrayElement, lArrayRef, lIndexRef, plObjectRef)
JNI_METHOD_IMPL_V3 (jni_SetObjectArrayElement, lArrayRef, lIndexRef, lValRef)
JNI_METHOD_IMPL_2 (jni_NewArray, lType, lLenRef, plArrayRef)

HRESULT STDMETHODCALLTYPE CJniSequence::jni_GetArrayElements ( 
	/* [in] */ long lType,
    /* [in] */ long lArrayRef,
    /* [optional][out] */ long *plIsCopyRef,
    /* [retval][out] */ long *plValueRef
	) {
	if (!plValueRef) return E_POINTER;
	__JNI_OPERATION {
		hr = AddOperation (JniOperation::jni_GetArrayElements, lType, lArrayRef, plIsCopyRef ? m_cValue : -1);
		if (SUCCEEDED (hr)) {
			if (plIsCopyRef) *plIsCopyRef = m_cValue++;
			*plValueRef = m_cValue++;
		}
	}
	__RETURN_HR;
}

JNI_METHOD_IMPL_V4 (jni_ReleaseArrayElements, lType, lArrayRef, lElemsRef, lModeRef)
JNI_METHOD_IMPL_V5 (jni_GetArrayRegion, lType, lArrayRef, lStartRef, lLRef, lBufRef)
JNI_METHOD_IMPL_V5 (jni_SetArrayRegion, lType, lArrayRef, lStartRef, lLRef, lBufRef)
JNI_METHOD_IMPL_3 (jni_RegisterNatives, lClassRef, lMethodsRef, lNMethodsRef, plIntRef)
JNI_METHOD_IMPL_1 (jni_UnregisterNatives, lClassRef, plIntRef)
JNI_METHOD_IMPL_1 (jni_MonitorEntry, lObjRef, plIntRef)
JNI_METHOD_IMPL_1 (jni_MonitorExit, lObjRef, plIntRef)
JNI_METHOD_IMPL_V4 (jni_GetStringRegion, lStrRef, lStartRef, lLenRef, lBufRef)
JNI_METHOD_IMPL_V4 (jni_GetStringUTFRegion, lStrRef, lStartRef, lLenRef, lBufRef)

HRESULT STDMETHODCALLTYPE CJniSequence::jni_GetPrimitiveArrayCritical ( 
    /* [in] */ long lArrayRef,
    /* [optional][out] */ long *plIsCopyRef,
    /* [retval][out] */ long *plVoidRef
	) {
	if (!plVoidRef) return E_POINTER;
	__JNI_OPERATION {
		hr = AddOperation (JniOperation::jni_GetPrimitiveArrayCritical, lArrayRef, plIsCopyRef ? m_cValue : -1);
		if (SUCCEEDED (hr)) {
			if (plIsCopyRef) *plIsCopyRef = m_cValue++;
			*plVoidRef = m_cValue++;
		}
	}
	__RETURN_HR;
}

JNI_METHOD_IMPL_V3 (jni_ReleasePrimitiveArrayCritical, lArrayRef, lCArrayRef, lModeRef)

HRESULT STDMETHODCALLTYPE CJniSequence::jni_GetStringCritical ( 
    /* [in] */ long lStringRef,
    /* [optional][out] */ long *plIsCopyRef,
    /* [retval][out] */ long *plCharRef
	) {
	if (!plCharRef) return E_POINTER;
	__JNI_OPERATION {
		hr = AddOperation (JniOperation::jni_GetStringCritical, lStringRef, plIsCopyRef ? m_cValue : -1);
		if (SUCCEEDED (hr)) {
			if (plIsCopyRef) *plIsCopyRef = m_cValue++;
			*plCharRef = m_cValue++;
		}
	}
	__RETURN_HR;
}

JNI_METHOD_IMPL_V2 (jni_ReleaseStringCritical, lStringRef, lCStringRef)
JNI_METHOD_IMPL_1 (jni_NewWeakGlobalRef, lObjRef, plWeakRef)
JNI_METHOD_IMPL_V1 (jni_DeleteWeakGlobalRef, lRefRef)
JNI_METHOD_IMPL_0 (jni_ExceptionCheck, plBooleanRef)
JNI_METHOD_IMPL_2 (jni_NewDirectByteBuffer, lAddressRef, lCapacityRef, plObjectRef)
JNI_METHOD_IMPL_1 (jni_GetDirectBufferAddress, lBufRef, plVoidRef)
JNI_METHOD_IMPL_1 (jni_GetDirectBufferCapacity, lBufRef, plLongRef)
JNI_METHOD_IMPL_1 (jni_GetObjectRefType, lObjRef, plObjectRefTypeRef)
