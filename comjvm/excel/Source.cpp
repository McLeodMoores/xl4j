#pragma once
#include "stdafx.h"
#include <comdef.h>
#include <iostream>
#include "comjvm/local.h"
#include "comjvm/core.h"

int main() {
//	IClasspathEntries *pClasspathEntries;
//	pTemplate->get_Classpath(&pClasspathEntries);


	IJvmConnector *pConnector;
	if (ComJvmCreateLocalConnector(&pConnector) != S_OK) {
		std::cerr << "CreateLocalConnector failed";
		return 1;
	}
	if (pConnector->Lock() != S_OK) {
		std::cerr << "connector Lock could not be aquired";
		return 1;
	}
		
	IJvm *pJvm;
	IJvmTemplate *pTemplate;

	if (ComJvmCreateTemplate(NULL, &pTemplate) != S_OK) {
		std::cerr << "could not create template";
		return 1;
	}
	HRESULT result = pConnector->CreateJvm(pTemplate, NULL, &pJvm);
	if (result != S_OK) {
		_com_error err(result);
		LPCTSTR errMsg = err.ErrorMessage();
		std::cerr << "could not create JVM:" << errMsg;
		return 1;
	}
	std::cerr << "Created JVM!\n";
	pConnector->Unlock();
	std::cerr << "Unlocked connector" << std::endl;
	if (pTemplate->Release() != S_OK) {
		std::cerr << "Could not release template" << std::endl;
		return 1;
	}
	IJniSequence *pJni;
	if (pJvm->CreateJni(&pJni) != S_OK) {
		std::cerr << "Could not create JNI sequence" << std::endl;
		return 1;
	}
	long lVersionRef;
	if (pJni->jni_GetVersion(&lVersionRef) != S_OK) {
		std::cerr << "Could not call jni_GetVersion" << std::endl;
		return 1;
	}
	if (pJni->Result(lVersionRef) != S_OK) {
		std::cerr << "Could not get result reference" << std::endl;
		return 1;
	}
	VARIANT aResults[1];
	if (pJni->Execute(0, NULL, 1, aResults) != S_OK) {
		std::cerr << "Could not call execute" << std::endl;
		return 1;
	}
	std::cerr << "Output was " << HIWORD(aResults[0].intVal) << "." << LOWORD(aResults[0].intVal) << std::endl;
	pJni->Release();
	pJvm->Release();
	pConnector->Release();
	std::cerr << "Released all memory" << std::endl;
};
