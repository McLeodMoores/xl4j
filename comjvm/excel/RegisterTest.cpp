#include "stdafx.h"
#include "Register.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace localtest {

	TEST_CLASS (RegisterTest) {
	public:
		TEST_METHOD (CreateRegister) {
			Register reg;
			reg.scanAndRegister(*(TempStr12 (L"TEST")));
		}
	};
}