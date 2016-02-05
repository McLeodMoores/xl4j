#include "stdafx.h"
#include "comjvm/local.h"
#include "comjvm/core.h"
//#include "local/CCallExecutor.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace localtest {

	TEST_CLASS (RecordInfoTest) {
private:
public:
	TEST_METHOD (XL4JOPER12RecordInfoRawTest) {
		const IID ComJvmCore_LIBID = {
			0x0e07a0b8,
			0x0fa3,
			0x4497,
			{
				0xbc,
				0x66,
				0x6d,
				0x2a,
				0xf2,
				0xa0,
				0xb9,
				0xc8
			}
		};
		const IID FUNCTIONINFO_IID = {
			0xdff6d900,
			0xb72f,
			0x4f06,
			{
				0xa1,
				0xad,
				0x04,
				0x66,
				0xad,
				0x25,
				0xc3,
				0x52
			}
		};
		const IID XL4JOPER12_IID = {
			0x053798d7,
			0xeef0,
			0x4ac5,
			{
				0x8e,
				0xb8,
				0x4d,
				0x51,
				0x5e,
				0x7c,
				0x5d,
				0xb5
			}
		};
		HRESULT hr;
		ITypeInfo *pTypeInfo;
		ITypeLib *pTypeLib;
		hr = LoadRegTypeLib (ComJvmCore_LIBID, 1, 0,/** LOCALE_USER_DEFAULT*/0, &pTypeLib);
		if (FAILED (hr)) {
			_com_error err (hr);
			LPCTSTR errMsg = err.ErrorMessage ();
			TRACE ("Failed to load type lib via LoadRegTypeLib: %s", errMsg);
			Assert::Fail ();
		}
		hr = pTypeLib->GetTypeInfoOfGuid (XL4JOPER12_IID, &pTypeInfo);
		if (FAILED (hr)) {
			_com_error err (hr);
			LPCTSTR errMsg = err.ErrorMessage ();
			TRACE ("Failed to load type lib via GetTypeInfoOfGuid: %s", errMsg);
			Assert::Fail ();
		}
		IRecordInfo *pFunctionInfoRecordInfo = NULL;
		hr = GetRecordInfoFromTypeInfo (pTypeInfo, &pFunctionInfoRecordInfo);
		
		if (FAILED (hr)) {
			_com_error err (hr);
			LPCTSTR errMsg = err.ErrorMessage ();
			TRACE ("Failed to get RecordInfoFromTypeInfo %s", errMsg);
			Assert::Fail ();
		}
	}

	TEST_METHOD (XL4JOPER12RecordInfoTest) {
		const IID ComJvmCore_LIBID = {
			0x0e07a0b8,
			0x0fa3,
			0x4497,
			{
				0xbc,
				0x66,
				0x6d,
				0x2a,
				0xf2,
				0xa0,
				0xb9,
				0xc8
			}
		};
		const IID FUNCTIONINFO_IID = {
			0xdff6d900,
			0xb72f,
			0x4f06,
			{
				0xa1,
				0xad,
				0x04,
				0x66,
				0xad,
				0x25,
				0xc3,
				0x52
			}
		};
		const IID XL4JOPER12_IID = {
			0x053798d7,
			0xeef0,
			0x4ac5,
			{
				0x8e,
				0xb8,
				0x4d,
				0x51,
				0x5e,
				0x7c,
				0x5d,
				0xb5
			}
		};
		HRESULT hr;
		IRecordInfo *pFunctionInfoRecordInfo = NULL;
		if (FAILED (hr = ::GetRecordInfoFromGuids (ComJvmCore_LIBID, 1, 0, LOCALE_USER_DEFAULT, FUNCTIONINFO_IID, &pFunctionInfoRecordInfo))) {
			_com_error err (hr);
			LPCTSTR errMsg = err.ErrorMessage ();
			TRACE ("Failed to get RecordInfoFromGuids (%x) %s", hr, errMsg);
			Assert::Fail ();
		}
	}

	};

}