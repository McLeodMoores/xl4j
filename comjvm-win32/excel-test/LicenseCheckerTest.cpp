/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "helper/LicenseChecker.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace exceltest {

	TEST_CLASS(LicenseCheckerTest) {
	private:
		CLicenseChecker *m_pInstance;

	public:

		TEST_METHOD_INITIALIZE(CreateInstance) {
			m_pInstance = new CLicenseChecker();
		}

		TEST_METHOD_CLEANUP(ReleaseInstance) {
			if (m_pInstance) {
				delete m_pInstance;
				m_pInstance = NULL;
			}
		}

		TEST_METHOD(TestValidate) {
			m_pInstance->Validate();
		}
	};
}