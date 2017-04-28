/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "CScan.h"
#include "Internal.h"

CScanExecutor::CScanExecutor (CScan *pOwner, SAFEARRAY **pResults)
	: m_lRefCount (1), m_pOwner (pOwner), m_pResults (pResults) {
	if (!pOwner) {
		throw std::logic_error ("CScanExecutor called with null CScan");
	}
	m_hSemaphore = CreateSemaphore (NULL, 0, 1, NULL);//pOwner->BeginExecution ();
	m_hRunResult = E_NOT_SET;
	pOwner->AddRef ();
}

CScanExecutor::~CScanExecutor () {
	m_pOwner->Release ();
}

#define CHECK_EXCEPTION() if (pEnv->ExceptionCheck ()) { LOGERROR("EXCEPTION!"); Debug::printException (pEnv, pEnv->ExceptionOccurred ()); return E_FAIL; } 0

HRESULT CScanExecutor::Run (JNIEnv *pEnv) {
	HRESULT hResult = S_OK;
	try {
		//LOGTRACE ("In CScanExecutor::Run");
		jclass jcExcelFactory = pEnv->FindClass ("com/mcleodmoores/xl4j/v1/api/core/ExcelFactory");
		jmethodID jmExcelFactory_Instance = pEnv->GetStaticMethodID (jcExcelFactory, "getInstance", "()Lcom/mcleodmoores/xl4j/v1/api/core/Excel;");
		jobject joExcel = pEnv->CallStaticObjectMethod (jcExcelFactory, jmExcelFactory_Instance);
		CHECK_EXCEPTION ();
		//LOGTRACE ("Got Excel object %p", joExcel);
		jclass jcExcel = pEnv->FindClass ("com/mcleodmoores/xl4j/v1/api/core/Excel");
		CHECK_EXCEPTION ();
		jmethodID jmExcel_GetExcelCallback = pEnv->GetMethodID (jcExcel, "getExcelCallback", "()Lcom/mcleodmoores/xl4j/v1/api/core/ExcelCallback;");
		jobject joExcelCallback = pEnv->CallObjectMethod (joExcel, jmExcel_GetExcelCallback);
		CHECK_EXCEPTION ();
		//LOGTRACE ("Got Excel callback object %p", joExcelCallback);
		jclass jcFunctionRegistry = pEnv->FindClass ("com/mcleodmoores/xl4j/v1/api/core/FunctionRegistry");
		jmethodID jmFunctionRegistry_RegisterFunctions = pEnv->GetMethodID (jcFunctionRegistry, "registerFunctions", "(Lcom/mcleodmoores/xl4j/v1/api/core/ExcelCallback;)V");
		jmethodID jmExcel_GetFunctionRegistry = pEnv->GetMethodID (jcExcel, "getFunctionRegistry", "()Lcom/mcleodmoores/xl4j/v1/api/core/FunctionRegistry;");
		jobject joFunctionRegistry = pEnv->CallObjectMethod (joExcel, jmExcel_GetFunctionRegistry);
		CHECK_EXCEPTION ();
		LOGTRACE ("Calling registerFunctions...");
		pEnv->CallVoidMethod (joFunctionRegistry, jmFunctionRegistry_RegisterFunctions, joExcelCallback);
		LOGTRACE ("...Returned.");
		CHECK_EXCEPTION();
		jmethodID jmExcel_GetLowLevelExcelCallback = pEnv->GetMethodID (jcExcel, "getLowLevelExcelCallback", "()Lcom/mcleodmoores/xl4j/v1/xll/LowLevelExcelCallback;");
		jobject joLowLevelExcelCallback = pEnv->CallObjectMethod (joExcel, jmExcel_GetLowLevelExcelCallback);
		CHECK_EXCEPTION ();
		//LOGTRACE ("Got LowLevelExcelCallback %p", joLowLevelExcelCallback);
		jclass jcNativeExcelFunctionEntryAccumulator = pEnv->FindClass ("com/mcleodmoores/xl4j/v1/xll/NativeExcelFunctionEntryAccumulator");
		jmethodID jmNativeExcelFunctionEntryAccumulator_GetEntries = pEnv->GetMethodID (jcNativeExcelFunctionEntryAccumulator, "getEntries", "()[Lcom/mcleodmoores/xl4j/v1/xll/NativeExcelFunctionEntryAccumulator$FunctionEntry;");
		jobjectArray jaEntries = (jobjectArray) pEnv->CallObjectMethod (joLowLevelExcelCallback, jmNativeExcelFunctionEntryAccumulator_GetEntries);
		CHECK_EXCEPTION ();
		//LOGTRACE ("Got entries array %p", jaEntries);
		long cEntries = pEnv->GetArrayLength (jaEntries);
		LOGTRACE ("Got %d entries", cEntries);

		jclass jcFunctionEntry = pEnv->FindClass ("com/mcleodmoores/xl4j/v1/xll/NativeExcelFunctionEntryAccumulator$FunctionEntry");
		jfieldID jfExportNumber = pEnv->GetFieldID (jcFunctionEntry, "_exportNumber", "I");
		jfieldID jfFunctionExportName = pEnv->GetFieldID (jcFunctionEntry, "_functionExportName", "Ljava/lang/String;");
		jfieldID jfIsVarArgs = pEnv->GetFieldID (jcFunctionEntry, "_isVarArgs", "Z");
		jfieldID jfIsLongRunning = pEnv->GetFieldID(jcFunctionEntry, "_isLongRunning", "Z");
		jfieldID jfIsAutoAsynchronous = pEnv->GetFieldID(jcFunctionEntry, "_isAutoAsynchronous", "Z");
		jfieldID jfIsManualAsynchronous = pEnv->GetFieldID(jcFunctionEntry, "_isManualAsynchronous", "Z");
		jfieldID jfIsCallerRequired = pEnv->GetFieldID(jcFunctionEntry, "_isCallerRequired", "Z");
		jfieldID jfFunctionSignature = pEnv->GetFieldID (jcFunctionEntry, "_functionSignature", "Ljava/lang/String;");
		jfieldID jfFunctionWorksheetname = pEnv->GetFieldID (jcFunctionEntry, "_functionWorksheetName", "Ljava/lang/String;");
		jfieldID jfArgumentNames = pEnv->GetFieldID (jcFunctionEntry, "_argumentNames", "Ljava/lang/String;");
		jfieldID jfFunctionType = pEnv->GetFieldID (jcFunctionEntry, "_functionType", "I");
		jfieldID jfFunctionCategory = pEnv->GetFieldID (jcFunctionEntry, "_functionCategory", "Ljava/lang/String;");
		jfieldID jfAcceleratorKey = pEnv->GetFieldID (jcFunctionEntry, "_acceleratorKey", "Ljava/lang/String;");
		jfieldID jfHelpTopic = pEnv->GetFieldID (jcFunctionEntry, "_helpTopic", "Ljava/lang/String;");
		jfieldID jfDescription = pEnv->GetFieldID (jcFunctionEntry, "_description", "Ljava/lang/String;");
		jfieldID jfArgsHelp = pEnv->GetFieldID (jcFunctionEntry, "_argsHelp", "[Ljava/lang/String;");

		IRecordInfo *pFunctionInfoRecordInfo = NULL;
		if (FAILED (hResult = ::GetRecordInfoFromGuids (LIBID_ComJvmCore, 1, 0, 0, FUNCTIONINFO_IID, &pFunctionInfoRecordInfo))) {
			LOGERROR ("Couldn't get IRecotrdInfo");
			goto fail;
		}
		
		SAFEARRAYBOUND bounds;
		bounds.cElements = cEntries;
		bounds.lLbound = 0;
		if (FAILED (hResult = ::SafeArraySetRecordInfo (*m_pResults, pFunctionInfoRecordInfo))) {
			LOGERROR ("CScanExecutor::Run: couldn't set record info");
			goto fail;
		}
		if (FAILED (hResult = ::SafeArrayRedim (*m_pResults, &bounds))) {
			LOGERROR ("CScanExecutor::Run: Couldn't redim");
			goto fail;
		}
		FUNCTIONINFO *pFunctionInfos;
		hResult = SafeArrayAccessData (*m_pResults, reinterpret_cast<PVOID*>(&pFunctionInfos));
		for (jsize i = 0; i < cEntries; i++) {
			jobject joElement = pEnv->GetObjectArrayElement (jaEntries, i);
			pFunctionInfos[i].iExportNumber = pEnv->GetIntField (joElement, jfExportNumber);
			jstring jsFunctionExportName = (jstring) pEnv->GetObjectField (joElement, jfFunctionExportName);
			storeBSTR (pEnv, jsFunctionExportName, &pFunctionInfos[i].bsFunctionExportName);
			jboolean jsIsVarArgs = pEnv->GetBooleanField (joElement, jfIsVarArgs);
			pFunctionInfos[i].bIsVarArgs = jsIsVarArgs;
			jboolean jsIsLongRunning = pEnv->GetBooleanField(joElement, jfIsLongRunning);
			pFunctionInfos[i].bIsLongRunning = jsIsLongRunning;
			jboolean jsIsAutoAsynchronous = pEnv->GetBooleanField(joElement, jfIsAutoAsynchronous);
			pFunctionInfos[i].bIsAutoAsynchronous = jsIsAutoAsynchronous;
			jboolean jsIsManualAsynchronous = pEnv->GetBooleanField(joElement, jfIsManualAsynchronous);
			pFunctionInfos[i].bIsManualAsynchronous = jsIsManualAsynchronous;
			jboolean jsIsCallerRequired = pEnv->GetBooleanField(joElement, jfIsCallerRequired);
			pFunctionInfos[i].bIsCallerRequired = jsIsCallerRequired;
			jstring jsFunctionSignature = (jstring)pEnv->GetObjectField (joElement, jfFunctionSignature);
			storeBSTR (pEnv, jsFunctionSignature, &pFunctionInfos[i].bsFunctionSignature);
			jstring jsFunctionWorksheetname = (jstring)pEnv->GetObjectField (joElement, jfFunctionWorksheetname);
			storeBSTR (pEnv, jsFunctionWorksheetname, &pFunctionInfos[i].bsFunctionWorksheetName);
			jstring jsArgumentNames = (jstring)pEnv->GetObjectField (joElement, jfArgumentNames);
			storeBSTR (pEnv, jsArgumentNames, &pFunctionInfos[i].bsArgumentNames);
			pFunctionInfos[i].iFunctionType = pEnv->GetIntField (joElement, jfFunctionType);
			jstring jsFunctionCategory = (jstring)pEnv->GetObjectField (joElement, jfFunctionCategory);
			storeBSTR (pEnv, jsFunctionCategory, &pFunctionInfos[i].bsFunctionCategory);
			jstring jsAcceleratorKey = (jstring)pEnv->GetObjectField (joElement, jfAcceleratorKey);
			storeBSTR (pEnv, jsAcceleratorKey, &pFunctionInfos[i].bsAcceleratorKey);
			jstring jsHelpTopic = (jstring)pEnv->GetObjectField (joElement, jfHelpTopic);
			storeBSTR (pEnv, jsHelpTopic, &pFunctionInfos[i].bsHelpTopic);
			jstring jsDescription = (jstring)pEnv->GetObjectField (joElement, jfDescription);
			storeBSTR (pEnv, jsDescription, &pFunctionInfos[i].bsDescription);
			jobjectArray jaArgsHelp = (jobjectArray) pEnv->GetObjectField (joElement, jfArgsHelp);
			jsize cArgsHelp = pEnv->GetArrayLength (jaArgsHelp);
			SAFEARRAY *psaArgsHelp;
			allocSAFEARRAY_BSTR (&psaArgsHelp, cArgsHelp);
			BSTR *pArgsHelp;
			SafeArrayAccessData (psaArgsHelp, reinterpret_cast<PVOID*>(&pArgsHelp));
			for (jsize j = 0; j < cArgsHelp; j++) {
				jstring jsArgHelp = (jstring) pEnv->GetObjectArrayElement (jaArgsHelp, j);
				storeBSTR (pEnv, jsArgHelp, &pArgsHelp[j]);
				//freeBSTR (vArgHelp.bstrVal); // SafeArrayPutElement makes copy apparently. https://msdn.microsoft.com/en-us/library/windows/desktop/ms221283(v=vs.85).aspx
			}
			SafeArrayUnaccessData (psaArgsHelp);
			pFunctionInfos[i].saArgsHelp = psaArgsHelp;
		}
		SafeArrayUnaccessData (*m_pResults);
	} catch (std::bad_alloc) {
		LOGERROR ("CScanExecutor::Run: out of memory");
		hResult = E_OUTOFMEMORY;
		goto fail;
	} catch (_com_error &e) {
		LOGERROR ("CScanExecutor::Run: com error %s", e.ErrorMessage());
		hResult = e.Error ();
		goto fail;
	}
	LOGTRACE ("CScanExecutor::Run: Releasing semaphore");
	m_hRunResult = S_OK;
	ReleaseSemaphore (m_hSemaphore, 1, NULL);
	return S_OK;
fail:
	LOGERROR ("CScanExecutor::Run: Releasing semaphore (failure mode)");
	m_hRunResult = hResult;
	ReleaseSemaphore (m_hSemaphore, 1, NULL);
	return hResult;
}

void CScanExecutor::allocSAFEARRAY_BSTR (SAFEARRAY **ppsa, size_t cElem) {
	*ppsa = ::SafeArrayCreateVectorEx (VT_BSTR, 0, cElem, NULL);
}

void CScanExecutor::freeBSTR (BSTR pStr) {
#if 0
	::SysFreeString (pStr);
#else
	::CoTaskMemFree ((int *) pStr - 1); // go back 4 bytes to include header.
#endif
}

HRESULT CScanExecutor::storeBSTR (JNIEnv *pEnv, jstring jsStr, BSTR *result) {
	const jchar* jcstr = pEnv->GetStringCritical (jsStr, JNI_FALSE);
#if 1
	/* use system allocator */
	* result = ::SysAllocStringLen ((const OLECHAR *)jcstr, pEnv->GetStringLength (jsStr));
#else
	/* use COM task allocator */
	size_t len = pEnv->GetStringLength (jsStr);
	unsigned int *mem = (unsigned int *) ::CoTaskMemAlloc (((len + 1) * sizeof (OLECHAR)) + 4); // 4 for 4-byte prefix
	mem[0] = (unsigned long)(len * sizeof (OLECHAR)); // put byte count 
	*result = (BSTR)(mem + 1); // point after 4-byte prefix.
	wmemcpy_s ((wchar_t *)*result, len + 1, (wchar_t *)jcstr, len);
	(*result)[len] = '\0';
#endif
	pEnv->ReleaseStringCritical (jsStr, jcstr);
	return S_OK;
}

HRESULT CScanExecutor::Wait () {
	DWORD dwStatus = WaitForSingleObject (m_hSemaphore, INFINITE);
	if (dwStatus == WAIT_OBJECT_0) {
		return m_hRunResult;
	} else {
		return E_FAIL;
	}
}

void CScanExecutor::AddRef () {
	InterlockedIncrement (&m_lRefCount);
}

void CScanExecutor::Release () {
	long lCount = InterlockedDecrement (&m_lRefCount);
	if (!lCount) delete this;
}
