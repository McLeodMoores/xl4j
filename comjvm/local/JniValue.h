/*
 * JVM as a COM object
 *
 * Copyright 2014 by Andrew Ian William Griffin <griffin@beerdragon.co.uk>
 * Released under the GNU General Public License.
 */

#pragma once

/// <summary>Value placeholder for a intermediate results during a JNI sequence execution.</summary>
class CJniValue {
private:

	/// <summary>Type of value held.</summary>
	enum _type {
		/// <summary>No value.</summary>
		t_nothing = 0,
		/// <summary>Value held in v._jbyte.</summary>
		t_jbyte = 1,
		/// <summary>Value held in v._jshort.</summary>
		t_jshort,
		/// <summary>Value held in v._jint.</summary>
		t_jint,
		/// <summary>Value held in v._jlong.</summary>
		t_jlong,
		/// <summary>Value held in v._jfloat.</summary>
		t_jfloat,
		/// <summary>Value held in v._jdouble.</summary>
		t_jdouble,
		/// <summary>Value held in v._jchar.</summary>
		t_jchar,
		/// <summary>Value held in v._jboolean.</summary>
		t_jboolean,
		/// <summary>Value held in v._BSTR.</summary>
		t_BSTR,
		/// <summary>Value held in v._pchar.</summary>
		t_pchar,
		/// <summary>Value held in v._jsize.</summary>
		t_jsize,
		/// <summary>Value held in v._jstring.</summary>
		t_jstring,
		/// <summary>Value held in v._pjchar.</summary>
		t_pjchar,
		/// <summary>Value held in v._jclass.</summary>
		t_jclass
	} type;

	class CBSTRRef {
	private:
		int m_cRefCount;
		_bstr_t m_bstr;
		~CBSTRRef () {
			assert (m_cRefCount == 0);
		}
	public:
		CBSTRRef (BSTR bstr)
		: m_cRefCount (1), m_bstr (bstr) {
		}
		void AddRef () {
			m_cRefCount++;
		}
		void Release () {
			if (--m_cRefCount == 0) {
				delete this;
			}
		}
		BSTR bstr () { return m_bstr.GetBSTR (); }
		BSTR copy () { return m_bstr.copy (); }
		PCSTR pcstr () { return (PCSTR)m_bstr; }
		PCWSTR pcwstr () { return (PCWSTR)m_bstr; }
	};

	/// <summary>Value held.</summary>
	///
	/// <para>Most values are held directly with no allocation, copying, or releasing
	/// concerns. The JNI sequence must, for example, include correct calls to the
	/// operations to deallocate memory or return it to the JVM to avoid resource
	/// leaks. Where this is not the case, it is noted in the documentation for each
	/// element.</para>
	union {
		/// <summary>Java primitive byte.</summary>
		jbyte _jbyte;
		/// <summary>Java primitive short.</summary>
		jshort _jshort;
		/// <summary>Java primitive integer.</summary>
		jint _jint;
		/// <summary>Java primitive long.</summary>
		jlong _jlong;
		/// <summary>Java primitive float.</summary>
		jfloat _jfloat;
		/// <summary>Java primitive double.</summary>
		jdouble _jdouble;
		/// <summary>Java primitive boolean.</summary>
		jboolean _jboolean;
		/// <summary>Java primitive char.</summary>
		jchar _jchar;
		/// <summary>Platform string.</summary>
		///
		/// <para>The instance here is reference counted and shared by multiple
		/// CJniValue instances.</para>
		CBSTRRef *_bstr;
		/// <summary>Pointer to platform characters.</summary>
		char *_pchar;
		/// <summary>Java size reference.</summary>
		jsize _jsize;
		/// <summary>JVM string.</summary>
		jstring _jstring;
		/// <summary>Pointer to Java primitive characters.</summary>
		jchar *_pjchar;
		/// <summary>Java class reference.</summary>
		jclass _jclass;
	} v;

	void free ();
	void reset (_type typeNew) { free (); type = typeNew; }
public:
	CJniValue (const CJniValue &copy);
	CJniValue () : type (t_nothing) { }
	~CJniValue () { free (); }
	void put_nothing () { reset (t_nothing); }
	void put_variant (const VARIANT *pvValue);
	void get_variant (VARIANT *pvValue) const;
	CJniValue &operator= (const CJniValue &rhs);
#define __MTD2(_tFormal, _tImpl) \
	void put_##_tImpl (_tFormal value) { reset (t_##_tImpl); v._##_tImpl = value; } \
	_tFormal get_##_tImpl () const;
#define __MTD(_t) __MTD2(_t, _t)
#define __CONS(_t) \
	__MTD(_t) \
	CJniValue (_t value) : type (t_##_t) { v._##_t = value; }
	__CONS (jbyte)
	__CONS (jshort)
	__CONS (jint)
	__CONS (jlong)
	__CONS (jfloat)
	__CONS (jdouble)
	__MTD (jchar)
	__MTD (jboolean)
	void put_BSTR (BSTR bstr);
	CJniValue (BSTR bstr);
	__MTD2 (char *, pchar)
	__MTD (jsize)
	__MTD (jstring)
	__MTD2 (jchar *, pjchar)
	__MTD (jclass)
#undef __CONS
#undef __MTD
#undef __MTD2
	HRESULT load (std::vector<CJniValue> &aValue);
};
