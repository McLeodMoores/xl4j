/*
* Copyright 2014-present by McLeod Moores Software Limited.
* See distribution for license.
*/

#include "stdafx.h"
#include "utils/Debug.h"
#include "utils/DateUtils.h"
#include "helper/UpdateChecker.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace localtest {

	TEST_CLASS(UpdateCheckerTest) {
private:
public:
	TEST_METHOD(ShouldWeCheckTest) {
		CSettings *pSettings = new CSettings(L"local", L"test", CSettings::INIT_APPDATA);
		pSettings->DeleteKey(SECTION_ADDIN); // in case there's junk in there from previous run
		CUpdateChecker checker;
		bool result;
		// check default state with no settings, which should be to check.
		HRESULT hr = checker.ShouldWeCheck(pSettings, &result);
		Assert::AreEqual(ERROR_NO_DATA, hr);
		Assert::AreEqual(true, result);
		// check explcitly disallow
		pSettings->PutString(SECTION_ADDIN, KEY_UPGRADE_CHECK_REQUIRED, VALUE_UPGRADE_CHECK_REQUIRED_NO);
		Assert::AreEqual(S_OK, checker.ShouldWeCheck(pSettings, &result));
		Assert::AreEqual(false, result);
		// put in earliest update value before today (i.e. should return true if not for disallowance)
		SYSTEMTIME time, yesterday, tomorrow;
		GetSystemTime(&time);
		Assert::AreEqual(S_OK, DateUtils::AddDays(time, -1, &yesterday));
		size_t cchYesterday;
		Assert::AreEqual(S_OK, DateUtils::DateToStr(yesterday, nullptr, &cchYesterday));
		wchar_t *szYesterday = (wchar_t *)calloc(cchYesterday, sizeof(wchar_t));
		Assert::IsNotNull(szYesterday);
		Assert::AreEqual(S_OK, DateUtils::DateToStr(time, szYesterday, &cchYesterday));
		pSettings->PutString(SECTION_ADDIN, KEY_UPGRADE_EARLIEST, szYesterday);
		Assert::AreEqual(S_OK, checker.ShouldWeCheck(pSettings, &result));
		Assert::AreEqual(false, result);
		// Now change check required to yes, now with a date yesterday
		pSettings->PutString(SECTION_ADDIN, KEY_UPGRADE_CHECK_REQUIRED, VALUE_UPGRADE_CHECK_REQUIRED_YES);
		Assert::AreEqual(S_OK, checker.ShouldWeCheck(pSettings, &result));
		Assert::AreEqual(true, result);
		// try with it set to default value (should be yes) to get same result
		pSettings->PutString(SECTION_ADDIN, KEY_UPGRADE_CHECK_REQUIRED, VALUE_UPGRADE_CHECK_REQUIRED_DEFAULT);
		Assert::AreEqual(S_OK, checker.ShouldWeCheck(pSettings, &result));
		Assert::AreEqual(true, result);
		// delete the check upgrade required key completely and see if it still works (it should because checking is default)
		pSettings->DeleteString(SECTION_ADDIN, KEY_UPGRADE_CHECK_REQUIRED);
		Assert::AreEqual(S_OK, checker.ShouldWeCheck(pSettings, &result));
		Assert::AreEqual(true, result);
		
		// create a date tomorrow to check date comparison 
		Assert::AreEqual(S_OK, DateUtils::AddDays(time, 1, &tomorrow));
		size_t cchTomorrow;
		Assert::AreEqual(S_OK, DateUtils::DateToStr(time, nullptr, &cchTomorrow));
		wchar_t *szTomorrow = (wchar_t *)calloc(cchTomorrow, sizeof(wchar_t));
		Assert::IsNotNull(szTomorrow);
		Assert::AreEqual(S_OK, DateUtils::DateToStr(tomorrow, szTomorrow, &cchTomorrow));
		pSettings->PutString(SECTION_ADDIN, KEY_UPGRADE_EARLIEST, szTomorrow);
		Assert::AreEqual(S_OK, checker.ShouldWeCheck(pSettings, &result));
		Assert::AreEqual(false, false);
		// Now change check required to yes, now with a date yesterday
		pSettings->PutString(SECTION_ADDIN, KEY_UPGRADE_CHECK_REQUIRED, VALUE_UPGRADE_CHECK_REQUIRED_YES);
		Assert::AreEqual(S_OK, checker.ShouldWeCheck(pSettings, &result));
		Assert::AreEqual(false, result);
		// try with it set to default value (should be yes) to get same result
		pSettings->PutString(SECTION_ADDIN, KEY_UPGRADE_CHECK_REQUIRED, VALUE_UPGRADE_CHECK_REQUIRED_DEFAULT);
		Assert::AreEqual(S_OK, checker.ShouldWeCheck(pSettings, &result));
		Assert::AreEqual(false, result);
		pSettings->PutString(SECTION_ADDIN, KEY_UPGRADE_CHECK_REQUIRED, VALUE_UPGRADE_CHECK_REQUIRED_NO);
		Assert::AreEqual(S_OK, checker.ShouldWeCheck(pSettings, &result));
		Assert::AreEqual(false, result);
		free(szTomorrow);
		free(szYesterday);
	}

	TEST_METHOD(CheckTest) {
		CUpdateChecker checker;
		//bool result;
		Assert::AreEqual(S_OK, checker.Check());
	}
	};

}