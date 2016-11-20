XL4J Tutorial
=============

# Getting started
## Pre-requisites
You can build XL4J Add-ins on any operating system that supports Java, you *do not need Windows or Visual Studio* unless you
want to modify the native parts of the Add-in, which is unlikely for most users.  Being able to build from Linux or MacOS
can prove very useful later on for e.g. continuous integration, but given that you're likely to want to test and use your 
Add-in in Excel, it is probably advisable to start development there.  Windows 10 is recommended, but anything version of 
Windows later than Vista (7, 8, 8.1, 10, 10 Aniversary Update) should be fine.

You will need to install a current JDK and a version of Maven after 3.1.  You should also have a git client installed.  I
would suggest using the [Chocolatey](https://www.chocolatey.org) package manager, which can install these packages for you 
using:
```
choco install jdk8 maven git
```
but it't fine to just download and install them yourself too.  You'll probably want your favorite IDE too.  If you want to 
be able to actually test your Add-in's, you will probably want to install Excel too!  Any version after (not including) 
Excel 2007 is fine.

Note that you MUST have a 32-bit Java runtime (JRE) installed for XL4J to work.  I have found chocolatey can be a bit hit and miss
at installing this correctly, so I'd recommend downloading the i586 version of the JRE yourself and installing manually.

## Cloning the template project
We're provided a template project that you can use to get started.  It's very basic, and just consists of a Mavne POM which pulls 
in the required libraries and a Maven assembly plug-in manifest which describes for to package the resulting Add-in into both a 
directory (so you can use it directly) and a zip file for distribution.

Start by cloning the project:
```
git clone https://github.com/McLeodMoores/xl4j-template.git
```
Inside you'll find a standard Maven project layout.  Now is a good time to import this project into your IDE of choice, or
fire up a text editor.  

## Adding a new custom Excel function
Try adding this class:
``` java
package com.mcleodmoores.xl4j.template;

import com.mcleodmoores.xl4j.XLFunction;

public final class MyFunctions {
  @XLFunction(name = "MyAdd")
  public static double myadd(double one, double two) {
    return one + two;
  }
}
```
## Build the Add-in
Then build using:
```
mvn install
```
This will pull in required libraries and produce the build artifact.  Have a look in `xl4j-template\target` and you should see two 
files:
 - A folder called `xl4j-template-0.0.1-SHAPSHOT-distribution`
 - A zip file of containing that folder called `xl4j-template-0.0.1-SNAPSHOT-distribution.zip`
 
## Manually add the Add-in to Excel
 1. Start Excel.
 2. On the Backstage (the screen revealed by clicking on the `File` ribbon header or Office button), choose Options.
 3. Next to the dropdown list towards the botton, click 'Go'.
