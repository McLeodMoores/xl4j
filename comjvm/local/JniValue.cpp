/*
 * JVM as a COM object
 *
 * Copyright 2014
 *      Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 *      McLeodMoores Software Limited <jim@mcleodmoores.com>
 *
 * Released under the GNU General Public License.
 */

#include "stdafx.h"
#include "JniValue.h"

void CJniValue::free () {
	switch (type) {
	case t_BSTR :
		v._bstr->Release ();
		break;
	}
}

CJniValue::CJniValue (const CJniValue &copy)
: type (copy.type), v (copy.v) {
	switch (type) {
	case t_BSTR :
		v._bstr->AddRef ();
		break;
	}
}

CJniValue &CJniValue::operator= (const CJniValue &rhs) {
	reset (rhs.type);
	v = rhs.v;
	switch (rhs.type) {
	case t_BSTR :
		v._bstr->AddRef ();
		break;
	}
	return *this;
}

void CJniValue::put_variant (const VARIANT *pvValue) {
	switch (pvValue->vt) {
	case VT_I1 :
		put_jbyte (pvValue->bVal);
		break;
	case VT_I2 :
		put_jshort (pvValue->iVal);
		break;
	case VT_I4 :
		put_jint (pvValue->lVal);
		break;
	case VT_I8 :
		put_jlong (pvValue->llVal);
		break;
	case VT_R4 :
		put_jfloat (pvValue->fltVal);
		break;
	case VT_R8 :
		put_jdouble (pvValue->dblVal);
		break;
	case VT_BOOL :
		put_jboolean (pvValue->boolVal == VARIANT_FALSE ? JNI_FALSE : JNI_TRUE);
		break;
	case VT_BSTR :
		put_BSTR (pvValue->bstrVal);
		break;
	default :
		_com_raise_error (E_NOTIMPL);
	}
}

// TODO: Set up IJObject properly with a local implementation backed by global refs etc.
// TODO: Might mean taking ref handling out of the sequence as it might have little purpose
class CJObject_ReallyNeededSoon : public IUnknown {
private:
	volatile long m_lRefCount;
	~CJObject_ReallyNeededSoon () {
		assert (m_lRefCount == 0);
	}
public:
	CJObject_ReallyNeededSoon ()
		: m_lRefCount (1) {
	}
	// IUnknown
	HRESULT STDMETHODCALLTYPE QueryInterface (
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ _COM_Outptr_ void __RPC_FAR *__RPC_FAR *ppvObject) {
		*ppvObject = NULL;
		return E_NOINTERFACE;
	}
    ULONG STDMETHODCALLTYPE AddRef () {
		return InterlockedIncrement (&m_lRefCount);
	}
    ULONG STDMETHODCALLTYPE Release () {
		long lResult = InterlockedDecrement (&m_lRefCount);
		if (!lResult) delete this;
		return lResult;
	}
};

void CJniValue::get_variant (VARIANT *pvValue) const {
	switch (type) {
	case t_jbyte :
		pvValue->vt = VT_I1;
		pvValue->bVal = v._jbyte;
		break;
	case t_jshort :
		pvValue->vt = VT_I2;
		pvValue->iVal = v._jshort;
		break;
	case t_jint :
		pvValue->vt = VT_I4;
		pvValue->lVal = v._jint;
		break;
	case t_jlong :
		pvValue->vt = VT_I8;
		pvValue->llVal = v._jlong;
		break;
	case t_jfloat :
		pvValue->vt = VT_R4;
		pvValue->fltVal = v._jfloat;
		break;
	case t_jdouble :
		pvValue->vt = VT_R8;
		pvValue->dblVal = v._jdouble;
		break;
	case t_jboolean :
		pvValue->vt = VT_BOOL;
		pvValue->boolVal = v._jboolean ? VARIANT_TRUE : VARIANT_FALSE;
		break;
	case t_jchar :
		pvValue->vt = VT_I2;
		pvValue->iVal = v._jchar;
		break;
	case t_jobject :
		pvValue->punkVal = new CJObject_ReallyNeededSoon ();
		pvValue->vt = VT_UNKNOWN;
		break;
	case t_jsize :
		pvValue->vt = VT_I4;
		pvValue->intVal = v._jsize;
		break;
	case t_BSTR :
		pvValue->bstrVal = v._bstr->copy ();
		pvValue->vt = VT_BSTR;
		break;
	case t_pjchar :
		pvValue->vt = VT_BSTR;
		pvValue->bstrVal = SysAllocString ((const OLECHAR*)v._pjchar);
		if (!pvValue->bstrVal) {
			pvValue->vt = VT_NULL;
			_com_raise_error (E_OUTOFMEMORY);
		}
		break;
	default :
		_com_raise_error (E_NOTIMPL);
	}
}

jint CJniValue::get_jint () const {
	switch (type) {
	case t_jbyte :
		return v._jbyte;
	case t_jshort :
		return v._jshort;
	case t_jint :
		return v._jint;
	case t_jsize :
		return v._jsize;
	}
	_com_raise_error (E_INVALIDARG);
}

jobject CJniValue::get_jobject () const {
	switch (type) {
	case t_jobject :
		return v._jobject;
	}
	_com_raise_error (E_INVALIDARG);
}

CJniValue::CJniValue (BSTR bstr)
: type (t_BSTR) {
	v._bstr = new CBSTRRef (bstr);
}

void CJniValue::get_jvalue (jvalue *pjValue) const {
	switch (type) {
	case t_jbyte :
	case t_jshort :
	case t_jint :
	case t_jlong :
	case t_jfloat :
	case t_jdouble :
	case t_jchar :
	case t_jboolean :
	case t_jobject :
		*pjValue = v._jvalue;
		break;
	default :
		_com_raise_error (E_INVALIDARG);
		break;
	}
}

void CJniValue::put_BSTR (BSTR bstr) {
	CBSTRRef *pValue = new CBSTRRef (bstr);
	reset (t_BSTR);
	v._bstr = pValue;
}

char *CJniValue::get_pchar () const {
	switch (type) {
	case t_BSTR :
		return (char*)v._bstr->pcstr ();
	case t_pchar :
		return v._pchar;
	}
	_com_raise_error (E_INVALIDARG);
}

jsize CJniValue::get_jsize () const {
	switch (type) {
	case t_jbyte :
		if (v._jbyte >= 0) {
			return v._jbyte;
		} else {
			_com_raise_error (E_INVALIDARG);
		}
	case t_jshort :
		if (v._jshort >= 0) {
			return v._jshort;
		} else {
			_com_raise_error (E_INVALIDARG);
		}
	case t_jint :
		if (v._jint >= 0) {
			return v._jint;
		} else {
			_com_raise_error (E_INVALIDARG);
		}
	case t_jlong :
		if ((v._jlong >= 0) && (v._jlong < 0x100000000)) {
			return (jsize)v._jlong;
		} else {
			_com_raise_error (E_INVALIDARG);
		}
	case t_jsize :
		return v._jsize;
	}
	_com_raise_error (E_INVALIDARG);
}

jstring CJniValue::get_jstring () const {
	switch (type) {
	case t_jstring :
		return v._jstring;
	}
	_com_raise_error (E_INVALIDARG);
}

jchar *CJniValue::get_pjchar () const {
	switch (type) {
	case t_BSTR :
		return (jchar*)v._bstr->pcwstr ();
	case t_pjchar :
		return v._pjchar;
	}
	_com_raise_error (E_INVALIDARG);
}

jclass CJniValue::get_jclass () const {
	switch (type) {
	case t_jclass :
		return v._jclass;
	}
	_com_raise_error (E_INVALIDARG);
}

jfieldID CJniValue::get_jfieldID () const {
	switch (type) {
	case t_jfieldID :
		return v._jfieldID;
	}
	_com_raise_error (E_INVALIDARG);
}

jmethodID CJniValue::get_jmethodID () const {
	switch (type) {
	case t_jmethodID :
		return v._jmethodID;
	}
	_com_raise_error (E_INVALIDARG);
}

HRESULT CJniValue::load (std::vector<CJniValue> &aValue) {
	try {
		aValue.push_back (*this);
		type = t_nothing;
		return S_OK;
	} catch (std::bad_alloc) {
		return E_OUTOFMEMORY;
	}
}
