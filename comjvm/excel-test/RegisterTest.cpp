#include "stdafx.h"
#include "excel/Register.h"
#include <io.h>
#include <iostream>
#include <fcntl.h>

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace localtest {

	TEST_CLASS (RegisterTest) {
	public:
		TEST_METHOD (CreateRegister) {
			HRESULT res = AllocConsole ();
			if (FAILED (res)) {
				_com_raise_error (res);
			}
			else {
				//int ifd = _open_osfhandle ((intptr_t)GetStdHandle (STD_INPUT_HANDLE), _O_TEXT);
				//int ofd = _open_osfhandle ((intptr_t)GetStdHandle (STD_OUTPUT_HANDLE), _O_TEXT);
				//*stdin = *_fdopen (ifd, "r");
				//*stdout = *_fdopen (ofd, "w");
				//std::cout << "I made a console window";
				//std::cin.get ();
				//fclose (stdout);
				//fclose (stdin);
			}
			Register reg;
			reg.scanAndRegister(*(TempStr12 (L"TEST")));
		}
	};
}