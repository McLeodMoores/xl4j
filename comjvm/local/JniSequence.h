/*
 * JVM as a COM object
 *
 * Copyright 2014
 *      Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 *      McLeodMoores Software Limited <jim@mcleodmoores.com>
 *
 * Released under the GNU General Public License.
 */

#pragma once

#include "core_h.h"
#include "Jvm.h"
#include "JniValue.h"

class CJniSequence;

#include "JniSequenceExecutor.h"

/// <summary>Operations that make up a JNI sequence.</summary>
enum JniOperation {
	io_LoadArgument,
	io_LoadConstant,
	io_StoreResult,
	jni_GetVersion,
	jni_DefineClass,
	jni_FindClass,
	jni_FromReflectedMethod,
	jni_FromReflectedField,
	jni_ToReflectedMethod,
	jni_GetSuperclass,
	jni_IsAssignableFrom,
	jni_ToReflectedField,
	jni_Throw,
	jni_ThrowNew,
	jni_ExceptionOccurred,
	jni_ExceptionDescribe,
	jni_ExceptionClear,
	jni_FatalError,
	jni_PushLocalFrame,
	jni_PopLocalFrame,
	jni_NewGlobalRef,
	jni_DeleteGlobalRef,
	jni_DeleteLocalRef,
	jni_IsSameObject,
	jni_NewLocalRef,
	jni_EnsureLocalCapacity,
	jni_AllocObject,
	jni_NewObject,
	jni_GetObjectClass,
	jni_IsInstanceOf,
	jni_GetMethodID,
	jni_CallMethod,
	jni_CallNonVirtualMethod,
	jni_GetFieldID,
	jni_GetField,
	jni_SetField,
	jni_GetStaticMethodID,
	jni_CallStaticMethod,
	jni_GetStaticFieldID,
	jni_GetStaticField,
	jni_SetStaticField,
	jni_NewString,
	jni_GetStringLength,
	jni_GetStringChars,
	jni_ReleaseStringChars,
	jni_NewStringUTF,
	jni_GetStringUTFLength,
	jni_GetStringUTFChars,
	jni_ReleaseStringUTFChars,
	jni_GetArrayLength,
	jni_NewObjectArray,
	jni_GetObjectArrayElement,
	jni_SetObjectArrayElement,
	jni_NewArray,
	jni_GetArrayElements,
	jni_ReleaseArrayElements,
	jni_GetArrayRegion,
	jni_SetArrayRegion,
	jni_RegisterNatives,
	jni_UnregisterNatives,
	jni_MonitorEntry,
	jni_MonitorExit,
	jni_GetStringRegion,
	jni_GetStringUTFRegion,
	jni_GetPrimitiveArrayCritical,
	jni_ReleasePrimitiveArrayCritical,
	jni_GetStringCritical,
	jni_ReleaseStringCritical,
	jni_NewWeakGlobalRef,
	jni_DeleteWeakGlobalRef,
	jni_ExceptionCheck,
	jni_NewDirectByteBuffer,
	jni_GetDirectBufferAddress,
	jni_GetDirectBufferCapacity,
	jni_GetObjectRefType
};

class CJniSequence : public IJniSequence {
private:
	friend class CJniSequenceExecutor;
	volatile ULONG m_lRefCount;
	
	/// <summary>Lock for this object.</summary>
	CRITICAL_SECTION m_cs;

	CJvm *m_pJvm;

	/// <summary>Number of intermediate values needed for execution.</summary>
	long m_cValue;

	/// <summary>Number of active executions.</summary>
	///
	/// <para>A sequence will support parallel executions, but can only be
	/// modified when there are no active executions.</para>
	long m_cExecuting;

	std::vector<HANDLE> m_ahSemaphore;

	/// <summary>Sequence operations.</summary>
	std::vector<JniOperation> m_aOperation;

	/// <summary>Operation parameters.</summary>
	///
	/// <para>Each operation may consume zero or more parameter values identifying
	/// the slots containing the parameters to be passed.</para>
	std::vector<long> m_aParam;

	/// <summary>Constant pool.</summary>
	///
	/// <para>During execution, if a constant is needed in an intermediate value
	/// slot then the next value is taken from this pool.</para>
	std::vector<CJniValue> m_aConstant;

	/// <summary>Number of declared argument values.</summary>
	long m_cArgument;

	/// <summary>Number of declared result values.</summary>
	long m_cResult;

	~CJniSequence ();
	HRESULT AddOperation (JniOperation operation);
	HRESULT AddOperation (JniOperation operation, long lParam);
	HRESULT AddOperation (JniOperation operation, long lParam1, long lParam2);
	HRESULT AddOperation (JniOperation operation, long lParam1, long lParam2, long lParam3);
	HRESULT AddOperation (JniOperation operation, long lParam1, long lParam2, long lParam3, long lParam4);
	HRESULT AddOperation (JniOperation operation, long lParam1, long lParam2, long lParam3, long lParam4, long lParam5);
	HRESULT LoadConstant (CJniValue &value, long *plRef);
	HANDLE BeginExecution ();
	void EndExecution (HANDLE hSemaphore);
	long Values () { return m_cValue; }
	const std::vector<JniOperation> *Operations () { return &m_aOperation; }
	const std::vector<long> *Params () { return &m_aParam; }
	const std::vector<CJniValue> *Constants () { return &m_aConstant; }
public:
	CJniSequence (CJvm *pJvm);
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject);
    ULONG STDMETHODCALLTYPE AddRef ();
    ULONG STDMETHODCALLTYPE Release ();
	// IJniSequence
    /* [propget] */ HRESULT STDMETHODCALLTYPE get_Arguments ( 
        /* [retval][out] */ long *pcArgs);
    /* [propget] */ HRESULT STDMETHODCALLTYPE get_Results ( 
        /* [retval][out] */ long *pcResults);
    HRESULT STDMETHODCALLTYPE Execute ( 
        /* [in] */ long cArgs,
        /* [size_is][in] */ VARIANT *aArgs,
        /* [in] */ long cResults,
        /* [size_is][out] */ VARIANT *aResults);
    HRESULT STDMETHODCALLTYPE Argument ( 
        /* [retval][out] */ long *plValueRef);
    HRESULT STDMETHODCALLTYPE Result ( 
        /* [in] */ long lValueRef);
    HRESULT STDMETHODCALLTYPE StringConstant ( 
        /* [in] */ BSTR bstr,
        /* [retval][out] */ long *plValueRef);
    HRESULT STDMETHODCALLTYPE ByteConstant (
        /* [in] */ byte nValue,
        /* [retval][out] */ long *plValueRef);
    HRESULT STDMETHODCALLTYPE ShortConstant ( 
        /* [in] */ short nValue,
        /* [retval][out] */ long *plValueRef);
    HRESULT STDMETHODCALLTYPE IntConstant ( 
        /* [in] */ long nValue,
        /* [retval][out] */ long *plValueRef);
    HRESULT STDMETHODCALLTYPE LongConstant ( 
        /* [in] */ hyper nValue,
        /* [retval][out] */ long *plValueRef);
	HRESULT STDMETHODCALLTYPE FloatConstant (
		/* [in] */ float fValue,
		/* [retval][out] */ long *plValueRef);
	HRESULT STDMETHODCALLTYPE DoubleConstant (
		/* [in] */ double dValue,
		/* [retval][out] */ long *plValueRef);
    HRESULT STDMETHODCALLTYPE BooleanConstant ( 
        /* [in] */ BOOL fValue,
        /* [retval][out] */ long *plValueRef);
    HRESULT STDMETHODCALLTYPE jni_GetVersion ( 
        /* [out] */ long *plValueRef);
    HRESULT STDMETHODCALLTYPE jni_DefineClass ( 
        /* [in] */ long lNameRef,
        /* [in] */ long lLoaderRef,
        /* [in] */ long lBufRef,
        /* [in] */ long lLenRef,
        /* [retval][out] */ long *plClassRef);
    HRESULT STDMETHODCALLTYPE jni_FindClass ( 
        /* [in] */ long lNameRef,
        /* [retval][out] */ long *plClassRef);
    HRESULT STDMETHODCALLTYPE jni_FromReflectedMethod ( 
        /* [in] */ long lMethodRef,
        /* [retval][out] */ long *plMethodIDRef);
    HRESULT STDMETHODCALLTYPE jni_FromReflectedField ( 
        /* [in] */ long lFieldRef,
        /* [retval][out] */ long *plFieldIDRef);
    HRESULT STDMETHODCALLTYPE jni_ToReflectedMethod ( 
        /* [in] */ long lClsRef,
        /* [in] */ long lMethodIDRef,
        /* [in] */ long lIsStaticRef,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_GetSuperclass ( 
        /* [in] */ long lSubRef,
        /* [retval][out] */ long *plClassRef);
    HRESULT STDMETHODCALLTYPE jni_IsAssignableFrom ( 
        /* [in] */ long lSubRef,
        /* [in] */ long lSupRef,
        /* [retval][out] */ long *plBooleanRef);
    HRESULT STDMETHODCALLTYPE jni_ToReflectedField ( 
        /* [in] */ long lClsRef,
        /* [in] */ long lFieldIDRef,
        /* [in] */ long lIsStaticRef,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_Throw ( 
        /* [in] */ long lObjRef,
        /* [retval][out] */ long *plIntRef);
    HRESULT STDMETHODCALLTYPE jni_ThrowNew ( 
        /* [in] */ long lClassRef,
        /* [in] */ long lMsgRef,
        /* [retval][out] */ long *plIntRef);
    HRESULT STDMETHODCALLTYPE jni_ExceptionOccurred ( 
        /* [retval][out] */ long *plThrowableRef);
    HRESULT STDMETHODCALLTYPE jni_ExceptionDescribe ();
    HRESULT STDMETHODCALLTYPE jni_ExceptionClear ();
    HRESULT STDMETHODCALLTYPE jni_FatalError ( 
        /* [in] */ long lMsgRef);
    HRESULT STDMETHODCALLTYPE jni_PushLocalFrame ( 
        /* [in] */ long lCapacityRef,
        /* [retval][out] */ long *plIntRef);
    HRESULT STDMETHODCALLTYPE jni_PopLocalFrame ( 
        /* [in] */ long lResultRef,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_NewGlobalRef ( 
        /* [in] */ long lLobjRef,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_DeleteGlobalRef ( 
        /* [in] */ long lGrefRef);
    HRESULT STDMETHODCALLTYPE jni_DeleteLocalRef ( 
        /* [in] */ long lObjRef);
    HRESULT STDMETHODCALLTYPE jni_IsSameObject ( 
        /* [in] */ long lObj1Ref,
        /* [in] */ long lObj2Ref,
        /* [retval][out] */ long *plBooleanRef);
    HRESULT STDMETHODCALLTYPE jni_NewLocalRef ( 
        /* [in] */ long lRefRef,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_EnsureLocalCapacity ( 
        /* [in] */ long lCapacityRet,
        /* [retval][out] */ long *plIntRef);
    HRESULT STDMETHODCALLTYPE jni_AllocObject ( 
        /* [in] */ long lClassRef,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_NewObject ( 
        /* [in] */ long lClassRef,
        /* [in] */ long lMethodIDRef,
        /* [in] */ long cArgs,
        /* [size_is][in] */ long *alArgRefs,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_GetObjectClass ( 
        /* [in] */ long lObjRef,
        /* [retval][out] */ long *plClassRef);
    HRESULT STDMETHODCALLTYPE jni_IsInstanceOf ( 
        /* [in] */ long lObjRef,
        /* [in] */ long lClassRef,
        /* [retval][out] */ long *plBooleanRef);
    HRESULT STDMETHODCALLTYPE jni_GetMethodID ( 
        /* [in] */ long lClassRef,
        /* [in] */ long lNameRef,
        /* [in] */ long lSigRef,
        /* [retval][out] */ long *plMethodIDRef);
    HRESULT STDMETHODCALLTYPE jni_CallMethod ( 
		/* [in] */ long lType,
        /* [in] */ long lObjRef,
        /* [in] */ long lMethodIDRef,
        /* [in] */ long cArgs,
        /* [size_is][in] */ long *alArgRefs,
        /* [retval][out] */ long *plResultRef);
    HRESULT STDMETHODCALLTYPE jni_CallNonVirtualMethod ( 
		/* [in] */ long lType,
        /* [in] */ long lObjRef,
        /* [in] */ long lClassRef,
        /* [in] */ long lMethodIDRef,
        /* [in] */ long cArgs,
        /* [size_is][in] */ long *alArgRefs,
        /* [retval][out] */ long *plesultRef);
    HRESULT STDMETHODCALLTYPE jni_GetFieldID ( 
        /* [in] */ long lClassRef,
        /* [in] */ long lNameRef,
        /* [in] */ long lSigRef,
        /* [retval][out] */ long *plFieldIDRef);
    HRESULT STDMETHODCALLTYPE jni_GetField ( 
		/* [in] */ long lType,
        /* [in] */ long lObjRef,
        /* [in] */ long lFieldIDRef,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_SetField ( 
		/* [in] */ long lType,
        /* [in] */ long lObjRef,
        /* [in] */ long lFieldIDRef,
        /* [in] */ long lValueRef);
    HRESULT STDMETHODCALLTYPE jni_GetStaticMethodID ( 
        /* [in] */ long lClassRef,
        /* [in] */ long lNameRef,
        /* [in] */ long lSigRef,
        /* [retval][out] */ long *plMethodIDRef);
    HRESULT STDMETHODCALLTYPE jni_CallStaticMethod ( 
		/* [in] */ long lType,
        /* [in] */ long lClassRef,
        /* [in] */ long lMethodIDRef,
        /* [in] */ long cArgs,
        /* [size_is][in] */ long *alArgsRef,
        /* [retval][out] */ long *plResultRef);
    HRESULT STDMETHODCALLTYPE jni_GetStaticFieldID ( 
        /* [in] */ long lClassRef,
        /* [in] */ long lNameRef,
        /* [in] */ long lSigRef,
        /* [retval][out] */ long *plFieldIDRef);
    HRESULT STDMETHODCALLTYPE jni_GetStaticField ( 
		/* [in] */ long lType,
        /* [in] */ long lClassRef,
        /* [in] */ long lFieldIDRef,
        /* [retval][out] */ long *plResultRef);
    HRESULT STDMETHODCALLTYPE jni_SetStaticField ( 
		/* [in] */ long lType,
        /* [in] */ long lClassRef,
        /* [in] */ long lFieldIDRef,
        /* [in] */ long lValueRef);
    HRESULT STDMETHODCALLTYPE jni_NewString ( 
        /* [in] */ long lUnicodeRef,
        /* [in] */ long lSizeRef,
        /* [retval][out] */ long *plStringRef);
    HRESULT STDMETHODCALLTYPE jni_GetStringLength ( 
        /* [in] */ long lStrRef,
        /* [retval][out] */ long *plSizeRef);
    HRESULT STDMETHODCALLTYPE jni_GetStringChars ( 
        /* [in] */ long lStrRef,
        /* [optional][out] */ long *plIsCopyRef,
        /* [retval][out] */ long *plCharRef);
    HRESULT STDMETHODCALLTYPE jni_ReleaseStringChars ( 
        /* [in] */ long lStrRef,
        /* [in] */ long lCharsRef);
    HRESULT STDMETHODCALLTYPE jni_NewStringUTF ( 
        /* [in] */ long lUtfRef,
        /* [retval][out] */ long *plStringRef);
    HRESULT STDMETHODCALLTYPE jni_GetStringUTFLength ( 
        /* [in] */ long lStrRef,
        /* [retval][out] */ long *plSizeRef);
    HRESULT STDMETHODCALLTYPE jni_GetStringUTFChars ( 
        /* [in] */ long lStrRef,
        /* [optional][out] */ long *plIsCopyRef,
        /* [retval][out] */ long *plCharRef);
    HRESULT STDMETHODCALLTYPE jni_ReleaseStringUTFChars ( 
        /* [in] */ long lStrRef,
        /* [in] */ long lCharsRef);
    HRESULT STDMETHODCALLTYPE jni_GetArrayLength ( 
        /* [in] */ long lArrayRef,
        /* [retval][out] */ long *plSizeRef);
    HRESULT STDMETHODCALLTYPE jni_NewObjectArray ( 
        /* [in] */ long lLenRef,
        /* [in] */ long lClassRef,
        /* [in] */ long lInitRef,
        /* [retval][out] */ long *plObjectArrayRef);
    HRESULT STDMETHODCALLTYPE jni_GetObjectArrayElement ( 
        /* [in] */ long lArrayRef,
        /* [in] */ long lIndexRef,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_SetObjectArrayElement ( 
        /* [in] */ long lArrayRef,
        /* [in] */ long lIndexRef,
        /* [in] */ long lValRef);
    HRESULT STDMETHODCALLTYPE jni_NewArray ( 
		/* [in] */ long lType,
        /* [in] */ long lLenRef,
        /* [retval][out] */ long *plArrayRef);
    HRESULT STDMETHODCALLTYPE jni_GetArrayElements ( 
		/* [in] */ long lType,
        /* [in] */ long lArrayRef,
        /* [out] */ long *plIsCopyRef,
        /* [retval][out] */ long *plValueRef);
    HRESULT STDMETHODCALLTYPE jni_ReleaseArrayElements ( 
		/* [in] */ long lType,
        /* [in] */ long lArrayRef,
        /* [in] */ long lElemsRef,
        /* [in] */ long lModeRef);
    HRESULT STDMETHODCALLTYPE jni_GetArrayRegion ( 
		/* [in] */ long lType,
        /* [in] */ long lArrayRef,
        /* [in] */ long lStartRef,
        /* [in] */ long lLRef,
        /* [in] */ long lBufRef);
    HRESULT STDMETHODCALLTYPE jni_SetArrayRegion ( 
		/* [in] */ long lType,
        /* [in] */ long lArrayRef,
        /* [in] */ long lStartRef,
        /* [in] */ long lLRef,
        /* [in] */ long lBufRef);
    HRESULT STDMETHODCALLTYPE jni_RegisterNatives ( 
        /* [in] */ long lClassRef,
        /* [in] */ long lMethodsRef,
        /* [in] */ long lNMethodsef,
        /* [retval][out] */ long *plIntRef);
    HRESULT STDMETHODCALLTYPE jni_UnregisterNatives ( 
        /* [in] */ long lClassRef,
        /* [retval][out] */ long *plIntRef);
    HRESULT STDMETHODCALLTYPE jni_MonitorEntry ( 
        /* [in] */ long lObjRef,
        /* [retval][out] */ long *plIntRef);
    HRESULT STDMETHODCALLTYPE jni_MonitorExit ( 
        /* [in] */ long lObjRef,
        /* [retval][out] */ long *plIntRef);
    HRESULT STDMETHODCALLTYPE jni_GetStringRegion ( 
        /* [in] */ long lStrRef,
        /* [in] */ long lStartRef,
        /* [in] */ long lLenRef,
        /* [in] */ long lBufRef);
    HRESULT STDMETHODCALLTYPE jni_GetStringUTFRegion ( 
        /* [in] */ long lStrRef,
        /* [in] */ long lStartRef,
        /* [in] */ long lLenRef,
        /* [in] */ long lBufRef);
    HRESULT STDMETHODCALLTYPE jni_GetPrimitiveArrayCritical ( 
        /* [in] */ long lArrayRef,
        /* [optional][out] */ long *plIsCopyRef,
        /* [retval][out] */ long *plVoidRef);
    HRESULT STDMETHODCALLTYPE jni_ReleasePrimitiveArrayCritical ( 
        /* [in] */ long lArrayRef,
        /* [in] */ long lCArrayRef,
        /* [in] */ long lModeRef);
    HRESULT STDMETHODCALLTYPE jni_GetStringCritical ( 
        /* [in] */ long lStringRef,
        /* [optional][out] */ long *plIsCopyRef,
        /* [retval][out] */ long *plCharRef);
    HRESULT STDMETHODCALLTYPE jni_ReleaseStringCritical ( 
        /* [in] */ long lStringRef,
        /* [in] */ long lCStringRef);
    HRESULT STDMETHODCALLTYPE jni_NewWeakGlobalRef ( 
        /* [in] */ long lObjRef,
        /* [retval][out] */ long *plWeakRef);
    HRESULT STDMETHODCALLTYPE jni_DeleteWeakGlobalRef ( 
        /* [in] */ long lRefRef);
    HRESULT STDMETHODCALLTYPE jni_ExceptionCheck ( 
        /* [retval][out] */ long *plBooleanRef);
    HRESULT STDMETHODCALLTYPE jni_NewDirectByteBuffer ( 
        /* [in] */ long lAddressRef,
        /* [in] */ long lCapacityRef,
        /* [retval][out] */ long *plObjectRef);
    HRESULT STDMETHODCALLTYPE jni_GetDirectBufferAddress ( 
        /* [in] */ long lBufRef,
        /* [retval][out] */ long *plVoidRef);
    HRESULT STDMETHODCALLTYPE jni_GetDirectBufferCapacity ( 
        /* [in] */ long lBufRef,
        /* [retval][out] */ long *plLongRef);
    HRESULT STDMETHODCALLTYPE jni_GetObjectRefType ( 
        /* [in] */ long lObjRef,
        /* [retval][out] */ long *plObjectRefTypeRef);
};