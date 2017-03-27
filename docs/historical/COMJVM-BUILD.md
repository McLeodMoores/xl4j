Prerequisites
=============

1. Visual Studio (e.g. x86 2013).  Don't need .NET or Web tools.
2. Java (JDK1.8.0_11)
3. Maven (put in e.g. `C:\Program Files\apache-maven-3.2.2`)
4. Change some environment variables:
    1. 
        - Windows 7: Start Menu, right-click `Computer`, select Properties.
           Click *Advanced System Settings* then *Environment Variables* and in the *System Variables* block
        - Windows 10: Control Panel -> System and Security -> System -> Advanced system settings -> Environment variables
    2. add `C:\Program Files\apache-maven-3.2.2\bin` to `PATH`
    3. Check there is a `JAVA_HOME` system environment variable.  If there is, skip 4.
    4. set `JAVA_HOME` to e.g. `C:\Program Files\Java\jdk1.8.0_11`.  Note it's not the bin directory in this case.
5. Install git from http://git-scm.com/
6. Either copy your .ssh from your existing machine or generate new key pairs and upload to github.  
   See https://help.github.com/articles/set-up-git#setting-up-git .
7. XLL SDK

Building
========
1. Start a command shell (Start->All Programs->Visual Studio 2013->Tools->VS2013 x64 Native Tools Command Prompt).
   Note this is not the same as just running cmd.exe from the search/run menu as it sets up more environment variables.
2. Clone the helper plug-in and install in the local maven repo

        git clone git@github.com:beerdragon/helpers.git
        cd helpers
        mvn install (or mvn install -DskipTests if there are test failures)
        cd ..
        
    
    Make a note of the version number (e.g. `helpers-0.1.2`).
3. **Build the framewrk library**

        **Open Framework in Visual Studio (2013 Office System Developer Resources -> Excel2013XLLSDK -> SAMPLES -> FRAMEWRK)**
        **Build Framewrk.c**
        **Copy frmwrk32 into LIB directory**

4. Clone the maven-native-packaging plug-in and install in the local maven repo after checking that the version number of the `helpers` dependency is the same as that in (1).

        git clone git@github.com:beerdragon/maven-native-packaging.git
        cd maven-native-packaging
        mvn install
            
   Make a note of the version number (e.g. `maven-native-packaging-0.2.1-SNAPSHOT`).
5. Check that:
    - `xllsdk.xml` is present in the examples directory
    - the `target/win32` directory contains `include`, `lib-i386` and `lib-x64` directories
    - the java version properties (`java.i386.build` and `java.x64.build`) in the `properties` section of `jdk.xml` are the same as the version number of the JDK on the system, e.g. `77` for `jdk1.8.0_77`
    - the version number for the `maven-native-packing.version` property matches that in (4)
    - the `version` property matches the JDK version (e.g. `1.8.0`) 
        
    Then build and install the native JDK artifact in the local maven repo:

        cd examples
        mvn -f jdk.xml install
        mvn -f xllsdk.xml install
6. Now go back to your clone of XL4J and build and install. 
        
        mvn install 
7.  Switch to the comjvm/jstub directory:
        
        cd comjvm
        cd jstub
   and check that the `helpers` version in pom.xml matches that in (1). Then build:

        mvn install
        cd ..
8. Check that the `maven-native-packing.version` property in `pom.xml` matches that in (4) and build:

        mvn install
        

  
