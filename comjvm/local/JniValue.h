


/// <summary>Type of value held.</summary>
enum vtype {
	/// <summary>No value.</summary>
	t_nothing = 0,
	/// <para>primitive(ish) java types (jsize == jint)</para>
	/// <summary>Value held in v._jvalue.z</summary>
	t_jbyte = 1,
	/// <summary>Value held in v._jvalue.s</summary>
	t_jshort,
	/// <summary>Value held in v._jvalue.i</summary>
	t_jint,
	/// <summary>Value held in v._jvalue.j</summary>
	t_jlong,
	/// <summary>Value held in v._jvalue.z</summary>
	t_jboolean,
	/// <summary>Value held in v._jvalue.c</summary>
	t_jchar,
	/// <summary>Value held in v._jvalue.f</summary>
	t_jfloat,
	/// <summary>Value held in v._jvalue.d</summary>
	t_jdouble,
	// these are all in _jvalue.l (object)
	/// <summary>Value held in v._jvalue.l</summary>
	t_jstring,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jobject,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jclass,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jobjectRefType,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jthrowable,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jobjectArray,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jbooleanArray,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jbyteArray,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jcharArray,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jshortArray,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jintArray,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jlongArray,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jfloatArray,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jdoubleArray,
	/// <summary>Value held in v._jvalue.l</summary>
	t_jweak,
	/// <summary>Value held in v._jvalue.l</summary>

	/// <para>these cannot be passed into a Java Method or Constructor
	/// but can be parameters to JNI calls</para>
	/// <summary>Value held in v._pjchar</summary>
	t_pjchar,
	/// <summary>Value held in v._pchar</summary>
	t_pchar,
	/// <summary>Value held in v._bstr</summary>
	t_BSTR,
	/// <summary>Value held in v._HANDLE</summary>
	t_HANDLE,
	/// <summary>Value held in v._methodID</summary>
	t_jmethodID,
	/// <summary>Value held in v._fieldID</summary>
	t_jfieldID,
	/// <summary>Value held in v._jbyteBuffer</summary>
	t_jbyteBuffer,
	/// <summary>Value held in v._jsize</summary>
	t_jsize,
};

class CJniValue {
private:
	vtype type;

	class CBSTRRef {
	private:
		int m_cRefCount;
		_bstr_t m_bstr;
		~CBSTRRef () {
			assert (m_cRefCount == 0);
		}
	public:
		CBSTRRef (BSTR bstr) : m_cRefCount (1), m_bstr (bstr) {
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
	union {
		// a COM string, converted on demand into a jstring.
		CBSTRRef *_bstr;
		// _HANDLE stores all the reference types when going via VARIANT
		// which is simple, but obviously loses type safety completely.
		// we use a ULONGLONG because HANDLE will differ in length across
		// 32-bit/64-bit client/server boundaries and could lead to very
		// difficult to debug pointer truncations.
		ULONGLONG _HANDLE;
		// this is itself a union (see java's jni.h), holds most of the java types
		jvalue _jvalue; // itself a union, see jni.h
		jsize _jsize;
		jmethodID _jmethodID;
		jfieldID _jfieldID;
		jobjectRefType _jobjectRefType;
		jchar *_pjchar;
		char *_pchar;
		struct __jbyteBuffer {
			jbyte *_pjbyte;
			jsize _jsize;
		} _jbyteBuffer;
	} v;
	void free ();
	void reset (vtype typeNew) { free (); type = typeNew; }
public:
	CJniValue (const CJniValue &copy);
	CJniValue () : type (t_nothing) { }
	~CJniValue () { free (); }
	void put_nothing () { reset (t_nothing); }
	void put_variant (const VARIANT *pvValue);
	void get_variant (VARIANT *pvValue) const;
	CJniValue &operator= (const CJniValue &rhs);
	void get_jvalue (jvalue *pValue) const;

	/// <summary>these are specialised so they access the jvalue embedded union</summary>
#define __GETPRIMITIVE(_t, _field) \
	_t get_##_t () const { \
		switch (type) { \
		case t_##_t: \
			return v._jvalue.##_field; \
			} \
		assert(0); \
		return v._jvalue.##_field; \
		}
#define __PUTPRIMITIVE(_t,_field) \
	void put_##_t (_t value) { \
	  reset (t_##_t); \
	  v._jvalue.##_field = value; \
			}

#define __CONSPRIMITIVE(_t,_field) \
	__PUTPRIMITIVE(_t,_field) \
	CJniValue (_t value) : type (t_##_t) { \
		v._jvalue.##_field = value; \
			}

	/// <summary>these are for anything bunged in the _HANDLE field.</summary>
#define __GETHANDLE(_t) \
	_t get_##_t () const { \
		switch (type) { \
		case t_##_t: \
			return (_t) v._jvalue.l; \
		case t_HANDLE: \
		    return (_t) v._HANDLE; \
			} \
		assert(0); \
		return (_t) v._jvalue.l; \
		}

#define __PUTHANDLE(_t) \
	void put_##_t (_t value) { \
		reset (t_##_t); \
		v._jvalue.l = value; \
		}

#define __GET(_t) \
	_t get_##_t () const { \
		switch (type) { \
		case t_##_t: \
			return v._##_t; \
				} \
		assert(0); \
		return v._##_t; \
			}
#define __PUT(_t) \
	void put_##_t (_t value) { \
		reset (t_##_t); \
		v._##_t = value; \
		}
#define __CONS(_t) \
	__PUT(_t) \
	CJniValue (_t value) : type (t_##_t) { v._##_t = value; }
	__CONSPRIMITIVE (jint, i);
	jint get_jint () const;
	__PUTPRIMITIVE (jsize, i); // CONS clashes with jint because typedef
	jint get_jsize () const;
	__CONSPRIMITIVE (jboolean, z);
	__GETPRIMITIVE (jboolean, z);
	__CONSPRIMITIVE (jbyte, b);
	__GETPRIMITIVE (jbyte, b);
	__CONSPRIMITIVE (jchar, c);
	__GETPRIMITIVE (jchar, c);
	__CONSPRIMITIVE (jshort, s);
	__GETPRIMITIVE (jshort, s);
	__CONSPRIMITIVE (jlong, j);
	__GETPRIMITIVE (jlong, j);
	__CONSPRIMITIVE (jfloat, f);
	__GETPRIMITIVE (jfloat, f);
	__CONSPRIMITIVE (jdouble, d);
	__GETPRIMITIVE (jdouble, d);
	CJniValue (BSTR bstr);
	void put_BSTR (BSTR bstr);
	void put_pjchar (jchar *value) { reset (t_pjchar); v._pjchar = value; }
	jchar *get_pjchar () const;
	void put_pchar (char *value) { reset (t_pchar); v._pchar = value; }
	char *get_pchar () const;
	CJniValue (jbyte *buffer, jsize size);
	__PUTHANDLE (jobject);
	__GETHANDLE (jobject);
	__PUTHANDLE (jclass);
	__GETHANDLE (jclass);
	__PUTHANDLE (jthrowable);
	__GETHANDLE (jthrowable);
	__PUTHANDLE (jobjectArray);
	__GETHANDLE (jobjectArray);
	__PUTHANDLE (jbooleanArray);
	__GETHANDLE (jbyteArray);
	__PUTHANDLE (jbyteArray);
	__GETHANDLE (jcharArray);
	__PUTHANDLE (jcharArray);
	__GETHANDLE (jshortArray);
	__PUTHANDLE (jshortArray);
	__GETHANDLE (jintArray);
	__PUTHANDLE (jintArray);
	__GETHANDLE (jlongArray);
	__PUTHANDLE (jlongArray);
	__GETHANDLE (jfloatArray);
	__PUTHANDLE (jfloatArray);
	__GETHANDLE (jdoubleArray);
	__PUTHANDLE (jdoubleArray);
	__GETHANDLE (jweak);
	__PUTHANDLE (jweak);
	__PUT (jobjectRefType);
	jobjectRefType get_jobjectRefType_t () const;
	__PUT (jmethodID);
	__GET (jmethodID);
	__PUT (jfieldID);
	__GET (jfieldID);

	__PUTHANDLE (jstring);
	jstring get_jstring () const;

	void put_HANDLE (ULONGLONG handle);
	void put_jbyteBuffer (jbyte *buffer, jsize size);
	jbyte *get_jbyteBuffer () const;
	jsize get_jbyteBufferSize () const;

#undef __CONS
#undef __PUT
	// TODO undef all the other crazy macros
	HRESULT load (std::vector<CJniValue> &aValue);
};