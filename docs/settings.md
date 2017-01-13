Settings and Configuration
==========================

XL4J stores all its settings in an `.ini` style file rather than the registry. This makes it easier for developers and admins to manage 
the configuration and makes it less likely that there will be issues with permissions.  To make configuration easier to manage, you can
edit all configuration via the in-built settings panel, which you can access via the Add-in toolbar:

![Toolbar](https://github.com/McLeodMoores/xl4j/blob/master/docs/images/toolbar.png "The default toolbar")

# Configuring XL4J via the Settings dialog
Clicking the Settings icon will open the dialog, which contains three tabs.

## General Add-in Settings

![Settings (Add-in)](https://github.com/McLeodMoores/xl4j/blob/master/docs/images/settings-add-in.PNG "The settings dialog (add-in tab)")

The default tab here is for options relating to the whole Add-in:
 * General Options
   - Enable/disable Garbage Collection **NOTE: THIS OPTION DOESN'T CURRENTLY DO ANYTHING**
   - Enable/disable Toolbar - this turns off the toolbar, which you might like to do to prevent end users from changing options.  Note 
     that if you turn the toolbar off, the only way to turn it back on is by editing the configuration file by hand - see the 
     section below on how to do this.
 * C++ Logging Options
   - Radio buttons to determine whether to send C++ logging to a file (opened via the toolbar), or to Windows Debug, which can be viewed
     using visual studio or `DebugView.exe`.  The latter is quite convenient, although the performance leaves something to be desired.
   - A combo box allowing you to choose the logging level.  `TRACE` is the most detailed and `FATAL` is the least. It is recommended 
     not to have anything below `WARN` during normal operation as the logging causes a significant performance hit.

## Classpath Settings
  
![Settings (Classpath)](https://github.com/McLeodMoores/xl4j/blob/master/docs/images/settings-classpath.PNG "The settings dialog (classpath tab)")

By default, the classpath is set to include all the jars in the `lib/` directory in the root of the installation directory.  This dialog
lets you include other jars or folders (folders will be searched for all contained jars).  Both the **Add JAR...** and **Add Folder...**
buttons open file dialogs that support selecting more than one file.  Entries can be removed and reorded by selecting and using the 
arrow buttons to the right of the list and **Remove** button at the bottom.  These entries will come *before* the automatically added
entries, allowing you to override classes included in the Add-in distribution.

Note that classpath changes will not currently take effect until you restart Excel (This is a limitation of the Oracle JVM in that it
doesn't support being restarted when run in-process).

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

in addition to these easy-to-use common options, you can configure any custom VM options you like.  Just click the **Add...** button
and enter the full option (including any minus signs) and click okay.  You can edit existing options by highlighting them in the 
list and then clicking **Edit...** and similarly, you can remove options by highlighting in the list and clicking the **Remove** button.
The order than options are passed to the JVM can be altered by highlighting options and using the up and down arrows to the right of the
list to move them.  Any changes can be aborted by clicking **Cancel** or confirmed by clicking **OK**.

Note that VM options will not currently take effect until you restart Excel (This is a limitation of the Oracle JVM in that it
doesn't support being restarted when run in-process).

# Configuration file
The configuration file itself is in the [INI file format](https://en.wikipedia.org/wiki/INI_file), which is human-readable, easy to
parse and well understood.  When you first activate the Add-in, it will check for a file in the following locations:
 1. `%APPDATA%\XL4J\inproc\default.ini` - This will typically be something like `C:\Users\username\AppData\Roaming\XL4J\inproc`.  
    The `inproc` (in-process) directory is there so we can support multiple independent configurations for different types of JVM in 
    the future.  Note that the `AppData` folder is hidden by default, so the easiest way of accessing it is usually to click on the path 
    breadcrumb trail and entering/editing the path on the keyboard:
    
    ![How to edit the path in explorer](https://github.com/McLeodMoores/xl4j/blob/master/docs/images/explorer-breadcrumbs.PNG "Windows Explorer where to click to edit path")
    
 2. `<install-dir>\<binary-dir>\default.ini` - This will typically be something like 
    `C:\Users\<username>\Download\xl4j-0.1.0\bin-x86\default.ini` or perhaps on a shared network drive.
    
The behaviour is that if `default.ini` file is found in **1** that is used, otherwise the system checks in **2**.  If the file is 
found in **2**, it is copied to location **1** and there after any modification are made to this copy.  This means users aren't 
always altering each-others settings if the Add-in is deployed on a network drive, but allows admins to distribute default settings
very easily.  Note that if no file is found in either **1** or **2**, an empty file is created in **2**.  XL4J is written to assume
sensible defaults in the absence of any configuration and if any particular setting is missing from the configuration file, it will
fall back to an in-built default value.

