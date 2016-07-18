#include "stdafx.h"
//#include "Jvm.h"
//#include "local/CScanExecutor.h"
//#include <vector>

//#ifdef COMJVM_EXCEL_EXPORT
//# define COMJVM_EXCEL_API __declspec(dllexport)
//#else
//# define COMJVM_EXCEL_API __declspec(dllimport)
//#endif /* ifndef COMJVM_DEBUG_API */
#pragma once

class TypeLib {
private:
	IRecordInfo *m_pMultiReferenceRecInfo;
	IRecordInfo *m_pLocalReferenceRecInfo;
	IRecordInfo *m_pFunctionInfoRecInfo;
	HRESULT LoadTypeLibrary ();
	HRESULT FindTypeInfoAndGetRecordInfo (ITypeLib *pTypeLib, GUID typeGUID, IRecordInfo **ppRecordInfo);
public:
	TypeLib ();
	~TypeLib ();
	HRESULT GetMultReferenceRecInfo (IRecordInfo **ppRecordInfo);
	HRESULT GetLocalReferenceRecInfo (IRecordInfo **ppRecordInfo);
	HRESULT GetFunctionInfoRecInfo (IRecordInfo **ppRecordInfo);
};