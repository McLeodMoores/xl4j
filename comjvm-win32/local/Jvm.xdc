<?xml version="1.0"?><doc>
<members>
<member name="T:CAbstractJvm" decl="false" source="c:\users\jim\xl4j\comjvm-win32\core\abstractjvm.h" line="12">
<summary>Partial implementation of IJvm.</summary>
</member>
<member name="F:CScan.m_cs" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\cscan.h" line="14">
<summary>Lock for this object.</summary>
</member>
<member name="F:CCall.m_cs" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\ccall.h" line="15">
<summary>Lock for this object.</summary>
</member>
<member name="F:CCollect.m_cs" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\ccollect.h" line="15">
<summary>Lock for this object.</summary>
</member>
<member name="M:CJvm.Execute(=FUNC:System.Int32!System.Runtime.CompilerServices.IsLong(System.Void*,JNIEnv_*),System.Void*)" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\jvm.cpp" line="28">
<summary>Schedules the callback on one of the JVM bound threads.</summary>

<para>If there are no idle threads, one is spawned and attached to the JVM.</para>

<param name="pfnCallback">Callback function</param>
<param name="lpData">Callback function user data</param>
<returns>S_OK if successful, an error code otherwise</returns>
</member>
</members>
</doc>