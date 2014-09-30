#include "stdafx.h"

class Register {
private:
	IJniSequence *m_pJni;
	IJvm *m_pJvm;
	IJvmConnector *m_pConnector;
public:
	Register ();
	~Register ();
	IJniSequence *get_JniSequence ();
	void scanAndRegister (XLOPER12 xDLL);
};