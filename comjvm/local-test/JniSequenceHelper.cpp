#include "stdafx.h"
#include "comjvm/local.h"
#include "comjvm/core.h"
#include "Debug.h"

namespace localtest {
	class JniSequenceHelper {
	private:
		IJniSequence *pJni;
	public:
		JniSequenceHelper (IJniSequence *pJni) {
			this->pJni = pJni;
			pJni->AddRef ();
		}

		~JniSequenceHelper () {
			pJni->Release ();
			delete this;
		}

		long StringConstant (TCHAR *str) {
			_bstr_t bstrString (str);
			long lStringRef;
			HRESULT result = pJni->StringConstant (bstrString.Detach (), &lStringRef);
			if (FAILED (result)) {
				_com_raise_error (result);
			}
			return lStringRef;
		}

		long IntegerConstant (int val) {
			long lIntegerRef;
			HRESULT result = pJni->IntConstant (val, &lIntegerRef);
			if (FAILED (result)) {
				_com_raise_error (result);
			}
			return lIntegerRef;
		}

		long FindClass (TCHAR *clsName) {
			long lClsNameRef = StringConstant (clsName);
			long lClsRef;
			HRESULT result = pJni->jni_FindClass (lClsNameRef, &lClsRef);
			if (FAILED (result)) {
				_com_raise_error (result);
			}
			return lClsRef;
		}

		long GetMethodID (TCHAR *clsName, TCHAR *methodName, TCHAR *methodSignature) {
			return GetMethodID (FindClass (clsName), methodName, methodSignature);
		}

		long GetMethodID (long clsRef, TCHAR *methodName, TCHAR *methodSignature) {
			long lMethodNameRef = StringConstant (methodName);
			long lMethodSignatureRef = StringConstant (methodSignature);
			long lMethodIDRef;
			HRESULT result = pJni->jni_GetMethodID (clsRef, lMethodNameRef, lMethodSignatureRef, &lMethodIDRef);
			if (FAILED (result)) {
				_com_raise_error (result);
			}
			return lMethodIDRef;
		}

		long GetStaticMethodID (TCHAR *clsName, TCHAR *methodName, TCHAR *methodSignature) {
			return GetStaticMethodID (FindClass (clsName), methodName, methodSignature);
		}

		long GetStaticMethodID (long clsRef, TCHAR *methodName, TCHAR *methodSignature) {
			long lMethodNameRef = StringConstant (methodName);
			long lMethodSignatureRef = StringConstant (methodSignature);
			long lMethodIDRef;
			HRESULT result = pJni->jni_GetStaticMethodID (clsRef, lMethodNameRef, lMethodSignatureRef, &lMethodIDRef);
			if (FAILED (result)) {
				_com_raise_error (result);
			}
			return lMethodIDRef;
		}

		long NewObject (TCHAR *clsName, TCHAR *constructorSignature, long size, long *argRefs) {
			long result = NewObject (FindClass (clsName), constructorSignature, size, argRefs);
			return result;
		}

		long NewObject (long clsRef, TCHAR *constructorSignature, long size, long *argRefs) {
			HRESULT result = pJni->jni_NewObject (clsRef, GetMethodID (clsRef, TEXT ("<init>"), constructorSignature), IntegerConstant (size), argRefs);
		}

		long NewObject (TCHAR *clsName, TCHAR *constructorSignature, long numArgs, ...) {
			std::vector<long> args (numArgs);
			va_list list;
			va_start (list, size);
			for (int i = 0; i < size; i++) {
				args[i] = va_arg (list, long);
			}
			long result = NewObject (FindClass (clsName), constructorSignature, size, args);
			long lObjectRef;
			long lClsRef = FindClass (clsName);
			HRESULT result = pJni->jni_NewObject (lClsRef, GetMethodID (lClsRef, TEXT ("<init>"), constructorSignature), IntegerConstant (size), &args[0], &lObjectRef);
			if (FAILED (result)) {
				_com_raise_error (result);
			}
			return lObjectRef;
		}

		long NewObject (long clsRef, TCHAR *constructorSignature, long numArgs, ...) {
			std::vector<long> args (numArgs);
			va_list list;
			va_start (list, numArgs);
			for (int i = 0; i < numArgs; i++) {
				args[i] = va_arg (list, long);
			}
			va_end (list);
			long lObjectRef;
			HRESULT result = pJni->jni_NewObject (clsRef, GetMethodID (clsRef, TEXT ("<init>"), constructorSignature), IntegerConstant (size), &args[0], &lObjectRef);
			if (FAILED (result)) {
				_com_raise_error (result);
			}
			return lObjectRef;
		}

		long CallMethod (long returnType, long lObjectRef, long lMethodIDRef, long numArgs, ...) {
			std::vector<long> args (numArgs);
			va_list list;
			va_start (list, numArgs);
			for (int i = 0; i < numArgs; i++) {
				args[i] = va_arg (list, long);
			}
			va_end (list);
			long lReturnTypeRef = IntegerConstant (returnType);
			long lResultRef;
			HRESULT result = pJni->jni_CallMethod (lReturnTypeRef, lObjectRef, lMethodIDRef, numArgs, &args[0], &lResultRef);
			if (FAILED (result)) {
				_com_raise_error (result);
			}
			return lResultRef;
		}

		long CallMethod (long returnType, long lObjectRef, long lMethodIDRef, long numArgs, long *args) {
			long lReturnTypeRef = IntegerConstant (returnType);
			long lResultRef;
			HRESULT result = pJni->jni_CallMethod (lReturnTypeRef, lObjectRef, lMethodIDRef, numArgs, args, &lResultRef);
			if (FAILED (result)) {
				_com_raise_error (result);
			}
			return lResultRef;
		}

		long CallStaticMethod (long returnType, long lClassRef, long lMethodIDRef, long numArgs, ...) {
			std::vector<long> args (numArgs);
			va_list list;
			va_start (list, numArgs);
			for (int i = 0; i < numArgs; i++) {
				args[i] = va_arg (list, long);
			}
			va_end (list);
			long lReturnTypeRef = IntegerConstant (returnType);
			long lResultRef;
			HRESULT result = pJni->jni_CallStaticMethod (lReturnTypeRef, lClassRef, lMethodIDRef, numArgs, &args[0], &lResultRef);
			if (FAILED (result)) {
				_com_raise_error (result);
			}
			return lResultRef;
		}

		long CallStaticMethod (long returnType, long lClassRef, long lMethodIDRef, long numArgs, long *args) {
			long lReturnTypeRef = IntegerConstant (returnType);
			long lResultRef;
			HRESULT result = pJni->jni_CallStaticMethod (lReturnTypeRef, lClassRef, lMethodIDRef, numArgs, args, &lResultRef);
			if (FAILED (result)) {
				_com_raise_error (result);
			}
			return lResultRef;
		}

		long CallStaticMethod (long returnType, TCHAR *className, TCHAR *methodName, TCHAR *methodSignature, long numArgs, ...) {
			std::vector<long> args (numArgs);
			va_list list;
			va_start (list, numArgs);
			for (int i = 0; i < numArgs; i++) {
				args[i] = va_arg (list, long);
			}
			va_end (list);
			long lReturnTypeRef = IntegerConstant (returnType);
			long lClassRef = FindClass (className);
			long lMethodIDRef = GetStaticMethodID (lClassRef, methodName, methodSignature);
			long lResultRef;
			HRESULT result = pJni->jni_CallStaticMethod (lReturnTypeRef, lClassRef, lMethodIDRef, numArgs, &args[0], &lResultRef);
			if (FAILED (result)) {
				_com_raise_error (result);
			}
			return lResultRef;
		}

		long CallStaticMethod (long returnType, TCHAR *className, TCHAR *methodName, TCHAR *methodSignature, long numArgs, long *args) {
			long lReturnTypeRef = IntegerConstant (returnType);
			long lClassRef = FindClass (className);
			long lMethodIDRef = GetStaticMethodID (lClassRef, methodName, methodSignature);
			long lResultRef;
			HRESULT result = pJni->jni_CallStaticMethod (lReturnTypeRef, lClassRef, lMethodIDRef, numArgs, args, &lResultRef);
			if (FAILED (result)) {
				_com_raise_error (result);
			}
			return lResultRef;
		}

		long NewArray (long type, int length) {
			long lTypeRef = IntegerConstant (type);
			long lSizeRef = IntegerConstant (length);
			long lArrayRef;
			HRESULT result = pJni->jni_NewArray (lTypeRef, lSizeRef, &lArrayRef);
			if (FAILED (result)) {
				_com_raise_error (result);
			}
			return lArrayRef;
		}

		long NewArray (long type, int length, int width) {
			long lTypeRef = IntegerConstant (type);
			long lLengthRef = IntegerConstant (length);
			long lWidthRef = IntegerConstant (width);
			long lArrayRef;
			long lFirstDimClsRef;
			switch (type) {
			case JTYPE_INT: {
				lFirstDimClsRef = FindClass (TEXT("[I"));
				break;
			}
			case JTYPE_BOOLEAN: {
				lFirstDimClsRef = FindClass (TEXT ("[Z"));
				break;
			}
			case JTYPE_BYTE: {
				lFirstDimClsRef = FindClass (TEXT ("[B"));
				break;
			}
			case JTYPE_CHAR: {
				lFirstDimClsRef = FindClass (TEXT ("[C"));
				break;
			}
			case JTYPE_SHORT: {
				lFirstDimClsRef = FindClass (TEXT ("[S"));
				break;
			}
			case JTYPE_LONG: {
				lFirstDimClsRef = FindClass (TEXT ("[J"));
				break;
			}
			case JTYPE_FLOAT: {
				lFirstDimClsRef = FindClass (TEXT ("[F"));
				break;
			}
			case JTYPE_DOUBLE: {
				lFirstDimClsRef = FindClass (TEXT ("[D"));
				break;
			}
			case JTYPE_OBJECT: {
				_com_raise_error (E_INVALIDARG);
			}
			}
			HRESULT result = pJni->jni_NewObjectArray (lLengthRef, lFirstDimClsRef, NULL, &lArrayRef);
			for (int i = 0; i < width; i++) {
				long lInnerArrayRef;
				HRESULT result = pJni->jni_NewArray (lTypeRef, lWidthRef, &lInnerArrayRef);
				if (FAILED (result)) {
					_com_raise_error (result);
				}
				result = pJni->jni_SetObjectArrayElement (lArrayRef, i, lInnerArrayRef);
				if (FAILED (result)) {
					_com_raise_error (result);
				}
			}
			return lArrayRef;
		}

		long NewObjectArray (long lClassRef, int lArrayClassRef, int length, int width) {
			long lLengthRef = IntegerConstant (length);
			long lWidthRef = IntegerConstant (width);
			long lArrayRef;			
			HRESULT result = pJni->jni_NewObjectArray (lLengthRef, lArrayClassRef, NULL, &lArrayRef);
			for (int i = 0; i < width; i++) {
				long lInnerArrayRef;
				HRESULT result = pJni->jni_NewObjectArray (lClassRef, lWidthRef, NULL, &lInnerArrayRef);
				if (FAILED (result)) {
					_com_raise_error (result);
				}
				result = pJni->jni_SetObjectArrayElement (lArrayRef, i, lInnerArrayRef);
				if (FAILED (result)) {
					_com_raise_error (result);
				}
			}
			return lArrayRef;
		}

		long NewObjectArray (TCHAR *cls, int length, int width) {
			long lLengthRef = IntegerConstant (length);
			long lWidthRef = IntegerConstant (width);
			TCHAR *clsArr = new TCHAR[_tcslen (cls) + 2];
			_tcscpy(clsArr, TEXT ("["));
			_tcscpy (clsArr + 1, cls);
			long lArrayClassRef = FindClass (clsArr);
			delete[] clsArr;
			long lClassRef = FindClass (cls);
			long lArrayRef;
			HRESULT result = pJni->jni_NewObjectArray (lLengthRef, lArrayClassRef, NULL, &lArrayRef);
			for (int i = 0; i < width; i++) {
				long lInnerArrayRef;
				HRESULT result = pJni->jni_NewObjectArray (lClassRef, lWidthRef, NULL, &lInnerArrayRef);
				if (FAILED (result)) {
					_com_raise_error (result);
				}
				result = pJni->jni_SetObjectArrayElement (lArrayRef, i, lInnerArrayRef);
				if (FAILED (result)) {
					_com_raise_error (result);
				}
			}
			return lArrayRef;
		}
	};
}