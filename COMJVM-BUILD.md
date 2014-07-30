Prerequisites
=============

1. Visual Studio (e.g. x86 2013).  Don't need .NET or Web tools.
2. Java (JDK1.8.0_11)
3. Maven (put in e.g. `C:\Program Files\apache-maven-3.2.2`)
4. Change some enviroment variables:
    1. Start Menu, right-click `Computer`, select Properties.
       Click *Advanced System Settings* then
       *Environment Variables* and in the *System Variables* block
    2. add `C:\Program Files\apache-maven-3.2.2\bin` to `PATH`
    3. Check there is a `JAVA_HOME` system environment variable.  If there is, skip 4.
    4. set `JAVA_HOME` to e.g. `C:\Program Files\Java\jdk1.8.0_11`.  Note it's not the bin directory in this case.
5. Install git from http://git-scm.com/
6. Either copy your .ssh from your existing machine or generate new key pairs and upload to github.  
   See https://help.github.com/articles/set-up-git#setting-up-git .

Building
========
1. Start a command shell (Start->All Programs->Visual Studio 2013->Tools->VS2013 x64 Native Tools Command Prompt).
   Note this is not the same as just running cmd.exe from the search/run menu as it sets up more environment variables.
2. Clone the helper plug-in and install in the local maven repo

        git clone git@github.com:beerdragon/helper.git
        cd helper
        mvn install
        cd ..

3. Clone the maven-native-packaging plug-in and install in the local maven repo

        git clone git@github.com:beerdragon/maven-native-packaging.git
        cd maven-native-packaging
        mvn install

4. Build and install the native JDK artifact in the local maven repo (assuming you're in the maven-native-packaging dir)

        cd examples
        mvn -f jdk.xml install
        cd ../..

5. If this step fails, it may be because the jdk.xml file needs editing to use the snapshot version of the plug-in.  If so
   go to the section that looks like this (run `write jdk.xml` to edit).

        <maven-native-packing.version>0.2.0</maven-native-packing.version>

   and change it to e.g. `0.2.1-SNAPSHOT`.  If you're not sure what version you have installed, have a dig around in
   `.m2/repository/uk/co/beerdragon/` and find a directory that actually has jars in it.
6. Now go back to your clone of Excel4J and go into the `comjvm` directory.
7. Build and install

        mvn install

8. If that doesn't work, try editing the `pom.xml` file and changing the version of maven native packaging in the same way 
    as described above, and then repeat step 7.

  
