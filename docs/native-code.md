Native code structure
=====================

# Overview
Many developers will not need to ever build the native code portion of XL4J and will usually consume the pre-built biniaries by pulling
in zip files using Maven and expanding them into the final packaging folder, but for those interested in contributing new low level 
features, adding custom native functionality (e.g. native UDFs for extra performance), this guide is for you.

The native code part of XL4J is stored in the comjvm-win32 sub-project in the XL4J source distribution.  The build process is kicked off 
by the Java build tool Maven.  Maven will only attempt a native build if you have Visual Studio installed.  If you do not, it will
completely skip this step and use the pre-built binary in Maven.  This means developers who don't need to rebuild the native code, do
not need to install Visual Studio or any native SDKs to get started.

# Using Visual Studio
Because Excel doesn't appear to support the newer style of C Runtime package, and to avoid users needing to install the C runtime 
redistributable (sorry Microsoft, but it's too complicated), it is necessary to build using the Visual Studio 2013 runtime libraries.
To keep the code up to date, we are Visual Studio 2015 to compile against the 2013 runtime.  This means you presently need to install 
both VS2013 and VS2015.

Once you have Visual Studio installed, Maven will detect that and find and download all the libraries and SDKs you need, pre-packaged 
by us as Maven artifacts.  This frees the you from needing to install a range of SDKs and libraries in specific locations and fiddle
with include and library paths and so on. To create these artifacts, we are  using the [maven-native-packaging]
(http://github.com/McLeodMoores/maven-native-packaging) Maven plug-in, created especially for this project.  See the `examples` 
directory in that project for example packaging files.

How it works is that maven pulls the native dependencies from the maven repository in the same way it usually handles Java JARs 
and unpacks them in `comjvm-win32\target\dependency`.  That directory then typically contains:

| Path     | Description      |
|----------|------------------|
| include  | Header files     |
| lib-i386 | 32-bit libraries |
| lib-x64  | 64-bit libraries |

When maven has pulled and expanded the artifacts, it triggers the build by invoking the `build.bat` file.  This batch file simply 
invokes msbuild for each configuration (you can speed up the build by choosing to edit this batch file and skip some configurations).
There are currently four configurations built:

 - Debug/Win32
 - Release/Win32
 - Debug/Win64
 - Release/Win64
 
Note that any errors at this stage are stored in a log file in the target folder (see the batch file).  It's usually more convenient
to track down build issues by building the solution in visual studio though.

## 64-bit support
We have maintained a 64-bit build since the beginning and included code that should handle the differences, but it's currently not
regularly tested, although when we have tried it, it has worked without issues.  The intention is to fully qualify 64-bit support 
in later versions and that's likely to be driven by demand, so speak up if it matters to you!

## Unicode
You may find evidence of ANSI/ASCII support in the code too - originally there were ASCII/ANSI variants of each build but we've 
decided to ditch that for Unicode only support.  Most of the code still uses the TCHAR types and functions that work as either ASCII or
Unicode builds but over time we'll migrate away from that.

# Opening in VS2015
You can open the native code solution simply by opening the `comjvm.sln` file in Visual Studio.  This will open all the projects that
make up the add-in.

# Solution Structure
| Project Name | Description |
|--------------|-------------|
| excel | Covers the XLL add-in interface between Excel and XL4J.  It is this project that handles incoming function calls, lifecycle calls and defines objects to encapsulate the state and lifecycle of the Add-in. |
| core | Defines all the COM interfaces used to talk to the JVM, and the common infrastructure classes for the JVM to support Classpaths, JVM discovery, JVM options, and so on. |
| local | Defines the default implementation of the JVM, and marshalls data to and from COM to Java objects using JNI.  Calls are constructed and handed to the JVM via the `jni` project\`s scheduler |
| jni | Defines the low level call scheduler multiple thread-pools to handle synchronous and asynchronous function calls |
| settings | Contains the user MFC-based user interface components needed by XL4J, including the Settings dialog, the splash screen, info dialog and update prompt dialogs |
| helper | Various helper classes that are specific to XL4J. |
| utils | Utility classes such as the logging system, date and file handling, etc, that are more general utilities not specific to XL4J | excel-test | Unit tests for excel |
| core-test | Unit tests for core |
| local-test | Unit tests for local and utils |

