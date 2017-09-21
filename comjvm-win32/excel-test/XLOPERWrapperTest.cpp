/*
* Copyright 2014-present by McLeod Moores Software Limited.
* See distribution for license.
*/

#include "stdafx.h"
#include "excel/XLOPERWrapper.h"
#include "excel/Excel.h"
#include <iostream>

using namespace Microsoft::VisualStudio::CppUnitTestFramework;
//namespace Microsoft {
//	namespace VisualStudio {
//		namespace CppUnitTestFramework {
//			template<>
//		    static std::wstring ToString<XLOPERWrapper<>>(const XLOPERWrapper<>& oper) {
//				return oper.ToString();
//			}
//
//		}
//	}
//}
namespace exceltest {
	XLOPER12 fuzzBigData(std::allocator<BYTE> allocator) {
		XLOPER12 xloper12;
		xloper12.xltype = xltypeBigData;
		if ((rand() % 2) == 0) { // 50%
			xloper12.val.bigdata.cbData = 0;
			xloper12.val.bigdata.h.hdata = (HANDLE)rand(); // random handle
		} else {
			xloper12.val.bigdata.cbData = rand() % 16384; // random block of memory
			xloper12.val.bigdata.h.lpbData = (BYTE *)allocator.allocate(xloper12.val.bigdata.cbData);
			for (long i = 0; i < xloper12.val.bigdata.cbData; i++) {
				xloper12.val.bigdata.h.lpbData[i] = (BYTE)rand(); // fill with random data. (why clear it then?)
			}
		}
		return xloper12;
	}

	XLOPER12 fuzzFlow(std::allocator<BYTE> allocator) {
		XLOPER12 xloper12;
		xloper12.xltype = xltypeFlow;
		int flow = rand() % 5;
		BYTE xlflow;
		switch (flow) {
		case 0: xlflow = xlflowHalt; break;
		case 1: xlflow = xlflowGoto; break;
		case 2: xlflow = xlflowRestart; break;
		case 3: xlflow = xlflowPause; break;
		case 4: xlflow = xlflowResume; break;
		}
		xloper12.val.flow.xlflow = xlflow;
		if (xloper12.val.flow.xlflow == xlflowGoto) {
			xloper12.val.flow.col = rand();
			xloper12.val.flow.rw = rand();
			xloper12.val.flow.valflow.idSheet = rand();
		}
		if (xloper12.val.flow.xlflow == xlflowPause) {
			xloper12.val.flow.valflow.tbctrl = rand();
		}
		if (xloper12.val.flow.xlflow == xlflowRestart) {
			xloper12.val.flow.valflow.level = rand();
		}
		return xloper12;
	}

	XLOPER12 fuzzValues(std::allocator<BYTE> allocator);

	XLOPER12 fuzzArray(std::allocator<BYTE> allocator) {
		XLOPER12 xloper12;
		xloper12.xltype = xltypeMulti;
		COL cols = rand() % 256;
		RW rws = rand() % 256;
		xloper12.val.array.columns = cols;
		xloper12.val.array.rows = rws;
		try {
			XLOPER12 *pArray = (XLOPER12 *)allocator.allocate(rws * cols * sizeof XLOPER12);
			xloper12.val.array.lparray = pArray;
			for (COL col = 0; col < cols; col++) {
				for (RW rw = 0; rw < rws; rw++) {
					*pArray++ = fuzzValues(allocator);
				}
			}
			return xloper12;
		} catch (std::bad_alloc ba) {
			return xloper12;
		}
	}

	XLOPER12 fuzzMRef(std::allocator<BYTE> allocator) {
		size_t length = rand() % 4;
		XLOPER12 xloper;
		XLMREF12 *lpmref = (XLMREF12 *)allocator.allocate(sizeof XLMREF12 + (sizeof XLREF12 * (length - 1)));
		xloper.xltype = xltypeRef;
		xloper.val.mref.idSheet = rand();
		xloper.val.mref.lpmref = lpmref;
		xloper.val.mref.lpmref->count = length;
		for (size_t i = 0; i < length; i++) {
			lpmref->reftbl[i].colFirst = rand() % 256;
			lpmref->reftbl[i].colLast = rand() % 256;
			lpmref->reftbl[i].rwFirst = rand() % 256;
			lpmref->reftbl[i].rwLast = rand() % 256;
		}
		return xloper;
	}

	XLOPER12 fuzzSRef(std::allocator<BYTE> allocator) {
		XLOPER12 xloper;
		xloper.xltype = xltypeSRef;
		xloper.val.sref.count = 1;
		xloper.val.sref.ref.colFirst = rand() % 256;
		xloper.val.sref.ref.colLast = rand() % 256;
		xloper.val.sref.ref.rwFirst = rand() % 256;
		xloper.val.sref.ref.rwLast = rand() % 256;
		return xloper;
	}

	XLOPER12 fuzzInt(std::allocator<BYTE> allocator) {
		XLOPER12 xloper;
		xloper.xltype = xltypeInt;
		xloper.val.w = rand();
		return xloper;
	}

	XLOPER12 fuzzErr(std::allocator<BYTE> allocator) {
		XLOPER12 xloper;
		xloper.xltype = xltypeErr;
		int err = rand() % 8;
		switch (err) {
		case 0:	xloper.val.err = xlerrDiv0;	break;
		case 1: xloper.val.err = xlerrGettingData; break;
		case 2: xloper.val.err = xlerrNA; break;
		case 3: xloper.val.err = xlerrName; break;
		case 4: xloper.val.err = xlerrNull; break;
		case 5: xloper.val.err = xlerrNum; break;
		case 6: xloper.val.err = xlerrRef; break;
		case 7: xloper.val.err = xlerrValue; break;
		}
		return xloper;
	}

	XLOPER12 fuzzBool(std::allocator<BYTE> allocator) {
		XLOPER12 xloper;
		xloper.xltype = xltypeBool;
		if (rand() % 2) {
			xloper.val.xbool = TRUE;
		} else {
			xloper.val.xbool = FALSE;
		}
		return xloper;
	}

	XLOPER12 fuzzStr(std::allocator<BYTE> allocator) {
		XLOPER12 xloper;
		xloper.xltype = xltypeStr;
		size_t length = rand() % 256; //sure it could be bigger...
		XCHAR *buf = (XCHAR *)allocator.allocate((length + 2) * sizeof XCHAR);
		*buf = (XCHAR)length;
		for (size_t i = 1; i < length + 1; i++) {
			buf[i] = (XCHAR)(rand() % 64) + 32; // some vaguely printable characters.
		}
		buf[length + 1] = 0; // zero terminate the string.
		xloper.val.str = buf;
		return xloper;
	}

	XLOPER12 fuzzNum(std::allocator<BYTE> allocator) {
		XLOPER12 xloper;
		xloper.xltype = xltypeNum;
		xloper.val.num = ((double)rand()) / 5.; // so they're not all integers.
		return xloper;
	}

	XLOPER12 fuzzAll(std::allocator<BYTE> allocator) {
		int num = rand() % 10; // crappy distribution but who cares.
		switch (num) {
		case 0: return fuzzBigData(allocator);
		case 1: return fuzzFlow(allocator);
		case 2: return fuzzArray(allocator);
		case 3: return fuzzMRef(allocator);
		case 4: return fuzzSRef(allocator);
		case 5: return fuzzInt(allocator);
		case 6: return fuzzErr(allocator);
		case 7: return fuzzBool(allocator);
		case 8: return fuzzStr(allocator);
		case 9: return fuzzNum(allocator);
		}
		return fuzzNum(allocator);
	}

	XLOPER12 fuzzValues(std::allocator<BYTE> allocator) {
		int num = rand() % 5; // crappy distribution but who cares.
		switch (num) {
		case 0: return fuzzInt(allocator);
		case 1: return fuzzErr(allocator);
		case 2: return fuzzBool(allocator);
		case 3: return fuzzStr(allocator);
		case 4: return fuzzNum(allocator);
		}
		return fuzzNum(allocator);
	}
	TEST_CLASS(XLOPERWrapperTest) {
private:

public:


	//TEST_METHOD_INITIALIZE(Init) {
	//}

	//TEST_METHOD_CLEANUP(Cleanup) {
	//}

	TEST_METHOD(FuzzMany) {
		std::allocator<BYTE> allocator;
		for (int i = 0; i < 1000; i++) {
			XLOPER12 random = fuzzAll(allocator);
			XLOPER12 random2 = fuzzAll(allocator);
			//LoadLibraryW(L"excel.xll");
			//LoadDLLs();
			//CoInitializeEx(nullptr, COINIT_MULTITHREADED);
			//Assert::AreEqual((int)g_dwTlsIndex, 0, L"tls same");
			const XLOPERWrapper randomWrapped(&random);
			
			const XLOPERWrapper randomWrappedCopy(randomWrapped);
			const XLOPERWrapper randomWrapped2(&random2);
			if (!(randomWrapped == randomWrappedCopy)) {
				fprintf(stderr, "Hello");
			}
			Assert::IsTrue(randomWrapped == randomWrappedCopy);
			size_t baseHash = std::hash<XLOPERWrapper>()(randomWrapped);
			size_t copyHash = std::hash<XLOPERWrapper>()(randomWrappedCopy);
			//Debug::SetLogLevel(LOGLEVEL_TRACE);
			//LOGTRACE("base = %d, %s", baseHash, randomWrapped.ToString());
			//LOGTRACE("copy = %d, %s", baseHash, randomWrappedCopy.ToString());
			Assert::IsTrue(baseHash == copyHash);
			//Assert::IsFalse(
			if (
				std::hash<XLOPERWrapper>()(randomWrapped) ==
				std::hash<XLOPERWrapper>()(randomWrapped2)) {
				OutputDebugString(L"Hello");
			}
		}
	}
	};
}