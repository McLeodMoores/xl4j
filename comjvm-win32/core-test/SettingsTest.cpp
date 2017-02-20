/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

/*
* JVM as a COM object
*
* Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
* Released under the GNU General Public License.
*/

#include "stdafx.h"
#include "comjvm/core.h"
#include "../core/Settings.h"
#include "../utils/Debug.h"
using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace coretest {

	TEST_CLASS (Settings) {

public:

	TEST_METHOD (GetSettings) {
		CSettings settings (_T ("local"), _T ("default"), CSettings::MOST_LOCAL);
		bstr_t result = settings.GetString (_T ("Addin"), _T ("GarbageCollection"));

	}

	TEST_METHOD (TestMultiple) {
		{
			CSettings *m_pSettings = new CSettings (_T ("local"), _T ("default"), CSettings::INIT_APPDATA);
			if (m_pSettings->IsValid ()) {
				_bstr_t bstrJvmVersion = m_pSettings->GetString (TEXT ("Jvm"), TEXT ("Version"));
				if (bstrJvmVersion.length () > 0) {
					LOGTRACE ("Slect string");
					//m_lbJvms.SelectString (-1, bstrJvmVersion);
				}
				LOGTRACE ("Getting Addin/GC");
				_bstr_t bstrGCEnabled = m_pSettings->GetString (TEXT ("Addin"), TEXT ("GarbageCollection"));
				//LOGTRACE ("Got it. %p", bstrGCEnabled.GetAddress ());
				if (bstrGCEnabled.length () > 0) {
					LOGTRACE ("bstrGCEnabled.length() > 0");
					const _bstr_t ENABLED (TEXT ("Enabled"));
					if (bstrGCEnabled == ENABLED) {
						LOGTRACE ("GC Check enabled");
						//m_bGarbageCollection.SetCheck (CBS_CHECKEDNORMAL);
					} else {
						LOGTRACE ("GC Check disabled");
						//m_bGarbageCollection.SetCheck (CBS_UNCHECKEDNORMAL);
					}
				} else {
					LOGTRACE ("bstrGCEnabled.length() <= 0");
				}
			}
		}
		LOGTRACE ("After block");
	}

	};

}