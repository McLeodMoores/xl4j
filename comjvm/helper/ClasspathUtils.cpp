#include "stdafx.h"
#include "ClasspathUtils.h"

void ClasspathUtils::AddEntries (IClasspathEntries *entries, TCHAR *base) {
	TCHAR szDir[MAX_PATH];
	WIN32_FIND_DATA findData;
	HANDLE hFind = INVALID_HANDLE_VALUE;
	StringCchCopy (szDir, MAX_PATH, base);
	StringCchCat (szDir, MAX_PATH, TEXT ("*.jar"));

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