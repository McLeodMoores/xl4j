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

		long NewObject (TCHAR *clsName, TCHAR *constructorSignature, long size, long *argRefs) {
			long result = NewObject (FindClass (clsName), constructorSignature, size, argRefs);
			return result;
		}

		long NewObject (long clsRef, TCHAR *constructorSignature, long size, long *argRefs) {
			HRESULT result = pJni->jni_NewObject (clsRef, GetMethodID (clsRef, TEXT ("<init>"), constructorSignature), IntegerConstant (size), argRefs);
		}

		long NewObject (TCHAR *clsName, TCHAR *constructorSignature, long size, ...) {
			std::vector<long> args (size);
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

		long NewObject (long clsRef, TCHAR *constructorSignature, long size, ...) {
			std::vector<long> args (size);
			va_list list;
			va_start (list, size);
			for (int i = 0; i < size; i++) {
				args[i] = va_arg (list, long);
			}
			long lObjectRef;
			HRESULT result = pJni->jni_NewObject (clsRef, GetMethodID (clsRef, TEXT ("<init>"), constructorSignature), IntegerConstant (size), &args[0], &lObjectRef);
			if (FAILED (result)) {
				_com_raise_error (result);
			}
			return lObjectRef;
		}
	};
}