#include "stdafx.h"
#include "Jvm.h"
#include "TypeLib.h"
#include "local/CScanExecutor.h"
#include <vector>

//#ifdef COMJVM_EXCEL_EXPORT
//# define COMJVM_EXCEL_API __declspec(dllexport)
//#else
//# define COMJVM_EXCEL_API __declspec(dllimport)
//#endif /* ifndef COMJVM_DEBUG_API */

class /*COMJVM_EXCEL_API*/ Converter {
private:
	HRESULT allocMREF (size_t ranges, XLMREF12 **result);
	HRESULT allocARRAY (size_t cols, size_t rows, XLOPER12 **arr);
	HRESULT allocBSTR (XCHAR *str, BSTR *out);
	HRESULT allocXCHAR (BSTR in, XCHAR **out);
	HRESULT allocMultiReference (XL4JMULTIREFERENCE **result, size_t elems);
	HRESULT allocReference (XL4JREFERENCE **result);
	IRecordInfo *m_pMultiReferenceRecInfo;
	IRecordInfo *m_pLocalReferenceRecInfo;

public:
	Converter (TypeLib *pTypeLib);
	~Converter ();
	HRESULT convert (VARIANT *in, XLOPER12 *out);
	HRESULT convert (XLOPER12 *in, VARIANT *out);
};