#include "stdafx.h"
#include "comjvm/local.h"
#include "comjvm/core.h"
#include "utils/Debug.h"
//#include "local/CCallExecutor.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace localtest {

	TEST_CLASS (SafeArrayTest) {
private:
public:
	TEST_METHOD (SafeArrayLBoundTest) {
		SAFEARRAYBOUND bounds = { 0, 0 };
		SAFEARRAY *psa = SafeArrayCreateEx (VT_VARIANT, 1, &bounds, NULL);
		HRESULT hr;
		long iLBound;
		hr = SafeArrayGetLBound (psa, 1, &iLBound);
		if (FAILED (hr)) {
			_com_error err (hr);
			TRACE ("SafeArrayGetLBound failed %s", err.ErrorMessage());
		}
		long iUBound;
		SafeArrayGetUBound (psa, 1, &iUBound);
		TRACE ("SafeArrayGetLBound returned %d", iLBound);
		TRACE ("SafeArrayGetUBound returned %d", iUBound);
	}

	};

}