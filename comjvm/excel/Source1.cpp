#pragma once
#include "stdafx.h"
#include <comdef.h>
#include <iostream>
#include "comjvm/local.h"
#include "comjvm/core.h"

using std::cerr;
using std::cout;
using std::endl;

int main2() {
	//	IClasspathEntries *pClasspathEntries;
	//	pTemplate->get_Classpath(&pClasspathEntries);

	IJvmConnector *pConnector;
	if (FAILED(ComJvmCreateLocalConnector(&pConnector))) {
		cerr << "CreateLocalConnector failed" << endl;
		return 1;
	}
	if (FAILED(pConnector->Lock())) {
		cerr << "connector Lock could not be aquired" << endl;
		return 1;
	}

	IJvm *pJvm;
	IJvmTemplate *pTemplate;

	if (FAILED(ComJvmCreateTemplate(NULL, &pTemplate))) {
		cerr << "could not create template" << endl;
		return 1;
	}
	HRESULT result = pConnector->CreateJvm(pTemplate, NULL, &pJvm);
	if (FAILED(result)) {
		_com_error err(result);
		LPCTSTR errMsg = err.ErrorMessage();
		cerr << "could not create JVM:" << errMsg;
		return 1;
	}
	cout << "Created JVM!" << endl;
	pConnector->Unlock();
	cout << "Unlocked connector" << endl;
	if (FAILED(pTemplate->Release())) {
		cerr << "Could not release template" << endl;
		return 1;
	}
	IJniSequence *pJni;
	if (FAILED(pJvm->CreateJni(&pJni))) {
		cerr << "Could not create JNI sequence" << endl;
		return 1;
	}
	long lVersionRef;
	if (FAILED(pJni->jni_GetVersion(&lVersionRef))) {
		cerr << "Could not call jni_GetVersion" << endl;
		return 1;
	}
	BSTR xlNumberClsStr = SysAllocString(L"com.mcleodmoores.excel4j.values.XLNumber");
	long lXlNumberClsStrRef;
	pJni->StringConstant(xlNumberClsStr, &lXlNumberClsStrRef);
	long lXlNumberClassRef;
	pJni->jni_FindClass(lXlNumberClsStrRef, &lXlNumberClassRef);
	long lOfRef;
	BSTR ofRefStr = SysAllocString(L"of");
	pJni->StringConstant(ofRefStr, &lOfRef);
	if (FAILED(pJni->Result(lVersionRef))) {
		cerr << "Could not get result reference" << endl;
		return 1;
	}
	VARIANT aResults[1];
	if (FAILED(pJni->Execute(0, NULL, 1, aResults))) {
		cerr << "Could not call execute" << endl;
		return 1;
	}
	cerr << "Output was " << HIWORD(aResults[0].intVal) << "." << LOWORD(aResults[0].intVal) << endl;
	
	pJni->Release();
	pJvm->Release();
	pConnector->Release();
	std::cerr << "Released all memory" << std::endl;
	return 0;
}
/*
int paramSize(VARIANT params...) {
	int i=0;
	//for (i = 0; params[i] != NULL; i++);
	return i;
}

HRESULT CallStaticMethod(IJniSequence *pJni, TCHAR *className, TCHAR *methodName, TCHAR *signature, VARIANT params, ...) {
	BSTR classStr = SysAllocString(className);
	long lClassStrRef;
	HRESULT result;
	result = pJni->StringConstant(classStr, &lClassStrRef);
	if (FAILED(result)) return result;
	BSTR methodNameStr = SysAllocString(methodName);
	long lMethodNameStrRef;
	result = pJni->StringConstant(methodNameStr, &lMethodNameStrRef);
	if (FAILED(result)) return result;
	BSTR signatureStr = SysAllocString(signature);
	long lSignatureStrRef;
	result = pJni->StringConstant(signatureStr, &lSignatureStrRef);
	if (FAILED(result)) return result;
	long lClassRef;
	result = pJni->jni_FindClass(lClassStrRef, &lClassRef);
	if (FAILED(result)) return result;
	long lMethodRef;
	result = pJni->jni_GetMethodID(lClassRef, lMethodNameStrRef, lSignatureStrRef, &lMethodRef);
	if (FAILED(result)) return result;

	long *lParamRefs = (long *) malloc(sizeof lMethodRef * paramSize(params));
//	result = encodeVariantParams(pJni, &lParamRefs, params);
	if (FAILED(result)) return result;
	
	//pJni->jni_CallStaticMethod()
}



HRESULT encodeVariantParams(IJniSequence *pJni, long **paramRefs, VARIANT params, ...) {
	int i = 0;
	while (params[i]) {
		HRESULT result;
		VARIANT param = params[i];
		long *paramRef = paramRefs[i]; 
		switch (param.vt) {
		case VT_ERROR:
		case VT_I1:
			result = pJni->ByteConstant(param.bVal, paramRef);
			break;
		case VT_I2:
			result = pJni->ShortConstant(param.iVal, paramRef);
			break;
		case VT_I4:
			result = pJni->IntConstant(param.intVal, paramRef);
			break;
		case VT_I8:
			result = pJni->LongConstant(param.llVal, paramRef);
			break;
		case VT_BSTR:
			result = pJni->StringConstant(param.bstrVal, paramRef);
			break;
		case VT_BOOL:
			result = pJni->BooleanConstant(param.boolVal, paramRef);
			break;
		case VT_R4:
		case VT_R8:
		case VT_UNKNOWN:
		case VT_DISPATCH:
		case VT_VARIANT:
		case VT_RECORD:
		default:
			// TODO: unsupported types
			return E_NOTIMPL;
		}
		if (FAILED(result)) {
			return result;
		}
		i++;
	}
	return S_OK;
}
*/
