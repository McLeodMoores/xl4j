#include "stdafx.h"
#include "helper/JniSequenceHelper.h"
#include <vector>

#ifdef COMJVM_EXCEL_EXPORT
# define COMJVM_EXCEL_API __declspec(dllexport)
#else
# define COMJVM_EXCEL_API __declspec(dllimport)
#endif /* ifndef COMJVM_DEBUG_API */

class COMJVM_EXCEL_API Jvm {
private:
	volatile ULONG m_lRefCount;
	IJvm *m_pJvm;
	IJvmConnector *m_pConnector;
public:
	Jvm ();
	~Jvm ();
	ULONG AddRef ();
	ULONG Release ();
	IJvm *getJvm () { return m_pJvm; }
};