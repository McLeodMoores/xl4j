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

