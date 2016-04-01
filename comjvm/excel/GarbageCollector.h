#include "stdafx.h"
#include "Jvm.h"
#include <vector>

class GarbageCollector {
private:
	//RW g_rw;
	//COL g_col;
	//IDSHEET g_sheet;

	// for ScanDocuments
	XLOPER12 m_documents;
	int m_cDocs;
	int m_iDoc;
	XLOPER12 *m_pWorkbookName;
	// for ScanWorkbook
	XLOPER12 *m_pCurrentWorkbookName;
	XLOPER12 m_sheets;
	int m_cSheets;
	int m_iSheet;
	XLOPER12 *m_pSheetName;

	// for ScanSheet
	XLOPER12 m_firstRow;
	XLOPER12 m_lastRow;
	XLOPER12 m_firstCol;
	XLOPER12 m_lastCol;
	XLOPER12 m_sheetId;
	XLOPER12 m_wholeRow;
	XLMREF12 m_xlmRef;
	int m_iRow;

	// for timing
	LARGE_INTEGER m_liLatestTime;
	LARGE_INTEGER m_liFrequency;
	LARGE_INTEGER m_liMaxTicks;

	// list observed ids
	std::vector<hyper> m_vObservedIds;
	// Java interface
	ICollect *m_pCollector;

	bool IsTimeUp ();
	HRESULT MakeSafeArray (SAFEARRAY **ppsaIds);
	hyper ParseId (XCHAR *pExcelStr);
	void Reset ();
	void ScanCell (XLOPER12 *cell);
	bool ScanCells (int cols, int rows, XLOPER12 *arr);
	bool ScanSheet (XLOPER12 *pWorkbookName, XLOPER12 *pSheetName);
	bool ScanWorkbook (XLOPER12 *pWorkbookName);
	bool ScanDocuments ();
public:
	GarbageCollector (ICollect *pCollector);
	
	void SetMaxTime (int iMillis);
	void Collect ();
};