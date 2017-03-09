/*
* Copyright 2014-present by McLeod Moores Software Limited.
* See distribution for license.
*/

#include "stdafx.h"
#include "utils/Debug.h"
#include "utils/DateUtils.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace localtest {

	TEST_CLASS(DateUtilsTest) {
private:
public:
	TEST_METHOD(DateToStrTest) {
		SYSTEMTIME time;
		GetSystemTime(&time);
		time.wDay = 18;
		time.wMonth = 3;
		time.wYear = 1975;
		size_t cchDate;
		HRESULT hr = DateUtils::DateToStr(time, nullptr, &cchDate);
		if (FAILED(hr)) {
			LOGERROR("DateToStr failed, error was %s", HRESULT_TO_STR(hr));
		}
		Assert::AreEqual(S_OK, hr);
		Assert::AreEqual(11, (int)cchDate); // "1975-03-18\0" expected = 11
		wchar_t *buf = (wchar_t *)calloc(cchDate, sizeof(wchar_t));
		Assert::IsNotNull(buf);
		hr = DateUtils::DateToStr(time, buf, &cchDate);
		Assert::AreEqual(S_OK, hr);
		Assert::AreEqual(_T("1975-03-18"), buf);
		Assert::AreEqual(E_POINTER, DateUtils::DateToStr(time, nullptr, nullptr));
	}

	TEST_METHOD(ParseDateTest) {
		SYSTEMTIME time;
		HRESULT hr = DateUtils::ParseDate(L"1975-03-18", &time);
		Assert::AreEqual(S_OK, hr);
		Assert::AreEqual(18, (int) time.wDay);
		Assert::AreEqual(3, (int) time.wMonth);
		Assert::AreEqual(1975, (int) time.wYear);
		hr = DateUtils::ParseDate(L"1975-03-1823243", &time);
		Assert::AreEqual(E_FAIL, hr);
		hr = DateUtils::ParseDate(L"1975-03-AB", &time);
		Assert::AreEqual(E_FAIL, hr);
		hr = DateUtils::ParseDate(nullptr, &time);
		Assert::AreEqual(E_POINTER, hr);
		hr = DateUtils::ParseDate(L"1975-03-18", nullptr);
		Assert::AreEqual(E_POINTER, hr);
		hr = DateUtils::ParseDate(nullptr, nullptr);
		Assert::AreEqual(E_POINTER, hr);
	}

	TEST_METHOD(AddDaysTest) {
		SYSTEMTIME time1;
		DateUtils::ParseDate(L"1975-03-18", &time1);
		SYSTEMTIME time2;
		DateUtils::ParseDate(L"1975-05-17", &time2);
		SYSTEMTIME time3;
		Assert::AreEqual(S_OK, DateUtils::AddDays(time1, 60, &time3));
		Assert::AreEqual((int)time2.wDay, (int)time3.wDay);
		Assert::AreEqual((int)time2.wMonth, (int)time3.wMonth);
		Assert::AreEqual((int)time2.wYear, (int)time3.wYear);
		DateUtils::ParseDate(L"2016-12-31", &time1);
		DateUtils::ParseDate(L"2017-03-01", &time2);
		DateUtils::AddDays(time1, 60, &time3);
		Assert::AreEqual((int)time2.wDay, (int)time3.wDay);
		Assert::AreEqual((int)time2.wMonth, (int)time3.wMonth);
		Assert::AreEqual((int)time2.wYear, (int)time3.wYear);
		Assert::AreEqual(E_POINTER, DateUtils::AddDays(time1, 1, nullptr));
	}

	TEST_METHOD(AddDaysTest2) {
		SYSTEMTIME time1;
		DateUtils::ParseDate(L"1975-03-18", &time1);
		SYSTEMTIME time2;
		DateUtils::ParseDate(L"1975-03-19", &time2);
		SYSTEMTIME time3;
		Assert::AreEqual(S_OK, DateUtils::AddDays(time1, 1, &time3));
		Assert::AreEqual((int)time2.wDay, (int)time3.wDay);
		Assert::AreEqual((int)time2.wMonth, (int)time3.wMonth);
		Assert::AreEqual((int)time2.wYear, (int)time3.wYear);
	}

	TEST_METHOD(CompareTest) {
		SYSTEMTIME time1;
		DateUtils::ParseDate(L"1975-03-18", &time1);
		SYSTEMTIME time2;
		DateUtils::ParseDate(L"1975-05-17", &time2);
		SYSTEMTIME time3;
		DateUtils::AddDays(time1, 60, &time3);
		long long cmp;
		Assert::AreEqual(S_OK, DateUtils::Compare(time2, time3, &cmp));
		Assert::AreEqual(0, (int) cmp);
		Assert::AreEqual(S_OK, DateUtils::Compare(time1, time2, &cmp));
		Assert::IsTrue(cmp < 0LL);
		Assert::AreEqual(S_OK, DateUtils::Compare(time2, time1, &cmp));
		Assert::IsTrue(cmp > 0LL);
		Assert::AreEqual(E_POINTER, DateUtils::Compare(time2, time1, nullptr));
	}
	};

}