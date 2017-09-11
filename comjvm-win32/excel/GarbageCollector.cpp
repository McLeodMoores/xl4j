/*
 * Copyright 2014-present by McLeod Moores Software Limited.
 * See distribution for license.
 */

#include "stdafx.h"
#include "GarbageCollector.h"
#include "ExcelUtils.h"
#include "Excel.h"
#include <wchar.h>

#define XLFGETDOCUMENT_BUG_WORKAROUND
//#include "../utils/TraceOff.h"

void GarbageCollector::ScanCell (XLOPER12 *cell) {
	//printXLOPER (cell);
	if ((cell->xltype == xltypeStr) &&
		(cell->val.str[0] > 0) &&
		(cell->val.str[1] == L'\xBB')) {
		//LOGTRACE ("Found an object ref!");
		//printXLOPER (cell);
		unsigned __int64 id = ParseId (cell->val.str);
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
	while (pStr <= pExcelStr + cChars) {
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

void GarbageCollector::SetFallbackArea() {
	m_firstRow.xltype = xltypeNum;
	m_firstRow.val.num = 1;
	m_lastRow.xltype = xltypeNum;
	m_lastRow.val.num = 2048;
	m_firstCol.xltype = xltypeNum;
	m_firstCol.val.num = 1;
	m_lastCol.xltype = xltypeNum;
	m_lastCol.val.num = 256;
}

bool GarbageCollector::ScanSheet (XLOPER12 *pWorkbookName, XLOPER12 *pSheetName) {
	XLOPER12 docType;
	Excel12f(xlfGetDocument, &docType, 2, TempInt12(3), pSheetName); // 3 == GetType
	LOGTRACE("Worksheet detected type %f (%d)", docType.val.num, docType.val.w);
	if (docType.val.num != 1.) { // 1 == worksheet (as opposed to e.g. a chart)
		LOGWARN("Non-worksheet detected type %f (%d)", docType.val.num, docType.val.w);
		// don't need to free docType as no pointers involved.
		return false;
	}
	if (m_firstRow.xltype == xltypeMissing) {
		Excel12f (xlfGetDocument, &m_firstRow, 2, TempInt12 (9), pSheetName); // 9 == Get first row
		ExcelUtils::PrintXLOPER(&m_firstRow);
		if (m_firstRow.val.num == 0.) {
			Excel12f(xlFree, 0, 1, &m_firstRow);
			LOGINFO("Returned zero (empty sheet) on get first row");
#ifdef XLFGETDOCUMENT_BUG_WORKAROUND
			SetFallbackArea();
#else
			m_firstRow.xltype = xltypeMissing;
#endif
			goto skip_xlfgetdocument;
			//return false;
		}
		Excel12f (xlfGetDocument, &m_lastRow, 2, TempInt12 (10), pSheetName); // 10 == Get last row
		ExcelUtils::PrintXLOPER(&m_lastRow);
		if (m_lastRow.val.num == 0.) {
			Excel12f(xlFree, 0, 1, &m_firstRow);
			Excel12f(xlFree, 0, 1, &m_lastRow);
			LOGINFO("Returned zero(empty sheet) on get last row");
#ifdef XLFGETDOCUMENT_BUG_WORKAROUND
			SetFallbackArea();
#else
			m_firstRow.xltype = xltypeMissing;
#endif
			goto skip_xlfgetdocument;
			//return false;
		}
		Excel12f (xlfGetDocument, &m_firstCol, 2, TempInt12 (11), pSheetName); // 11 == Get first column
		ExcelUtils::PrintXLOPER(&m_firstCol);
		if (m_firstCol.val.num == 0.) {
			Excel12f(xlFree, 0, 1, &m_firstRow);
			Excel12f(xlFree, 0, 1, &m_lastRow);
			Excel12f(xlFree, 0, 1, &m_firstCol);
			LOGINFO("Returned zero (empty sheet) on get first column");
#ifdef XLFGETDOCUMENT_BUG_WORKAROUND
			SetFallbackArea();
			goto skip_xlfgetdocument;
#else
			m_firstRow.xltype = xltypeMissing;
			return false;
#endif
		}

		Excel12f (xlfGetDocument, &m_lastCol, 2, TempInt12 (12), pSheetName); // 12 = Get last column
		ExcelUtils::PrintXLOPER(&m_lastCol);
		if (m_lastCol.val.num == 0.) {
			Excel12f(xlFree, 0, 1, &m_firstRow);
			Excel12f(xlFree, 0, 1, &m_lastRow);
			Excel12f(xlFree, 0, 1, &m_firstCol);
			Excel12f(xlFree, 0, 1, &m_lastCol);
			LOGINFO("Returned zero (empty sheet) on get last column");
#ifdef XLFGETDOCUMENT_BUG_WORKAROUND
			SetFallbackArea();
			goto skip_xlfgetdocument;
#else
			m_firstRow.xltype = xltypeMissing;
			return false;
#endif
		}
skip_xlfgetdocument:
		Excel12f (xlSheetId, &m_sheetId, 1, pSheetName);
		if (m_sheetId.xltype == xltypeErr) {
			Excel12f (xlFree, 0, 1, &m_firstRow);
			Excel12f (xlFree, 0, 1, &m_lastRow);
			Excel12f (xlFree, 0, 1, &m_firstCol);
			Excel12f (xlFree, 0, 1, &m_lastCol);
			// Excel12f (xlFree, 0, 1, &m_sheetId); -- don't need to free because no pointers
			LOGERROR ("Could not get sheet ID");
			//throw std::invalid_argument ("Could not get sheet ID");
			m_firstRow.xltype = xltypeMissing;
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
	LOGTRACE("Collecting in workbook %s, sheet %s, firstRow = %d, lastRow = %d, firstCol = %d, lastCol = %d, currentRow = %d", pWorkbookName->val.str, pSheetName->val.str, (RW)m_firstRow.val.num, (RW)m_lastRow.val.num, (COL)m_firstCol.val.num, (COL)m_lastCol.val.num, m_iRow);
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
		LOGTRACE ("Sheet");
		ExcelUtils::PrintXLOPER (m_pSheetName);
		bool partial = ScanSheet (pWorkbookName, m_pSheetName);
		if (partial) {
			return true;
		}
		m_pSheetName++;
		m_iSheet++;
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
		if (partial) {
			LOGTRACE("Abandoning pass as timeout happened");
			return true;
		}
		m_pWorkbookName++;
		m_iDoc++;
	}
	Excel12f (xlFree, 0, 1, (LPXLOPER12)&m_documents);
	m_documents.xltype = xltypeMissing;
	return false;
}

const ULONGLONG GarbageCollector::MINIMUM_RECALC_GAP = 30 * 1000;

boolean GarbageCollector::IsNotTooSoonForRefresh() {
	ULONGLONG now = GetTickCount64();
	return (now - m_lastRefreshTime) > MINIMUM_RECALC_GAP;
}

void GarbageCollector::Collect () {
	if (g_pAddinEnv->IsCalculateFullRebuildInProgress()) {
		LOGTRACE("Rebuild in progress, skipping GC scan");
		return;
	}
	LARGE_INTEGER liNow;
	QueryPerformanceCounter (&liNow);
	m_liLatestTime.QuadPart = liNow.QuadPart + m_liMaxTicks.QuadPart;
	if (ScanDocuments ()) {
		LOGTRACE ("Partial collection");
	} else {
		LOGTRACE ("Full collection");
		__int64 unrecognisedHandles;
		SAFEARRAY *psaIds;
		if (SUCCEEDED (MakeSafeArray (&psaIds))) {
			//Debug::odprintf (TEXT("Marking %d items still in use"), m_vObservedIds.size ());
			m_pCollector->Collect (psaIds, &unrecognisedHandles);
			SafeArrayDestroy (psaIds);
			//Debug::odprintf (TEXT("Collection complete, there were %ll allocations since"), allocations);
			Reset ();
			if (unrecognisedHandles > 0) {
				LOGTRACE("There were %lld unrecognised handles", unrecognisedHandles);
				if (unrecognisedHandles > m_previousUnknownHandles) {
					LOGINFO("Unrecognised handle count is not the same as last time (there were %lld last time)", m_previousUnknownHandles);
					// TODO: Pass this in rather than using global.
					if (IsNotTooSoonForRefresh()) {
						g_pAddinEnv->CalculateFullRebuild();
						m_lastRefreshTime = GetTickCount64();
					}
				}
				m_previousUnknownHandles = unrecognisedHandles; // save so we can only refresh when situation changes.
			}
		} else {
			LOGERROR ("Collector returned error");
		}
	}
}

HRESULT GarbageCollector::MakeSafeArray (SAFEARRAY **ppsaIds) {
	HRESULT hr;
	//std::wstring str(m_vObservedIds.begin(), m_vObservedIds.end());
	//for (int i = 0; i < m_vObservedIds.size(); i++) {
	//	LOGTRACE("Found element[%d] = %I64u", i, m_vObservedIds[i]);
	//}
	const size_t cElems = m_vObservedIds.size ();
	//LOGTRACE("cElems = %d", cElems);
	*ppsaIds = SafeArrayCreateVector (VT_I8, 0, cElems);
	if (*ppsaIds == NULL) {
		return E_OUTOFMEMORY;
	}
	unsigned hyper *pIdData;
	hr = SafeArrayAccessData (*ppsaIds, (PVOID *)&pIdData);
	if (FAILED (hr)) {
		LOGERROR("SafeArrayAccessData failed");
		SafeArrayDestroy (*ppsaIds);
		return hr;
	}
	const size_t cbElems = cElems * sizeof (hyper);
	memcpy_s (pIdData, cbElems, m_vObservedIds.data (), cbElems);
	hr = SafeArrayUnaccessData (*ppsaIds);
	if (FAILED (hr)) {
		SafeArrayDestroy (*ppsaIds);
		return hr;
	}
	//for (int i = 0; i < m_vObservedIds.size(); i++) {
	//	LOGTRACE("Found element in SAFEARRAY[%d] = %I64u", i, pIdData[i]);
	//}
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
	m_previousUnknownHandles = 0;
	Reset ();
	QueryPerformanceFrequency (&m_liFrequency);
	m_liPreviousRefresh.QuadPart = 0;
	m_lastRefreshTime = 0;
	SetMaxTime (10);
}

GarbageCollector::~GarbageCollector () {
	LOGTRACE("Destructor started");
	m_pCollector->Release ();
	LOGTRACE("Destructor complete");
}

//#include "../utils/TraceOn.h"