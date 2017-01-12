Settings
========

XL4J stores all its settings in an `.ini` style file rather than the registry. This makes it easier for developers and admins to manage 
the configuration and makes it less likely that there will be issues with permissions.  To make configuration easier to manage, you can
edit all configuration via the in-built settings panel, which you can access via the Add-in toolbar:

![Toolbar](https://github.com/McLeodMoores/xl4j/blob/master/docs/images/toolbar.png "The default toolbar")

Clicking the Settings icon will open the dialog.

## General Add-in Settings

![Settings (Add-in)](https://github.com/McLeodMoores/xl4j/blob/master/docs/images/settings-add-in.PNG "The settings dialog (add-in tab)")

The default tab here is for options relating to the whole Add-in:
 * General Options
   - Enable/disable Garbage Collection **THIS OPTION DOESN'T CURRENTLY DO ANYTHING**
   - Enable/disable Toolbar - this turns off the toolbar, which you might like to do to prevent end users from changing options.  Note 
     that if you turn the toolbar off, the only way to turn it back on is by editing the configuration file by hand.
 * C++ Logging Options
   - Radio buttons to determine whether to send C++ logging to a file (opened via the toolbar), or to Windows Debug, which can be viewed
     using visual studio or DebugView.exe.  The latter is quite convenient, although the performance leaves something to be desired.
   - A combo box allowing you to choose the logging level.  TRACE is the most detailed and FATAL is the least. It is recommended not to
     have anything below WARN during normal operation as the logging causes a significant performance hit.

## Classpath Settings
  
![Settings (Classpath)](https://github.com/McLeodMoores/xl4j/blob/master/docs/images/settings-classpath.PNG "The settings dialog (classpath tab)")

By default, the classpath is set to include all the jars in the `lib/` directory in the root of the installation directory.  This dialog
lets you include other jars or folders (folders will be searched for all contained jars).  Both the *Add JAR...* and *Add Folder...*
buttons open file dialogs that support selecting more than one file.  Entries can be removed and reorded by selecting and using the 
arrow buttons to the right of the list and *Remove* button at the bottom.  These entries will come *before* the automatically added
entries, allowing you to override classes.

### Quick turn-around development
It is using this mechanism you can include the build folder from your IDE to allow a simple Excel restart to pick up newly edited or 
added classes.

## VM Options

![Settings (VM Options)](https://github.com/McLeodMoores/xl4j/blob/master/docs/images/settings-vm-options.PNG "The settings dialog (VM options)")

The VM Options tab allows you to easily specify options that are passed directly to the Java Virtual Machine.  A range of commonly
used and useful options have been made avialable to save the developer from having to look up the options.  These options are:
 * Turn JVMTI debugging support on and off, which is used by Java debuggers and profiling tools to interact with the JVM.  
   It should be turned off in normal operation as it has a performance penalty.
 * Check JNI calls - this performs extra checks on JNI calls.  It can be disabled in normal operation.
 * Maximum Heap - the number of megabytes allowed for the heap.  This will depend on whether your VM is in-process, out-of-process 
   and whether it's 32 or 64 bit.  Currently VMs are in-process and 32-bit only and so must share the 32-bit address space with Excel.
   This means you shouldn't expect to be able to have very large heaps.  This restriction should change once out-of-process JVMs are
   available.
 * Remote debugging - this automatically enables JSWP debugging over a socket listening on port 8000, which you can use your IDEs 
   remote debugging option to connect to quite straightforwardly.
 * XL4J Logback Level - this sets the logback level used to provide debugging information on your own functions and information about
   XL4J type conversions.  The resulting file can be viewed via the toolbar *Java Logs* button.
 
