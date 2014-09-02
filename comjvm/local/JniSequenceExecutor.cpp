#include "stdafx.h"
#include "JniSequence.h"
#include "Internal.h"

CJniSequenceExecutor::CJniSequenceExecutor (CJniSequence *pOwner, long cArgs, VARIANT *pArgs, long cResults, VARIANT *pResults)
	: m_lRefCount (1), m_pOwner (pOwner), m_cArgs (cArgs), m_pArgs (pArgs), m_cResults (cResults), m_pResults (pResults) {
	if (!pOwner) {
		throw std::logic_error ("JniSequenceExecutor called with null JniSequence");
	}
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
			fprintf (stderr, "operation %x\n", *itr);
			switch (*itr) {
				case JniOperation::io_LoadArgument
					: {
					if (m_cArgs > 0) {
						m_cArgs--;
						aValues[cValue++].put_variant (m_pArgs++);
					}
					break;
				}
				case JniOperation::io_LoadConstant:
					(constants++)->copy_into (aValues[cValue++]);
					break;
				case JniOperation::jni_GetStringChars
					: {
					jstring str = aValues[*(params++)].get_jstring ();
					jboolean isCopy;
					long lIsCopyRef = *(params++);
					if (lIsCopyRef == cValue) {
						cValue++;
					}
					aValues[cValue++].put_pjchar (pEnv->GetStringChars (str, &isCopy));
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
				case JniOperation::io_StoreResult
					: {
					long lValueRef = *(params++);
					if (m_cResults > 0) {
						m_cResults--;
						aValues[lValueRef].get_variant (m_pResults++);
					}
					break;
				}
				case JniOperation::jni_GetVersion:
					aValues[cValue++].put_jint (pEnv->GetVersion ());
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
				case JniOperation::jni_FindClass
					: {
					const char *name = aValues[*(params++)].get_pchar ();
					jclass clazz = pEnv->FindClass (name);
					aValues[cValue++].put_jclass (clazz);
					break;
				}
				case JniOperation::jni_DefineClass
					: {
					const char *name = aValues[*(params++)].get_pchar ();
					jobject loader = aValues[*(params++)].get_jobject ();
					jbyte *buffer = aValues[*(params)].get_jbyteBuffer (); // note we don't ++ here because we use it twice
					jsize szBuffer = aValues[*(params++)].get_jbyteBufferSize ();
					jclass clazz = pEnv->DefineClass (name, loader, buffer, szBuffer);
					aValues[cValue++].put_jclass (clazz);
					break;
				}
				case JniOperation::jni_AllocObject
					: {
					jclass clazz = aValues[*(params++)].get_jclass ();
					jobject object = pEnv->AllocObject (clazz);
					aValues[cValue++].put_jobject (object);
					break;
				}
				case JniOperation::jni_NewObject
					: {
					jclass clazz = aValues[*(params++)].get_jclass ();
					jmethodID methodId = aValues[*(params++)].get_jmethodID ();
					jsize size = aValues[*(params++)].get_jsize (); //m_pOwner->Params ()->size ();
					jvalue *arguments = new jvalue[size];
					for (int i = 0; i < size; i++) {
						long index = *(params++);
						TCHAR buf[4096];
						_stprintf (buf, TEXT ("index = %d"), index);
						OutputDebugString (buf);
						aValues[index].get_jvalue (&arguments[i]);
					}
					jobject object = pEnv->NewObjectA (clazz, methodId, arguments);
					delete[] arguments;
					aValues[cValue++].put_jobject (object);
					break;
				}
				case JniOperation::jni_NewLocalRef
					: {
					jobject obj = aValues[*(params++)].get_jobject ();
					jobject localRef = pEnv->NewLocalRef (obj);
					aValues[cValue++].put_jobject (localRef);
					break;
				}
				case JniOperation::jni_DeleteLocalRef
					: {
					jobject localRef = aValues[*(params++)].get_jobject ();
					pEnv->DeleteLocalRef (localRef);
					break;
				}
				case JniOperation::jni_NewGlobalRef
					: {
					jobject localRef = aValues[*(params++)].get_jobject ();
					jobject globalRef = pEnv->NewGlobalRef (localRef);
					aValues[cValue++].put_jobject (globalRef);
					break;
				}
				case JniOperation::jni_DeleteGlobalRef
					: {
					jobject globalRef = aValues[*(params++)].get_jobject ();
					pEnv->DeleteGlobalRef (globalRef);
					break;
				}
				case JniOperation::jni_PushLocalFrame
					: {
					jint capacity = aValues[*(params++)].get_jint ();
					jint result = pEnv->PushLocalFrame (capacity);
					aValues[cValue++].put_jint (result);
					break;
				}
				case JniOperation::jni_PopLocalFrame
					: {
					jobject frame = aValues[*(params++)].get_jobject ();
					jobject previousFrame = pEnv->PopLocalFrame (frame);
					aValues[cValue++].put_jobject (frame);
				}
				case JniOperation::jni_EnsureLocalCapacity
					: {
					jint capacity = aValues[*(params++)].get_jint ();
					jint result = pEnv->EnsureLocalCapacity (capacity);
					aValues[cValue++].put_jint (result);
					break;
				}
				case JniOperation::jni_ExceptionOccurred
					: {
					jthrowable throwable = pEnv->ExceptionOccurred ();
					aValues[cValue++].put_jthrowable (throwable);
					break;
				}
				case JniOperation::jni_ExceptionDescribe
					: {
					pEnv->ExceptionDescribe ();
					break;
				}
				case JniOperation::jni_ExceptionClear
					: {
					pEnv->ExceptionClear ();
					break;
				}
				case JniOperation::jni_GetMethodID
					: {
					jclass clazz = aValues[*(params++)].get_jclass ();
					const char *methodName = aValues[*(params++)].get_alloc_pchar ();
					const char *signature = aValues[*(params++)].get_alloc_pchar ();
					jmethodID methodID = pEnv->GetMethodID (clazz, methodName, signature);
					odprintf (L"GetMethodID: putting value %p in slot %d", methodID, cValue);
					aValues[cValue++].put_jmethodID (methodID);
					//CoTaskMemFree ((LPVOID) methodName);
					//CoTaskMemFree ((LPVOID) signature);
					break;
				}
				case JniOperation::jni_CallMethod
					: {
					long jtype = (long)aValues[*(params++)].get_jint ();
					jobject object = aValues[*(params++)].get_jobject ();
					jmethodID methodId = aValues[*(params++)].get_jmethodID ();
					jsize size = aValues[*(params++)].get_jsize (); //m_pOwner->Params ()->size ();
					jvalue *arguments = new jvalue[size];
					for (int i = 0; i < size; i++) {
						(aValues[*(params++)].get_jvalue (&arguments[i]));
					}
					switch (jtype) {
					case JTYPE_INT: {
						jint result = pEnv->CallIntMethod (object, methodId, arguments);
						aValues[cValue++].put_jint (result);
						break;
					}
					case JTYPE_BOOLEAN: {
						jboolean result = pEnv->CallBooleanMethod (object, methodId, arguments);
						aValues[cValue++].put_jboolean (result);
						break;
					}
					case JTYPE_CHAR: {
						jchar result = pEnv->CallCharMethod (object, methodId, arguments);
						aValues[cValue++].put_jchar (result);
					}
					case JTYPE_SHORT: {
						jshort result = pEnv->CallShortMethod (object, methodId, arguments);
						aValues[cValue++].put_jshort (result);
					}
					case JTYPE_LONG: {
						jlong result = pEnv->CallLongMethod (object, methodId, arguments);
						aValues[cValue++].put_jlong (result);
					}
					case JTYPE_FLOAT: {
						jfloat result = pEnv->CallFloatMethod (object, methodId, arguments);
						aValues[cValue++].put_jfloat (result);
					}
					case JTYPE_DOUBLE: {
						jdouble result = pEnv->CallDoubleMethod (object, methodId, arguments);
						aValues[cValue++].put_jdouble (result);
					}
					case JTYPE_OBJECT:	{
						jobject result = pEnv->CallObjectMethod (object, methodId, arguments);
						aValues[cValue++].put_jobject (result);
					}
					default:
						delete arguments;
						_com_raise_error (E_INVALIDARG);
						break;
					}
					delete arguments;
					break;
				}
			}
		}
		m_hRunResult = S_OK;
	}
	catch (std::bad_alloc) {
		m_hRunResult = E_OUTOFMEMORY;
	}
	ReleaseSemaphore (m_hSemaphore, 1, NULL);
	return m_hRunResult;
}

HRESULT CJniSequenceExecutor::Wait () {
	DWORD dwStatus = WaitForSingleObject (m_hSemaphore, INFINITE);
	if (dwStatus == WAIT_OBJECT_0) {
		return m_hRunResult;
	}
	else {
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
