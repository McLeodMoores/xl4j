<?xml version="1.0"?><doc>
<members>
<member name="T:CClasspath" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\classpath.h" line="12">
<summary>Implementation of IClasspath.</summary>

<para>Each classpath instance has a logical "owner" - typically the identifier
of the JVM that it is being created for. This is used to scope the cache files
to avoid collisions between different applications.</para>
</member>
<member name="T:CJvmConnectorFactory" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\jvmconnector.h" line="13">
<summary>Factory for creating CJvmConnector instances.</summary>
</member>
<member name="T:CJvmConnector" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\jvmconnector.h" line="38">
<summary>Implementation of IJvmSupport.</summary>

<para>Instances of this are created by the ComJvmCreateLocal function or a
CJvmConnectorFactory.</para>
</member>
<member name="M:ComJvmCreateClasspathA(System.SByte!System.Runtime.CompilerServices.IsSignUnspecifiedByte!System.Runtime.CompilerServices.IsConst*,IClasspath**)" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\local.cpp" line="17">
<summary>Creates an IClasspath instance for building a local classpath.</summary>

<para>The JVM created by this library will be in-process so any classpath entries must be
local. If remote IClasspathEntry instances are used (for example, files that might be local
to the JVM consumer but are not immediately accessible as a local path for the JVM
implementation) then the files may be copied to a local area.</para>

<para>Note that the JVM identifier is used to scope locally cached classpath entries; it is
not necessary for that JVM to actually exist at this point.</para>

<param name="pszOwner">Identifier of the JVM this instance is being created for</param>
<param name="ppClasspath">Receives the classpath instance</param>
<returns>S_OK if successful, an error code otherwise</returns>
</member>
<member name="M:ComJvmCreateClasspathW(System.Char!System.Runtime.CompilerServices.IsConst*,IClasspath**)" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\local.cpp" line="41">
<summary>Creates an IClasspath instance for building a local classpath.</summary>

<para>The JVM created by this library will be in-process so any classpath entries must be
local. If remote IClasspathEntry instances are used (for example, files that might be local
to the JVM consumer but are not immediately accessible as a local path for the JVM
implementation) then the files may be copied to a local area.</para>

<para>Note that the JVM identifier is used to scope locally cached classpath entries; it is
not necessary for that JVM to actually exist at this point.</para>

<param name="pszOwner">Identifier of the JVM this instance is being created for</param>
<param name="ppClasspath">Receives the classpath instance</param>
<returns>S_OK if successful, an error code otherwise</returns>
</member>
<member name="M:ComJvmCreateDirectoryWriterA(System.SByte!System.Runtime.CompilerServices.IsSignUnspecifiedByte!System.Runtime.CompilerServices.IsConst*,IDirectoryWriter**)" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\local.cpp" line="130">
<summary>Creates an IDirectoryWriter instance.</summary>

<para>This is the ANSI form.</para>

<param name="pszPath">Base folder of the target filesystem area</param>
<param name="ppWriter">Receives the new instance</param>
<returns>S_OK if successful, an error code otherwise</returns>
</member>
<member name="M:ComJvmCreateDirectoryWriterW(System.Char!System.Runtime.CompilerServices.IsConst*,IDirectoryWriter**)" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\local.cpp" line="155">
<summary>Creates an IDirectoryWriter instance.</summary>

<para>This is the wide-character form.</para>

<param name="pszPath">Base folder of the target filesystem area</param>
<param name="ppWriter">Receives the new instance</param>
<returns>S_OK if successful, an error code otherwise</returns>
</member>
<member name="M:ComJvmCreateFileWriterA(System.SByte!System.Runtime.CompilerServices.IsSignUnspecifiedByte!System.Runtime.CompilerServices.IsConst*,IFileWriter**)" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\local.cpp" line="180">
<summary>Creates an IFileWriter instance.</summary>

<para>This is the ANSI form.</para>

<param name="pszPath">Target file to write to</param>
<param name="ppWriter">Receives the new instance</param>
<returns>S_OK if successful, an error code otherwise</returns>
</member>
<member name="M:ComJvmCreateFileWriterW(System.Char!System.Runtime.CompilerServices.IsConst*,IFileWriter**)" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\local.cpp" line="217">
<summary>Creates an IFileWriter instance.</summary>

<para>This is the wide-character form.</para>

<param name="pszPath">Target file to write to</param>
<param name="ppWriter">Receives the new instance</param>
<returns>S_OK if successful, an error code otherwise</returns>
</member>
<member name="M:ComJvmCreateLocalConnector(IJvmConnector**)" decl="false" source="c:\users\jim\xl4j\comjvm-win32\local\local.cpp" line="254">
<summary>Creates a connector to an in-process JVM.</summary>

<para>This is only a partial implementation of the full IJvmConnector
contract as it is only possible to load a single JVM into any given
process.</para>

<param name="ppConnector">Receives the connector instance</param>
<returns>S_OK if successful, an error code otherwise</returns>
</member>
</members>
</doc>