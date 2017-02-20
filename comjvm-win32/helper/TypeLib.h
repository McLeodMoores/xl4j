/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"

#pragma once

#ifdef COMJVM_HELPER_EXPORT
# define COMJVM_HELPER_API __declspec(dllexport)
#else
# define COMJVM_HELPER_API __declspec(dllimport)
#endif /* ifndef COMJVM_DEBUG_API */

class COMJVM_HELPER_API TypeLib {
private:
	IRecordInfo *m_pMultiReferenceRecInfo;
	IRecordInfo *m_pLocalReferenceRecInfo;
	IRecordInfo *m_pFunctionInfoRecInfo;
	HRESULT LoadTypeLibrary ();
	HRESULT FindTypeInfoAndGetRecordInfo (ITypeLib *pTypeLib, GUID typeGUID, IRecordInfo **ppRecordInfo) const;
public:
	TypeLib ();
	~TypeLib ();
	HRESULT GetMultReferenceRecInfo (IRecordInfo **ppRecordInfo);
	HRESULT GetLocalReferenceRecInfo (IRecordInfo **ppRecordInfo);
	HRESULT GetFunctionInfoRecInfo (IRecordInfo **ppRecordInfo);
};