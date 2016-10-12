<?xml version="1.0"?><doc>
<members>
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
<member name="M:CJarAndClassCache.#ctor(std.basic_string&lt;System.Char,std.char_traits{System.Char},std.allocator&lt;System.Char&gt;&gt;!System.Runtime.CompilerServices.IsConst*!System.Runtime.CompilerServices.IsImplicitlyDereferenced,std.basic_string&lt;System.Char,std.char_traits{System.Char},std.allocator&lt;System.Char&gt;&gt;!System.Runtime.CompilerServices.IsConst*!System.Runtime.CompilerServices.IsImplicitlyDereferenced,std.basic_string&lt;System.Char,std.char_traits{System.Char},std.allocator&lt;System.Char&gt;&gt;!System.Runtime.CompilerServices.IsConst*!System.Runtime.CompilerServices.IsImplicitlyDereferenced)" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\cache.cpp" line="66">
<summary>Creates a new instance.</summary>

<param name="strOwner">Identifier of the caching context</param>
<param name="strHost">Name of the host these files are cached for</param>
<param name="strHostPath">Path to the files, local to the host</param>
</member>
<member name="M:CJarAndClassCache.Dispose" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\cache.cpp" line="90">
<summary>Destroys an instance.</summary>
</member>
<member name="M:CJarAndClassCache.Clear" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\cache.cpp" line="94">
<summary>Empties this cache.</summary>

<returns>S_OK if successful, an error code otherwise</returns>
</member>
<member name="M:CJarAndClassCache.Flush" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\cache.cpp" line="106">
<summary>Ensures that any data in memory is flushed to disk.</summary>

<returns>S_OK if succesful, an error code otherwise</returns>
</member>
</members>
</doc>