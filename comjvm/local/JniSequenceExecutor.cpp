/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "JniSequenceExecutor.h"

CJniSequenceExecutor::CJniSequenceExecutor (CJniSequence *pOwner, long cArgs, VARIANT *pArgs, long cResults, VARIANT *pResults)
: m_lRefCount (1), m_pOwner (pOwner), m_cArgs (cArgs), m_pArgs (pArgs), m_cResults (cResults), m_pResults (pResults) {
	m_hSemaphore = pOwner->BeginExecution ();
	pOwner->AddRef ();
}

CJniSequenceExecutor::~CJniSequenceExecutor () {
	m_pOwner->EndExecution (m_hSemaphore);
	m_pOwner->Release ();
}

HRESULT CJniSequenceExecutor::Run (JNIEnv *pEnv) {
	try {
		long cValue = 0;
		std::vector<long>::const_iterator params = m_pOwner->Params ()->begin ();
		std::vector<CJniValue>::const_iterator constants = m_pOwner->Constants ()->begin ();
		std::vector<CJniValue> aValues (m_pOwner->Values ());
		for (std::vector<JniOperation>::const_iterator itr = m_pOwner->Operations ()->begin (), end = m_pOwner->Operations ()->end (); itr != end; itr++) {
			switch (*itr) {
			case JniOperation::io_LoadArgument
				: {
					long lValueRef = *(params++);
					if (m_cArgs > 0) {
						m_cArgs--;
						aValues[lValueRef].put_variant (m_pArgs++);
					}
					break;
				}
			case JniOperation::io_LoadConstant :
				aValues[cValue++] = *(constants++);
				break;
			case JniOperation::io_StoreResult
				: {
					long lValueRef = *(params++);
					if (m_cResults > 0) {
						m_cResults--;
						aValues[lValueRef].get_variant (m_pResults++);
					}
					break;
				}
			case JniOperation::jni_GetVersion :
				aValues[cValue++].put_jint (pEnv->GetVersion ());
				break;
			case JniOperation::jni_DefineClass :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_FindClass 
				: {
					const char *pszName = aValues[*(params++)].get_pchar ();
					aValues[cValue++].put_jclass (pEnv->FindClass (pszName));
					break;
				}
			case JniOperation::jni_FromReflectedMethod :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_FromReflectedField :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_ToReflectedMethod :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetSuperclass :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_IsAssignableFrom :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_ToReflectedField :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_Throw :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_ThrowNew :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_ExceptionOccurred :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_ExceptionDescribe :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_ExceptionClear :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_FatalError :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_PushLocalFrame :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_PopLocalFrame :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_NewGlobalRef :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_DeleteGlobalRef :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_DeleteLocalRef :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_IsSameObject :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_NewLocalRef :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_EnsureLocalCapacity :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_AllocObject :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_NewObject :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetObjectClass :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_IsInstanceOf :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetMethodID :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_CallMethod :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_CallNonVirtualMethod :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetFieldID :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetField :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_SetField :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetStaticMethodID :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_CallStaticMethod :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetStaticFieldID :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetStaticField :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_SetStaticField :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_NewString
				: {
					const jchar *unicode = aValues[*(params++)].get_pjchar ();
					jsize len = aValues[*(params++)].get_jsize ();
					aValues[cValue++].put_jstring (pEnv->NewString (unicode, len));
					break;
				}
			case JniOperation::jni_GetStringLength
				: {
					jstring str = aValues[*(params++)].get_jstring ();
					aValues[cValue++].put_jsize (pEnv->GetStringLength (str));
					break;
				}
			case JniOperation::jni_GetStringChars
				: {
					jstring str = aValues[*(params++)].get_jstring ();
					jboolean isCopy;
					long lIsCopyRef = *(params++);
					if (lIsCopyRef == cValue) {
						cValue++;
					}
					aValues[cValue++].put_pjchar ((jchar*)pEnv->GetStringChars (str, &isCopy));
					if (lIsCopyRef >= 0) {
						aValues[lIsCopyRef].put_jboolean (isCopy);
					}
					break;
				}
			case JniOperation::jni_ReleaseStringChars
				: {
					jstring str = aValues[*(params++)].get_jstring ();
					const jchar *chars = aValues[*(params++)].get_pjchar ();
					pEnv->ReleaseStringChars (str, chars);
					break;
				}
			case JniOperation::jni_NewStringUTF :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetStringUTFLength :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetStringUTFChars :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_ReleaseStringUTFChars :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetArrayLength :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_NewObjectArray :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetObjectArrayElement :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_SetObjectArrayElement :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_NewArray :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetArrayElements :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_ReleaseArrayElements :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetArrayRegion :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_SetArrayRegion :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_RegisterNatives :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_UnregisterNatives :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_MonitorEntry :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_MonitorExit :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetStringRegion :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetStringUTFRegion :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetPrimitiveArrayCritical :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_ReleasePrimitiveArrayCritical :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetStringCritical :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_ReleaseStringCritical :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_NewWeakGlobalRef :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_DeleteWeakGlobalRef :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_ExceptionCheck :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_NewDirectByteBuffer :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetDirectBufferAddress :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetDirectBufferCapacity :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetObjectRefType :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			default :
				_com_raise_error (E_NOTIMPL);
			}
		}
		m_hRunResult = S_OK;
	} catch (std::bad_alloc) {
		m_hRunResult = E_OUTOFMEMORY;
	} catch (_com_error &e) {
		m_hRunResult = e.Error ();
	}
	ReleaseSemaphore (m_hSemaphore, 1, NULL);
	return m_hRunResult;
}

HRESULT CJniSequenceExecutor::Wait () {
	DWORD dwStatus = WaitForSingleObject (m_hSemaphore, INFINITE);
	if (dwStatus == WAIT_OBJECT_0) {
		return m_hRunResult;
	} else {
		return E_FAIL;
	}
}

void CJniSequenceExecutor::AddRef () {
	InterlockedIncrement (&m_lRefCount);
}

void CJniSequenceExecutor::Release () {
	long lCount = InterlockedDecrement (&m_lRefCount);
	if (!lCount) delete this;
}
