<?xml version="1.0"?><doc>
<members>
<member name="T:CJvmConnectorFactory" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\jvmconnector.h" line="13">
<summary>Factory for creating CJvmConnector instances.</summary>
</member>
<member name="T:CJvmConnector" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\jvmconnector.h" line="38">
<summary>Implementation of IJvmSupport.</summary>

<para>Instances of this are created by the ComJvmCreateLocal function or a
CJvmConnectorFactory.</para>
</member>
<member name="T:CAbstractJvm" decl="false" source="c:\users\jim\xl4j\comjvm-win32\core\abstractjvm.h" line="12">
<summary>Partial implementation of IJvm.</summary>
</member>
<member name="T:CClasspath" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\classpath.h" line="12">
<summary>Implementation of IClasspath.</summary>

<para>Each classpath instance has a logical "owner" - typically the identifier
of the JVM that it is being created for. This is used to scope the cache files
to avoid collisions between different applications.</para>
</member>
<member name="M:CJvmConnectorFactory.#ctor" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\jvmconnector.cpp" line="18">
<summary>Creates a new instance.</summary>

<para>This should not be called directly but as a result of calling Instance.</para>
</member>
<member name="M:CJvmConnectorFactory.Dispose" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\jvmconnector.cpp" line="26">
<summary>Destroys an instance.</summary>

<para>This should not be called directly but as a result of calling IUnknown#Release on the instance.</para>
</member>
<member name="M:CJvmConnectorFactory.Instance" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\jvmconnector.cpp" line="34">
<summary>Obtains the existing, or creates a new, instance.</summary>

<returns>The object instance.</returns>
</member>
<member name="T:CJvmConnectorImpl" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\jvmconnector.cpp" line="96">
<summary>Underlying implementation of CJvmConnector.</summary>

<para>Multiple CJvmConnector instances may be created, but they can
only ever work with a single, global, instance of this because of
limitations of the underlying JNI.</para>
</member>
<member name="M:CJvmConnectorImpl.#ctor" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\jvmconnector.cpp" line="211">
<summary>Creates a new instance.</summary>
</member>
<member name="M:CJvmConnectorImpl.Dispose" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\jvmconnector.cpp" line="220">
<summary>Destroys an instance.</summary>
</member>
<member name="M:CJvmConnectorImpl.Lock" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\jvmconnector.cpp" line="227">
<summary>Claims the lock.</summary>

<para>If the lock is already claimed, this will wait for either the lock
to be released or five seconds to elapse since the object was last used.</para>

<returns>S_OK if successful, an error code otherwise</returns>
</member>
<member name="M:CJvmConnectorImpl.Unlock" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\jvmconnector.cpp" line="273">
<summary>Releases the lock.</summary>

<returns>S_OK if successful, an error code otherwise</returns>
</member>
<member name="M:CJvmConnector.#ctor" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\jvmconnector.cpp" line="296">
<summary>Creates a new instance.</summary>
</member>
<member name="M:CJvmConnector.Dispose" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\jvmconnector.cpp" line="302">
<summary>Destroys an instance.</summary>

<para>This should not be called directly but as a result of using IUnknown#Release.</para>
</member>
</members>
</doc>