Native code structure
=====================

# Overview
Many developers will not need to ever build the native code portion of XL4J and will usually consume the pre-built binaries by pulling
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
with include and library paths and so on. To create these artifacts, we are  using the ![maven-native-packaging](http://github.com/McLeodMoores/maven-native-packaging) Maven plug-in, created especially for this project.  See the `examples` 
directory in that project for example packaging files.

How it works is that maven pulls the native dependencies from the maven repository in the same way it usually handles Java JARs 
and unpacks them in `comjvm-win32\target\dependency`.  That directory then typically contains:

| Path     | Description      |
|----------|------------------|
| include  | Header files     |
| lib-i386 | 32-bit libraries |
| lib-x64  | 64-bit libraries |

Note that there is not currently any facility for having both debug and release builds of libraries hosted at the same time.

Once maven has pulled and expanded the artifacts, it triggers the build by invoking the `build.bat` file.  This batch file simply 
invokes `msbuild` for each configuration (you can speed up the build by choosing to edit this batch file and skip some configurations).
There are currently four configurations built:

 - Debug/Win32
 - Release/Win32
 - Debug/Win64
 - Release/Win64
 
Any errors at this stage are stored in a log file in the target folder (see the batch file).  It's usually more convenient
to track down build issues by building the solution in Visual Studio, though.

## 64-bit support
A 64-bit build has been maintained since the beginning and included code that should handle the differences, but it's currently not
regularly tested, although when it was tested for the first time recently, it worked without issues.  The intention is to fully 
qualify 64-bit support in later versions and that's likely to be driven by demand, so speak up if it matters to you!

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
| local | Defines the default implementation of the JVM, and marshalls data to and from COM to Java objects using JNI.  Calls are constructed and handed to the JVM via the `jni` project\'s scheduler |
| jni | Defines the low level call scheduler multiple thread-pools to handle synchronous and asynchronous function calls |
| settings | Contains the user MFC-based user interface components needed by XL4J, including the Settings dialog, the splash screen, info dialog and update prompt dialogs |
| helper | Various helper classes that are specific to XL4J |
| utils | Utility classes such as the logging system, date and file handling, etc., that are more general utilities not specific to XL4J | excel-test | Unit tests for excel |
| core-test | Unit tests for core |
| local-test | Unit tests for local and utils |

## Code style
The code is mostly C++ written in a C-like style, so more "C with classes" than modern C++.  There are a few reasons for this:

  * JNI is a C library
  * The Excel XLL interface is C-based
  * Native COM is C-based
  * Pure Win32 calls are usually faster than going via the C++ standard library
  
You may also see some use of Hungarian notation.  While of questionable utility, we generally follow the conventions found in some
of the initial code and general samples from Microsoft.  This is Windows software and uses so many Microsoft-specific technologies
and products; it will never be portable.

Apart from a couple of places, error/success values and returned using the `HRESULT` type rather than relying on C++ exception 
handlers.

A JavaDoc-style format for comments is preferred.

# How it works
The initial entry point is in the `excel` project in `Excel.cpp` in the `xlAutoOpen()` function, which is called when Excel first opens
a sheet.  Assuming the add-in hasn't been initialised by a previous call, we:
  - Manually load the DLLs required.  This is necessary because the add-in directory is not in the DLL search path.  Normally DLLs
    would be loaded automatically but in this case they are not as the linker was set to delay load the required DLLs which means
    they're loaded on demand.  If we load them manually we can specify where they are.  It's possible to determine this by looking
    up the path of the current module (excel.xll in our case) and constructing relative paths for the other DLLs and loading them
    explicitly using `LoadLibrary()`.
  - Perform some version checks.
  - Initialise COM.
  - Create an `CAddinEnvironment` instance and call `Start()` on it which:
    - Puts the add-in environment in a STARTING state.
    - Loads the COM type library, necessary for using the COM type defined in the `core` project (see `core.idl`), implementation 
      are split between the `core` and `local` projects.
    - Reads settings from .INI file and initialise the logging system and any global settings.  Most settings are read from the .INI
      file on every query, so it's important to cache settings in local classes if they're frequently accessed, which will need 
      flushing if the configuration is changed.
    - Registers for some calculation handling events, used for handling asynchronous call cancellations.
    - Creates an Excel<->COM type converter for later use using the type library information.
    - Registers some commands.  Commands are no-argument functions invoked by a user action or event.
    - Schedules Excel to call one of these commands, `RegisterSomeFunctions` in 100ms.
    - Puts the environment into the STARTED state.
  - Create a `CJvmEnvironment` and call `Start()` on it which:
    - Puts the JVM environment in a STARTING state.
    - Puts up the splash screen.
    - Creates a thread running the function `BackgroundJvmThread()` that starts up the JVM.  The JVM environment is passed as an 
      argument.
    - Creates a thread running the function `BackgroundWatchdogThread()` which closes the splash screen if Excel gets stuck 
      (revealing any hidden dialogs).
  - Return control to Excel.  It is important that xlAutoOpen not run for too long, which is why we go to all this performance of
    creating background threads to start the JVM.  Failure to start quickly will result in Excel black-listing the add-in and 
    disabling it.
  - In the background, the JVM thread progresses concurrently with Excel's main event thread:
    - Creates a JVM wrapper object (`Jvm.cpp` in the `excel` project).  This wrapper reads in classpath data from the .INI config file,
      creates a JVM template (which defines the characteristics required by the JVM) and creates the JVM object itself using an
      IJvmConnector instance.  In this case that connector instance is created using `ComJvmCreateLocalConnector`, but will 
      eventually use the standard COM `CoCreateInstance()` function to create a class object.
      - After some plumbing and unpacking of the template, the connector ends up in `JNICreateJavaVMA()` which prepares the 
        JVM arguments and creates another thread (JNIMainThreadProc in `JavaVM.cpp` in the `jni` project) that actually starts the 
        JVM with the appropriate JNI call and waits for execution jobs to be dispatched to it.  The connector then waits for that 
        thread to start up.  Actually, that thread spawns a pool of threads running `JNISlaveThread` that can each take execution
        jobs and run them in the JVM.
    - Once the JVM is up, we create a `FunctionRegistry`, passing it the JVM and call `Scan()`
      - `Scan()` creates an `IScan` instance by calling the factory method on the JVM.  `IScan` defines the interface to a single 
        method COM object `CScan` in the `local` project that queues an Executor (in this case `CScanExecutor`) for dispatch on the JVM.
      - This queue and JVM is implemented in the `jni` project - the majority of the code is in `SlaveThread.cpp` (the JNISlaveThread
        mentioned above).
      - So the slave thread pulls an executor to its internal queue.  The `Run` method in the executor is called with the JNI
        environment as an argument, giving its implementation access to the JVM API.  In this case we look up a large number of
        class handles, method IDs and invoke static factory methods to invoke `registerFunctions()` on the `FunctionRegistry` (on the
        Java side this time).  
        - The Java `FunctionRegistry` will scan for `@XLFunction` and `@XLFunctions` annotations, build up some data structures and
          eventually call a `NativeExcelFunctionEntryAccumulator`, which creates an array of objects with all the function meta-data
          pre-prepared for fast and easy extraction via JNI.
      - We then call `getEntries()` using JNI to get an array of `FunctionEntry` objects.
      - We then convert this Java array element by element into a COM SAFEARRAY of a user-defined struct type called 
        FUNCTIONINFO (defined in core.idl, we have to use its record ID, which get via the COM type library).
      - We then set this SAFEARRAY as the result member on the executor object.  The executor and slave thread then wake up
        the caller, who is waiting on a semaphore.
      - The `Scan()` method can now read the executor's result member and return it to the caller, with the SAFEARRAY possibly
        being marshalled via a network or non-local COM sub-system.
      - The caller, the `FunctionRegistry` (native-side class), just copies the array and keeps it ready to be interrogated.  It 
        will be consumed by Excel in chunks as we will see later.
    - Now in a very similar way as for function registration, a garbage collector COM interface instance is created, this time by 
      calling the `CreateCollect()` method on the JVM object.  In this case we pass this into a `GarbageCollector` instance for 
      invocation at a future moment, but when it is invoked, it is a similar mechanism of using a `CCollect` object and associated
      `CCollectExecutor` to run on the JVM thread pool.
    - We then create an asynchronous call handler and save it in a local member for when we need to call back Excel to notify it
      of a completed auto-asynchronous function call.
  - After 100ms, Excel will invoke the command `RegisterSomeFunctions`.  The stub for this is in `Excel.cpp`, but calls into 
    `JvmEnvironment::_RegisterSomeFunctions()`.  This polls the `FunctionRegistry` (native) to ask if the scan is complete, which 
    is determined by whether or not there is a SAFEARRAY been set on the member by the `Scan()` COM method.  If
    not, we schedule another call to this command in a short time.  Eventually, the registration will have completed, at which 
    point we can tell the `FunctionRegistry` to register some functions.
      - The `RegisterFunctions` method will run for a maximum time of (currently) 100ms before returning.  If there are more functions
        to register it will schedule another call from Excel after a brief delay.  This allows Excel to continue event processing
        without being blocked and allows us to register functions over a longer period than normal.
      - The `RegisterFunctions` method works by maintaining internal state and keep track of execution time by checking the Windows
        performance counter (`QueryPerformanceCounter`).  Once it gets a function to register, it simply unpacks the struct
        and makes a call to the native XLL Excel12() function with the command `xlfRegister`, which is used to register new user-defined
        functions.
  - Once all functions have been registered, rather than scheduling more calls to `RegisterSomeFunctions`, a call to `GarbageCollect`
    is scheduled to periodically perform some incremental garbage collection.
    
Now all the functions have been registered, Excel will allow the user to call them.  So how does that work?
  - We define all functions as taking a varargs list of any Excel type.
  - We create a large number of exported symbols, which get allocated one per user-defined function.  These symbols are named
    `UDF_0`, `UDF_1`, ..., `UDF_2999`.  Each of these is backed by a stub implementation which simply passes its export number to
    a single function (`UDF`), along with all of its arguments.  This single UDF function then passes this data on to the `_UDF` method
    of the `JVMEnvironment` object.
  - The `_UDF` method iterates over the arguments, using the registration metadata (which it can look up using the export number) to
    determine the number of arguments.
  - It creates a COM `SAFEARRAY` of `VARIANT` of the correct length (possibly including an async handle extra parameter).
  - It then gets the COM<->Java type converter from the add-in environment we created at start-up and uses it to convert each argument
    into a COM-equivalent `VARIANT` compatible type, which is then added to the `SAFEARRAY`.
  - Any excess xlNils are trimmed off the array.
  - Create an ICall instance using the JVM interface.  This will typically be an instance of CCall.  An instance is cached in 
    thread-local storage after first use.
  - Call either `Call()` or `AsyncCall()` depending on whether the function is asynchronous or not, passing in the array, export
    number, and a pointer to a `VARIANT` result (not the latter in the asynchronous case, which returns immediately).
  - In the synchronous case, the result, which will be a `VARIANT`, will be converted to an Excel type using the converter and passed
    back to Excel as the method returns.

Within the `Call()` or `AsyncCall()` methods, it creates a `CCallExecutor` instance, which is queued on the JVM thread queue and
eventually, the `Run()` method is called, again in the same was as with `CCollectExector` and `CScanExecutor`.  This `Run()` method
is passed the JVM environment so it can now access JNI functions.  It uses a lazily-initialised `JniCache` object to minimise the 
number of calls to `FindClass`, `GetMethodID` and so on (which should eventually be used by the other *Executor classes).

The arguments to `Call()` and `AsyncCall()` are both the export number and `SAFEARRAY` of `VARIANT`.  These are converted into 
appropriate Java objects using the `ComJavaConverter` class in the `local` project.  This uses the `JniCache` to create instances
of the classes in ![com.mcleodmoores.xl4j.v1.api.values.\*](https://github.com/McLeodMoores/xl4j/tree/master/xll-core/src/main/java/com/mcleodmoores/xl4j/v1/api/values) and add them to an `Object[]` for passing to Java.  Once all the arguments
are processed, the method `invoke` on the instance of `ExcelFunctionCallHandler` returned by the `Excel` instance, is called.  The
`Excel` instance is returned by the `ExcelFactory.getInstance()` factory method and cached after first invocation.  The returned Java
handle is then converted back to a COM `VARIANT` type, again using the `ComJavaConverter`, and returned back to the caller (again 
over a COM boundary, so possibly over a remote or non-local call).

If the result is asynchronous, the caller will not be waiting, so we can now use the `AsyncCallHandler` object we created in the 
AddInEnvironment back at start-up.
