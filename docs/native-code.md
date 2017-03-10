Native code structure
=====================

# Overview
Many developers will not need to ever build the native code portion of XL4J and will usually consume the pre-built biniaries by pulling
in zip files using Maven and expanding them into the final packaging folder, but for those interested in contributing new low level 
features, adding custom native functionality (e.g. native UDFs for extra performance), this guide is for you.

The native code part of XL4J is stored in the comjvm-win32 sub-project in the XL4J source distribution.  To free the developer from
needing to install a range of SDKs and libraries in specific locations, dependent libraries have been pre-packaged as Maven artifacts. 
Maven is normally used to handle Java artifacts, but there's no reason it can\'t handle native artifacts too.  To create these artifacts,
we are  using the [maven-native-packaging](http://github.com/McLeodMoores/maven-native-packaging) Maven plug-in, created especially for
this project.

# Build process
When maven has pulled and expanded the artifacts, it triggers the build by invoking the `build.bat` file.  This batch file simply 
invokes msbuild for each configuration (you can speed up the build by choosing to edit this batch file and skip some configurations).
There are currently four configurations built:

 - Debug/Win32
 - Release/Win32
 - Debug/Win64
 - Release/Win64
 
## 64-bit support
We have maintained a 64-bit build since the beginning and included code that should handle the differences, but it's important to note 
it's currently completely untested and so is likely to be non-functional at the moment.  The intention is to fully qualify 64-bit support
in later versions and that's likely to be driven by demand, so speak up if it matters to you!

## Unicode
You may find evidence of ANSI/ASCII support in the code too - originally there were ASCII/ANSI variants of each build but we've decided to
ditch that for Unicode only support.  Most of the code still uses the TCHAR types and functions that work as either ASCII or Unicode 
builds but over time we'll migrate away from that.


