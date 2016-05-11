#include "stdafx.h"
#include "GarbageCollector.h"
#include <wchar.h>

void GarbageCollector::ScanCell (XLOPER12 *cell) {
	//printXLOPER (cell);
	if ((cell->xltype == xltypeStr) &&
		(cell->val.str[0] > 0) &&
		(cell->val.str[1] == L'\x1A')) {
		//LOGTRACE ("Found an object ref!");
		//printXLOPER (cell);
		__int64 id = ParseId (cell->val.str);
		m_vObservedIds.push_back (id);
	}
}

__int64 GarbageCollector::ParseId (XCHAR *pExcelStr) {
	int cChars = *pExcelStr;
	XCHAR *pStr = pExcelStr + 1; // skip first char, which is the length in chars as 16-bit value
	int iHyphenPos;
	for (iHyphenPos = 0; *pStr != L'-' && iHyphenPos < cChars; iHyphenPos++, pStr++); // find dash separator
	++pStr; // skip over the dash and point to first digit
	++iHyphenPos;
	unsigned __int64 id = 0;
	while (pStr <= pExcelStr + cChars + 1) {
		id *= 10; // shift digit to left
		id += *pStr - L'0';
		pStr++;
	}
	return id;
}

bool GarbageCollector::ScanCells (int cols, int rows, XLOPER12 *arr) {
	for (int i = 0; i < rows; i++) {
		for (int j = 0; j < cols; j++) {
			ScanCell (arr++);
		}
	}
	return false;
}

bool GarbageCollector::ScanSheet (XLOPER12 *pWorkbookName, XLOPER12 *pSheetName) {
	if (m_firstRow.xltype == xltypeMissing) {
		Excel12f (xlfGetDocument, &m_firstRow, 2, TempInt12 (9), pSheetName);
		Excel12f (xlfGetDocument, &m_lastRow, 2, TempInt12 (10), pSheetName);
		Excel12f (xlfGetDocument, &m_firstCol, 2, TempInt12 (11), pSheetName);
		Excel12f (xlfGetDocument, &m_lastCol, 2, TempInt12 (12), pSheetName);
		Excel12f (xlSheetId, &m_sheetId, 1, pSheetName);
		if (m_firstRow.val.num == 0) {
			Excel12f (xlFree, 0, 1, &m_firstRow);
			Excel12f (xlFree, 0, 1, &m_lastRow);
			Excel12f (xlFree, 0, 1, &m_firstCol);
			Excel12f (xlFree, 0, 1, &m_lastCol);
			// Excel12f (xlFree, 0, 1, &m_sheetId); -- don't need to free because no pointers
			LOGTRACE ("sheet was empty");
			return false; // sheet empty (but not partial)
		}
		if (m_sheetId.xltype == xltypeErr) {
			Excel12f (xlFree, 0, 1, &m_firstRow);
			Excel12f (xlFree, 0, 1, &m_lastRow);
			Excel12f (xlFree, 0, 1, &m_firstCol);
			Excel12f (xlFree, 0, 1, &m_lastCol);
			// Excel12f (xlFree, 0, 1, &m_sheetId); -- don't need to free because no pointers
			LOGTRACE ("Could not get sheet ID");
			throw std::invalid_argument ("Could not get sheet ID");
			return false;
		}
		m_wholeRow.xltype = xltypeRef;
		m_wholeRow.val.mref.idSheet = m_sheetId.val.mref.idSheet;
		m_wholeRow.val.mref.lpmref = &m_xlmRef;
		m_xlmRef.count = 1;
		// set col first/last to min/max of bounding area and just change the row on each iteration.
		m_xlmRef.reftbl[0].colFirst = (COL)m_firstCol.val.num - 1;
		m_xlmRef.reftbl[0].colLast = (COL)m_lastCol.val.num - 1;
		// initialise the loop variable
		m_iRow = (RW)m_firstRow.val.num - 1;
	}
	
	while (m_iRow <= (RW)m_lastRow.val.num - 1) {
		XLOPER12 *pMulti = TempInt12 (xltypeMulti); // Excel type == multi (array)
		m_wholeRow.val.mref.lpmref->reftbl[0].rwFirst = m_iRow;
		m_wholeRow.val.mref.lpmref->reftbl[0].rwLast = m_iRow;
		XLOPER12 row;
		Excel12f (xlCoerce, &row, 2, &m_wholeRow, pMulti);
		boolean partial = ScanCells (row.val.array.columns, row.val.array.rows, row.val.array.lparray);
		Excel12f (xlFree, 0, 1, &row);
		m_iRow++;
		if (partial || IsTimeUp()) {
			return true;
		}
	}
	Excel12f (xlFree, 0, 1, &m_firstRow);
	Excel12f (xlFree, 0, 1, &m_lastRow);
	Excel12f (xlFree, 0, 1, &m_firstCol);
	Excel12f (xlFree, 0, 1, &m_lastCol);
	// Excel12f (xlFree, 0, 1, &m_sheetId); -- don't need to free because no pointers
	m_firstRow.xltype = xltypeMissing; // next time we call we'll be reinitialized
	return false;
}

bool GarbageCollector::ScanWorkbook (XLOPER12 *pWorkbookName) {
	if (m_sheets.xltype == xltypeMissing) {
		XLOPER12 *pArgNum = TempInt12 (1); // horiz array of all sheets in workbook
		Excel12f (xlfGetWorkbook, &m_sheets, 2, pArgNum, pWorkbookName);
		m_cSheets = m_sheets.val.array.columns;
		m_iSheet = 0;
		m_pSheetName = m_sheets.val.array.lparray;
	}
	
	while (m_iSheet < m_cSheets) {
		LOGTRACE ("Sheet=");
		//printXLOPER (pSheetName);
		bool partial = ScanSheet (pWorkbookName, m_pSheetName);
		m_pSheetName++;
		m_iSheet++;
		if (partial) {
			return true;
		}
	}

	FreeAllTempMemory ();
	Excel12f (xlFree, 0, 1, (LPXLOPER12)&m_sheets);
	m_sheets.xltype = xltypeMissing;
	return false;
}

bool GarbageCollector::ScanDocuments () {
	LOGTRACE ("GarbageCollect() called.");
	if (m_documents.xltype == xltypeMissing) {
		Excel12f (xlfDocuments, &m_documents, 0);
		m_cDocs = m_documents.val.array.columns;
		m_iDoc = 0;
		m_pWorkbookName = m_documents.val.array.lparray;
	}
	while (m_iDoc < m_cDocs) {
		LOGTRACE ("WorkbookName=");
		bool partial = ScanWorkbook (m_pWorkbookName);
		m_pWorkbookName++;
		m_iDoc++;
		if (partial) {
			return true;
		}
	}
	Excel12f (xlFree, 0, 1, (LPXLOPER12)&m_documents);
	m_documents.xltype = xltypeMissing;
	return false;
}

void GarbageCollector::Collect () {
	LARGE_INTEGER liNow;
	QueryPerformanceCounter (&liNow);
	m_liLatestTime.QuadPart = liNow.QuadPart + m_liMaxTicks.QuadPart;
	if (ScanDocuments ()) {
		LOGTRACE ("Partial collection");
	} else {
		LOGTRACE ("Full collection");
		__int64 allocations;
		SAFEARRAY *psaIds;
		if (SUCCEEDED (MakeSafeArray (&psaIds))) {
			Debug::odprintf (TEXT("Marking %d items still in use"), m_vObservedIds.size ());
			m_pCollector->Collect (psaIds, &allocations);
			SafeArrayDestroy (psaIds);
			Debug::odprintf (TEXT("Collection complete, there were %ll allocations since"), allocations);
			Reset ();
		} else {
			LOGTRACE ("Collector returned error");
		}
	}
}

HRESULT GarbageCollector::MakeSafeArray (SAFEARRAY **ppsaIds) {
	HRESULT hr;
	const size_t cElems = m_vObservedIds.size ();
	*ppsaIds = SafeArrayCreateVector (VT_I8, 0, cElems);
	if (*ppsaIds == NULL) {
		return E_OUTOFMEMORY;
	}
	hyper *pIdData;
	hr = SafeArrayAccessData (*ppsaIds, (PVOID *)&pIdData);
	if (FAILED (hr)) {
		SafeArrayDestroy (*ppsaIds);
		return hr;
	}
	const size_t cbElems = cElems * sizeof (hyper);
	memcpy_s (pIdData, cbElems, m_vObservedIds.data (), cbElems);
	SafeArrayUnaccessData (*ppsaIds);
	if (FAILED (hr)) {
		SafeArrayDestroy (*ppsaIds);
		return hr;
	}
	return S_OK;
}

bool GarbageCollector::IsTimeUp () {
	LARGE_INTEGER liNow;
	QueryPerformanceCounter (&liNow);
	return liNow.QuadPart > m_liLatestTime.QuadPart;
}

void GarbageCollector::SetMaxTime (int iMillis) {
	m_liMaxTicks.QuadPart = (iMillis * m_liFrequency.QuadPart) / 1000;
}

void GarbageCollector::Reset () {
	m_documents.xltype = xltypeMissing;
	m_sheets.xltype = xltypeMissing;
	m_firstRow.xltype = xltypeMissing;
	m_vObservedIds.clear ();
}

GarbageCollector::GarbageCollector (ICollect *pCollector) {
	m_pCollector = pCollector;
	Reset ();
	QueryPerformanceFrequency (&m_liFrequency);
	SetMaxTime (10);
}