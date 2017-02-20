/*
 * Copyright 2014-present by Andrew Ian William Griffin <griffin@beerdragon.co.uk> and McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "comjvm/local.h"
#include "local/delete.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace localtest {

	TEST_CLASS (WriteFilesAndFolders) {
	public:

		TEST_METHOD_INITIALIZE (DeleteDirs) {
			Assert::IsTrue (SUCCEEDED (DeleteFilesAndFolders (TEXT ("WriteFilesAndFoldersTest"))));
		}

		TEST_METHOD (InvalidArgs) {
			IDirectoryWriter *pDirectory;
			IDirectoryWriter *pDirectory2;
			IFileWriter *pFile;
			Assert::AreEqual (E_POINTER, ComJvmCreateDirectoryWriter (NULL, &pDirectory));
			Assert::AreEqual (E_POINTER, ComJvmCreateDirectoryWriter (TEXT ("Foo"), NULL));
			Assert::AreEqual (E_POINTER, ComJvmCreateFileWriter (NULL, &pFile));
			Assert::AreEqual (E_POINTER, ComJvmCreateFileWriter (TEXT ("Foo"), NULL));
			Assert::AreEqual (S_OK, ComJvmCreateDirectoryWriter (TEXT ("WriteFilesAndFoldersTest"), &pDirectory));
			Assert::IsNotNull (pDirectory);
			Assert::AreEqual (E_POINTER, pDirectory->File (NULL, &pFile));
			Assert::AreEqual (E_POINTER, pDirectory->Directory (NULL, &pDirectory2));
			_bstr_t bstr;
			bstr = TEXT ("foo\\foo");
			Assert::AreEqual (E_POINTER, pDirectory->File (bstr, NULL));
			Assert::AreEqual (E_POINTER, pDirectory->Directory (bstr, NULL));
			Assert::AreEqual (E_INVALIDARG, pDirectory->File (bstr, &pFile));
			Assert::AreEqual (E_INVALIDARG, pDirectory->Directory (bstr, &pDirectory2));
			bstr = TEXT ("..");
			Assert::AreEqual (E_INVALIDARG, pDirectory->File (bstr, &pFile));
			Assert::AreEqual (E_INVALIDARG, pDirectory->Directory (bstr, &pDirectory2));
			bstr = TEXT (".");
			Assert::AreEqual (E_INVALIDARG, pDirectory->File (bstr, &pFile));
			Assert::AreEqual (S_OK, pDirectory->Directory (bstr, &pDirectory2));
			Assert::IsTrue (pDirectory2 == pDirectory);
			pDirectory2->Release ();
			pDirectory->Release ();
		}

		TEST_METHOD (CreateDirectories) {
			IDirectoryWriter *pRoot;
			IDirectoryWriter *pFooDir;
			IDirectoryWriter *pBarDir;
			IFileWriter *pBarFile;
			DWORD dwAttr;
			_bstr_t bstr;
			Assert::AreEqual (S_OK, ComJvmCreateDirectoryWriter (TEXT ("WriteFilesAndFoldersTest\\Dirs"), &pRoot));
			dwAttr = GetFileAttributes (TEXT ("WriteFilesAndFoldersTest\\Dirs"));
			Assert::AreNotEqual (INVALID_FILE_ATTRIBUTES, dwAttr);
			Assert::IsTrue ((dwAttr & FILE_ATTRIBUTE_DIRECTORY) != 0);
			bstr = TEXT ("Foo");
			Assert::AreEqual (S_OK, pRoot->Directory (bstr, &pFooDir));
			Assert::IsNotNull (pFooDir);
			bstr = TEXT ("Bar");
			Assert::AreEqual (S_OK, pFooDir->Directory (bstr, &pBarDir));
			Assert::IsNotNull (pBarDir);
			pBarDir->Release ();
			Assert::AreEqual (HRESULT_FROM_WIN32 (ERROR_ACCESS_DENIED), pFooDir->File (bstr, &pBarFile));
			pFooDir->Release ();
			pRoot->Release ();
			dwAttr = GetFileAttributes (TEXT ("WriteFilesAndFoldersTest\\Dirs\\Foo"));
			Assert::AreNotEqual (INVALID_FILE_ATTRIBUTES, dwAttr);
			Assert::IsTrue ((dwAttr & FILE_ATTRIBUTE_DIRECTORY) != 0);
			dwAttr = GetFileAttributes (TEXT ("WriteFilesAndFoldersTest\\Dirs\\Foo\\Bar"));
			Assert::AreNotEqual (INVALID_FILE_ATTRIBUTES, dwAttr);
			Assert::IsTrue ((dwAttr & FILE_ATTRIBUTE_DIRECTORY) != 0);
		}

		void CreateFiles_checkFile (IFileWriter *pFile) {
			WIN32_FIND_DATA wfd;
			BYTE_SIZEDARR bsa;
			Assert::IsNotNull (pFile);
			bsa.clSize = 4;
			bsa.pData = (LPBYTE)"woot";
			Assert::AreEqual (S_OK, pFile->Write (&bsa));
			Assert::AreEqual (S_OK, pFile->Write (&bsa));
			Assert::AreEqual (S_OK, pFile->Close ());
			pFile->Release ();
			Assert::IsTrue (GetFileAttributesEx (TEXT ("WriteFilesAndFoldersTest\\Foo"), GetFileExInfoStandard, &wfd) != 0);
			Assert::AreEqual ((DWORD)8, wfd.nFileSizeLow);
		}

		TEST_METHOD (CreateFiles) {
			IDirectoryWriter *pDirectory;
			IDirectoryWriter *pDirectory2;
			IFileWriter *pFile;
			Assert::AreEqual (S_OK, ComJvmCreateFileWriter (TEXT ("WriteFilesAndFoldersTest\\Foo"), &pFile));
			CreateFiles_checkFile (pFile);
			Assert::AreEqual (HRESULT_FROM_WIN32 (ERROR_ALREADY_EXISTS), ComJvmCreateDirectoryWriter (TEXT ("WriteFilesAndFoldersTest\\Foo"), &pDirectory));
			Assert::AreEqual (S_OK, ComJvmCreateDirectoryWriter (TEXT ("WriteFilesAndFoldersTest\\Files"), &pDirectory));
			_bstr_t bstr (TEXT ("Foo"));
			Assert::AreEqual (S_OK, pDirectory->File (bstr, &pFile));
			CreateFiles_checkFile (pFile);
			Assert::AreEqual (HRESULT_FROM_WIN32 (ERROR_ALREADY_EXISTS), pDirectory->Directory (bstr, &pDirectory2));
			pDirectory->Release ();
		}

	};

}