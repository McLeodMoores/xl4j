/*
* JVM as a COM object
*
* Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
* Released under the GNU General Public License.
*/

#include "stdafx.h"
#include "comjvm/local.h"
#include "comjvm/core.h"
#include "JniSequenceHelper.h"

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace localtest {

	TEST_CLASS (JniSequenceHelperTest) {

	private:
		IJvmConnector *m_pConnector;
		IJvm *m_pJvm;
	public:

		TEST_METHOD_INITIALIZE (Connect) {
			m_pConnector = NULL;
			m_pJvm = NULL;
			Assert::AreEqual (S_OK, ComJvmCreateLocalConnector (&m_pConnector));
			Assert::AreEqual (S_OK, m_pConnector->Lock ());
			IJvmTemplate *pTemplate;
			Assert::AreEqual (S_OK, ComJvmCreateTemplate (NULL, &pTemplate));
			IClasspathEntries *entries;
			Assert::AreEqual (S_OK, pTemplate->get_Classpath (&entries));
			TCHAR szCurrentDir[MAX_PATH];
			GetCurrentDirectory(MAX_PATH, szCurrentDir);
			TRACE ("Current dir is %s", szCurrentDir);
			AddEntries (entries);
			Assert::AreEqual (S_OK, Debug::print_HRESULT(m_pConnector->CreateJvm (pTemplate, NULL, &m_pJvm)));
			pTemplate->Release ();
			Assert::AreEqual (S_OK, m_pConnector->Unlock ());
		}

		void AddEntries (IClasspathEntries *entries) {
			TCHAR *base = TEXT ("..\\lib\\");
			TCHAR szDir[MAX_PATH];
			WIN32_FIND_DATA findData;
			HANDLE hFind = INVALID_HANDLE_VALUE;
			StringCchCopy (szDir, MAX_PATH, base);
			StringCchCat (szDir, MAX_PATH, TEXT("*.jar"));
			
			// Find first file in directory
			hFind = FindFirstFile (szDir, &findData);
			if (INVALID_HANDLE_VALUE == hFind) {
				_com_raise_error (ERROR_FILE_NOT_FOUND);
			}

			do {
				if (!(findData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)) {
					TCHAR szRelativePath[MAX_PATH];
					StringCchCopy (szRelativePath, MAX_PATH, base);
					StringCchCat (szRelativePath, MAX_PATH, findData.cFileName);
					TRACE ("Adding ClasspathEntry for %s", szRelativePath);
					IClasspathEntry *pEntry;
					HRESULT hr = ComJvmCreateClasspathEntry (szRelativePath, &pEntry);
					if (FAILED (hr)) {
						_com_raise_error (hr); 
					}
					entries->Add (pEntry);
				}
			} while (FindNextFile (hFind, &findData) != 0);
		}

		TEST_METHOD_CLEANUP (Disconnect) {
			if (m_pJvm) m_pJvm->Release ();
			if (m_pConnector) m_pConnector->Release ();
		}

		TEST_METHOD (CreateXLNumberAndReadValue) {
			IJniSequence *pJni;
			Assert::AreEqual (S_OK, m_pJvm->CreateJni (&pJni));
			JniSequenceHelper *pHelper = new JniSequenceHelper (pJni);
			long lDoubleRef = pHelper->DoubleConstant (7.6);
			long lXLNumberRef = pHelper->CallStaticMethod (JTYPE_OBJECT, TEXT ("com/mcleodmoores/excel4j/values/XLNumber"), TEXT ("of"), TEXT ("(D)Lcom/mcleodmoores/excel4j/values/XLNumber;"), 1, lDoubleRef);
			long lIntegerRef = pHelper->NewObject (TEXT ("java/lang/Integer"), TEXT ("(I)V"), 1, pHelper->IntegerConstant (6));
			long lValueRef = pHelper->CallMethod (JTYPE_DOUBLE, lXLNumberRef, pHelper->GetMethodID (TEXT ("com/mcleodmoores/excel4j/values/XLNumber"), TEXT ("getValue"), TEXT ("()D")), 0);
			pJni->Result (lValueRef);
			VARIANT aResults[1];
			Assert::AreEqual (S_OK, Debug::print_HRESULT (pJni->Execute (0, NULL, 1, aResults)));
			Assert::AreEqual ((short)VT_R8, (short)aResults[0].vt);
			Assert::AreEqual (7.6, aResults[0].dblVal);
			delete pHelper;
			pJni->Release ();
		}
};

}