<?xml version="1.0"?><doc>
<members>
<member name="T:CClasspath" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\classpath.h" line="12">
<summary>Implementation of IClasspath.</summary>

<para>Each classpath instance has a logical "owner" - typically the identifier
of the JVM that it is being created for. This is used to scope the cache files
to avoid collisions between different applications.</para>
</member>
<member name="T:CJarAndClassCache" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\cache.h" line="10">
<summary>Locally cached JAR and class files.</summary>
</member>
<member name="M:CJarAndClassCache.get_FileCount" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\cache.h" line="24">
<summary>Gets the number of files cached.</summary>
</member>
<member name="M:CJarAndClassCache.set_FileCount(System.Int32!System.Runtime.CompilerServices.IsLong)" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\cache.h" line="26">
<summary>Sets the number of files in this cache.</summary>
</member>
<member name="M:CJarAndClassCache.get_FileSize" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\cache.h" line="28">
<summary>Gets the total size of the cached file(s).</summary>
</member>
<member name="M:CJarAndClassCache.set_FileSize(System.Int32!System.Runtime.CompilerServices.IsLong)" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\cache.h" line="30">
<summary>Sets the total size of the cached file(s).</summary>
</member>
<member name="M:CJarAndClassCache.get_Timestamp" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\cache.h" line="32">
<summary>Gets the timestamp information.</summary>
</member>
<member name="M:CJarAndClassCache.set_Timestamp(System.UInt64)" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\cache.h" line="34">
<summary>Sets the timestamp information.</summary>
</member>
<member name="M:CJarAndClassCache.get_LocalPath" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\cache.h" line="36">
<summary>Gets the local path where the cached file(s) reside.</summary>
</member>
<member name="M:CClasspath.#ctor(std.basic_string&lt;System.Char,std.char_traits{System.Char},std.allocator&lt;System.Char&gt;&gt;!System.Runtime.CompilerServices.IsConst*!System.Runtime.CompilerServices.IsImplicitlyDereferenced)" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\classpath.cpp" line="27">
<summary>Creates a new instance.</summary>

<param name="strOwner">Logical JVM identifier owning this classpath instance</param>
</member>
<member name="M:CClasspath.Dispose" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\classpath.cpp" line="36">
<summary>Destroys an instance.</summary>

<para>This should not be called directly but as a result of calling IUnknown#Release on the instance.</para>
</member>
<member name="M:CClasspath.GetPathComponents" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\classpath.cpp" line="58">
<summary>Fetches the components from the classpath.</summary>

<returns>Classpath components, in the order they should be present on the path.</returns>
</member>
<member name="M:CClasspath.GetPath" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\classpath.cpp" line="66">
<summary>Fetches the full classpath.</summary>

<para>This returns the local path in the same form as get_LocalPath but as a heap
allocated PTSTR instead of a BSTR. The caller must free this memory when done.</para>

<returns>The classpath</returns>
</member>
</members>
</doc>