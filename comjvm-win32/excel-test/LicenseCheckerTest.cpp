/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "helper/LicenseChecker.h"

namespace exceltest {

	class LicenseCheckerTest {
	private:
		CLicenseChecker *m_pInstance;

	public:

		LicenseCheckerTest() {
			m_pInstance = new CLicenseChecker();
		}

		~LicenseCheckerTest() {
			if (m_pInstance) {
				delete m_pInstance;
				m_pInstance = NULL;
			}
		}

		void TestValidate() {
			m_pInstance->Validate();
		}
	};
}