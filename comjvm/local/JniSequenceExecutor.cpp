/*
 * JVM as a COM object
 *
 * Copyright 2014
 *      Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 *      McLeodMoores Software Limited <jim@mcleodmoores.com>
 *
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
			case JniOperation::io_LoadArgument :
				if (m_cArgs > 0) {
					m_cArgs--;
					aValues[cValue++].put_variant (m_pArgs++);
				} else {
					aValues[cValue++].put_nothing ();
				}
				break;
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
			case JniOperation::jni_AllocObject
				: {
					jclass clazz = aValues[*(params++)].get_jclass ();
					aValues[cValue++].put_jobject (pEnv->AllocObject (clazz));
					break;
				}
			case JniOperation::jni_NewObject
				: {
					jclass clazz = aValues[*(params++)].get_jclass ();
					jmethodID methodID = aValues[*(params++)].get_jmethodID ();
					long cArgs = *(params++), l;
					std::vector<jvalue> args (cArgs);
					for (l = 0; l < cArgs; l++) {
						aValues[*(params++)].get_jvalue (&args[l]);
					}
					aValues[cValue++].put_jobject (pEnv->NewObjectA (clazz, methodID, args.data ()));
					break;
				}
			case JniOperation::jni_GetObjectClass :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_IsInstanceOf :
				// TODO
				_com_raise_error (E_NOTIMPL);
				break;
			case JniOperation::jni_GetMethodID
				: {
					jclass clazz = aValues[*(params++)].get_jclass ();
					const char *pszName = aValues[*(params++)].get_pchar ();
					const char *pszSig = aValues[*(params++)].get_pchar ();
					aValues[cValue++].put_jmethodID (pEnv->GetMethodID (clazz, pszName, pszSig));
					break;
				}
			case JniOperation::jni_CallMethod
				: {
					long lType = *(params++);
					jobject obj = aValues[*(params++)].get_jobject ();
					jmethodID methodID = aValues[*(params++)].get_jmethodID ();
					long cArgs = *(params++), l;
					std::vector<jvalue> args(cArgs);
					for (l = 0; l < cArgs; l++) {
						aValues[*(params++)].get_jvalue (&args[l]);
					}
					switch (lType) {
					case JTYPE_BOOLEAN :
						aValues[cValue++].put_jboolean (pEnv->CallBooleanMethodA (obj, methodID, args.data ()));
						break;
					case JTYPE_BYTE :
						aValues[cValue++].put_jbyte (pEnv->CallByteMethodA (obj, methodID, args.data ()));
						break;
					case JTYPE_CHAR :
						aValues[cValue++].put_jchar (pEnv->CallCharMethodA (obj, methodID, args.data ()));
						break;
					case JTYPE_DOUBLE :
						aValues[cValue++].put_jdouble (pEnv->CallDoubleMethodA (obj, methodID, args.data ()));
						break;
					case JTYPE_FLOAT :
						aValues[cValue++].put_jfloat (pEnv->CallFloatMethodA (obj, methodID, args.data ()));
						break;
					case JTYPE_INT :
						aValues[cValue++].put_jint (pEnv->CallIntMethodA (obj, methodID, args.data ()));
						break;
					case JTYPE_LONG :
						aValues[cValue++].put_jlong (pEnv->CallLongMethodA (obj, methodID, args.data ()));
						break;
					case JTYPE_OBJECT :
						aValues[cValue++].put_jobject (pEnv->CallObjectMethodA (obj, methodID, args.data ()));
						break;
					case JTYPE_SHORT :
						aValues[cValue++].put_jshort (pEnv->CallShortMethodA (obj, methodID, args.data ()));
						break;
					case JTYPE_VOID :
						pEnv->CallVoidMethodA (obj, methodID, args.data ());
						break;
					default :
						_com_raise_error (E_INVALIDARG);
					}
					break;
				}
			case JniOperation::jni_CallNonVirtualMethod
				: {
					long lType = *(params++);
					jobject obj = aValues[*(params++)].get_jobject ();
					jclass clazz = aValues[*(params++)].get_jclass ();
					jmethodID methodID = aValues[*(params++)].get_jmethodID ();
					long cArgs = *(params++), l;
					std::vector<jvalue> args(cArgs);
					for (l = 0; l < cArgs; l++) {
						aValues[*(params++)].get_jvalue (&args[l]);
					}
					switch (lType) {
					case JTYPE_BOOLEAN :
						aValues[cValue++].put_jboolean (pEnv->CallNonvirtualBooleanMethodA (obj, clazz, methodID, args.data ()));
						break;
					case JTYPE_BYTE :
						aValues[cValue++].put_jbyte (pEnv->CallNonvirtualByteMethodA (obj, clazz, methodID, args.data ()));
						break;
					case JTYPE_CHAR :
						aValues[cValue++].put_jchar (pEnv->CallNonvirtualCharMethodA (obj, clazz, methodID, args.data ()));
						break;
					case JTYPE_DOUBLE :
						aValues[cValue++].put_jdouble (pEnv->CallNonvirtualDoubleMethodA (obj, clazz, methodID, args.data ()));
						break;
					case JTYPE_FLOAT :
						aValues[cValue++].put_jfloat (pEnv->CallNonvirtualFloatMethodA (obj, clazz, methodID, args.data ()));
						break;
					case JTYPE_INT :
						aValues[cValue++].put_jint (pEnv->CallNonvirtualIntMethodA (obj, clazz, methodID, args.data ()));
						break;
					case JTYPE_LONG :
						aValues[cValue++].put_jlong (pEnv->CallNonvirtualLongMethodA (obj, clazz, methodID, args.data ()));
						break;
					case JTYPE_OBJECT :
						aValues[cValue++].put_jobject (pEnv->CallNonvirtualObjectMethodA (obj, clazz, methodID, args.data ()));
						break;
					case JTYPE_SHORT :
						aValues[cValue++].put_jshort (pEnv->CallNonvirtualShortMethodA (obj, clazz, methodID, args.data ()));
						break;
					case JTYPE_VOID :
						pEnv->CallNonvirtualVoidMethodA (obj, clazz, methodID, args.data ());
						break;
					default :
						_com_raise_error (E_INVALIDARG);
					}
					break;
				}
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
